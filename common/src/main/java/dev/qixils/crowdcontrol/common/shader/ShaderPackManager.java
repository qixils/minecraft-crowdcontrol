package dev.qixils.crowdcontrol.common.shader;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.SemVer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Offers the {@link ShaderPack} to clients who don't have all the post effects we ship,
 * and remembers which accepted it.
 *
 * @param <P> class used to represent online players
 */
public final class ShaderPackManager<P> {

	/**
	 * How long to wait after a player joins before offering them the pack.
	 * The mod answers the version request within a few ticks so after a couple seconds
	 * we'll know what they have available.
	 */
	private static final Duration MOD_HANDSHAKE_GRACE = Duration.ofSeconds(5);

	private final Plugin<P, ?> plugin;
	private final Set<UUID> loaded = ConcurrentHashMap.newKeySet();

	// written on the server thread, read from the scheduler thread that offers packs to players
	private volatile @Nullable ShaderPack pack;
	private volatile @Nullable ShaderPackServer server;
	private volatile @NotNull ShaderPackConfig config = ShaderPackConfig.DEFAULT;
	private volatile boolean started;
	private volatile boolean warnedAboutAddress;

	public ShaderPackManager(@NotNull Plugin<P, ?> plugin) {
		this.plugin = plugin;
	}

	/**
	 * Applies the given configuration, restarting pack hosting if anything about it changed.
	 * Players who are already online are re-offered the pack.
	 *
	 * @param config     configuration to apply
	 * @param packFormat the client resource pack format of the running game version
	 */
	public void reload(@NotNull ShaderPackConfig config, int packFormat) {
		if (started && this.config.equals(config)) return;

		boolean isRestart = started;
		start(config, packFormat);

		if (isRestart && pack != null) {
			for (P player : plugin.getPlayerManager().getAllPlayersFull())
				plugin.playerMapper().tryGetUniqueId(player).ifPresent(this::offerTo);
		}
	}

	/**
	 * (Re)starts pack hosting using the given configuration.
	 *
	 * @param config     configuration to apply
	 * @param packFormat the client resource pack format of the running game version
	 */
	public void start(@NotNull ShaderPackConfig config, int packFormat) {
		stop();
		this.config = config;
		this.started = true;
		this.warnedAboutAddress = false;

		if (!config.enabled()) {
			plugin.getSLF4JLogger().debug("Shader resource pack is disabled");
			return;
		}

		final ShaderPack localPack;
		try {
			localPack = pack = ShaderPack.build(packFormat);
		} catch (IOException e) {
			plugin.getSLF4JLogger().warn("Could not build the shader resource pack", e);
			return;
		}

		if (!config.url().isBlank()) {
			plugin.getSLF4JLogger().info("Serving shader resource pack from the configured URL {}", config.url());
			return;
		}

		try {
			final ShaderPackServer localServer = server = ShaderPackServer.start(localPack, config.bindAddress(), config.port());
			plugin.getSLF4JLogger().info("Hosting shader resource pack on port {}", localServer.port());
		} catch (IOException e) {
			plugin.getSLF4JLogger().warn("Could not host the shader resource pack on port {}", config.port(), e);
			pack = null;
		}
	}

	/**
	 * Stops pack hosting and forgets which players had the pack loaded.
	 */
	public void stop() {
		final ShaderPackServer runningServer = server;
		if (runningServer != null) {
			runningServer.close();
			server = null;
		}
		pack = null;
		started = false;
		loaded.clear();
	}

	/**
	 * Schedules a potential offer of the pack to a joining player.
	 *
	 * @param player joining player
	 */
	public void onPlayerJoin(@NotNull P player) {
		if (pack == null) return;

		UUID uuid = plugin.playerMapper().tryGetUniqueId(player).orElse(null);
		if (uuid == null) return;

		plugin.getScheduledExecutor().schedule(
			() -> offerTo(uuid),
			MOD_HANDSHAKE_GRACE.toMillis(),
			TimeUnit.MILLISECONDS
		);
	}

	/**
	 * Forgets a departing player.
	 *
	 * @param uuid departing player
	 */
	public void onPlayerLeave(@NotNull UUID uuid) {
		loaded.remove(uuid);
	}

	/**
	 * Whether the given player's client can render the given post effect.
	 * Must be a vanilla shader, an up-to-date mod, or have accepted the resource pack.
	 *
	 * @param player player to check
	 * @param shader post effect to check
	 * @return whether the effect will render for this player
	 */
	public boolean canRender(@NotNull P player, @NotNull Shader shader) {
		if (!shader.bundled()) return true;
		if (hasModSince(player, shader.addedIn())) return true;
		UUID uuid = plugin.playerMapper().tryGetUniqueId(player).orElse(null);
		return uuid != null && loaded.contains(uuid);
	}

	/**
	 * Whether the player's mod is missing any of the post effects we ship.
	 */
	private boolean needsPack(@NotNull P player) {
		return !hasModSince(player, Shader.NEWEST_BUNDLED);
	}

	private boolean hasModSince(@NotNull P player, @NotNull SemVer version) {
		return plugin.getModVersion(player).filter(modVersion -> modVersion.isAtLeast(version)).isPresent();
	}

	private void offerTo(UUID uuid) {
		ShaderPack pack = this.pack;
		if (pack == null) return;

		Optional<P> optPlayer = plugin.playerMapper().getPlayer(uuid);
		if (optPlayer.isEmpty()) return;
		P player = optPlayer.get();

		// clients whose mod holds every shader need nothing from us
		if (!needsPack(player)) return;

		URI uri = resolveUri(player);
		if (uri == null) return;

		ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(pack.id(), uri, pack.hash());
		Audience audience = plugin.playerMapper().asAudience(player);
		// ensure the translated version is sent
		Locale locale = plugin.playerMapper().getLocale(player).orElse(Locale.US);
		Component prompt = GlobalTranslator.render(Component.translatable("cc.shader-pack.prompt"), locale);

		audience.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
			.packs(info)
			.replace(false)
			.required(config.required())
			.prompt(prompt)
			.callback((packId, status, ignored) -> onPackStatus(uuid, status))
			.build());
	}

	private void onPackStatus(UUID uuid, ResourcePackStatus status) {
		if (status.intermediate()) return;

		if (status == ResourcePackStatus.SUCCESSFULLY_LOADED)
			loaded.add(uuid);
		else
			loaded.remove(uuid);

		plugin.getSLF4JLogger().debug("Shader resource pack {} for client {}", status, uuid);
		plugin.updateConditionalEffectVisibility(uuid);
	}

	/**
	 * Works out where the given player should download the pack from.
	 */
	private @Nullable URI resolveUri(P player) {
		ShaderPack pack = this.pack;
		if (pack == null) return null;

		try {
			if (!config.url().isBlank())
				return new URI(config.url());

			ShaderPackServer server = this.server;
			if (server == null) return null;

			String host = firstNonBlank(config.publicAddress(), plugin.getShaderPackHost(player), localHost());
			if (host == null) {
				if (!warnedAboutAddress) {
					warnedAboutAddress = true;
					plugin.getSLF4JLogger().warn("Could not determine an address for clients to download the shader resource pack from; please set shader-pack.public-address in the config to enable modded shaders");
				}
				return null;
			}

			// URI's multi-argument constructor wants IPv6 literals bracketed
			if (host.indexOf(':') >= 0 && !host.startsWith("["))
				host = "[" + host + "]";

			return new URI("http", null, host, server.port(), server.path(), null, null);
		} catch (URISyntaxException e) {
			plugin.getSLF4JLogger().warn("Could not build a shader resource pack URL", e);
			return null;
		}
	}

	private static @Nullable String localHost() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	private static @Nullable String firstNonBlank(@Nullable String @NotNull ... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) return value.trim();
		}
		return null;
	}
}
