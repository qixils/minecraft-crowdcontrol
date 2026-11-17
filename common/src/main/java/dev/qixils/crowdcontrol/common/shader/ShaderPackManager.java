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
import java.net.URI;
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
	private volatile @Nullable ResourcePackInfo pack;
	private volatile @NotNull ShaderPackConfig config = ShaderPackConfig.DEFAULT;
	private volatile boolean started;
	private volatile boolean warnedAboutDownload;

	public ShaderPackManager(@NotNull Plugin<P, ?> plugin) {
		this.plugin = plugin;
	}

	/**
	 * Applies the given configuration, restarting if anything about it changed.
	 * Players who are already online are re-offered the pack.
	 *
	 * @param config configuration to apply
	 */
	public void reload(@NotNull ShaderPackConfig config) {
		if (started && this.config.equals(config)) return;

		boolean isRestart = started;
		start(config);

		if (isRestart && pack != null) {
			for (P player : plugin.getPlayerManager().getAllPlayersFull())
				plugin.playerMapper().tryGetUniqueId(player).ifPresent(this::offerTo);
		}
	}

	/**
	 * (Re)starts offering the pack using the given configuration.
	 *
	 * @param config configuration to apply
	 */
	public void start(@NotNull ShaderPackConfig config) {
		stop();
		this.config = config;
		this.started = true;
		this.warnedAboutDownload = false;

		if (!config.enabled()) {
			plugin.getSLF4JLogger().debug("Shader resource pack is disabled");
			return;
		}

		final ShaderPack shaderPack;
		try {
			shaderPack = ShaderPack.load();
		} catch (IOException e) {
			plugin.getSLF4JLogger().warn("Could not read the shader resource pack details", e);
			return;
		}

		final URI uri;
		if (shaderPack.url() != null) {
			uri = shaderPack.url();
		} else {
			plugin.getSLF4JLogger().info("Shader pack URL unavailable, disabling");
			return;
		}

		plugin.getSLF4JLogger().info("Serving shaders from {}", uri);
		pack = ResourcePackInfo.resourcePackInfo(shaderPack.id(), uri, shaderPack.hash());
	}

	/**
	 * Stops offering the pack and forgets which players had it loaded.
	 */
	public void stop() {
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
		ResourcePackInfo pack = this.pack;
		if (pack == null) return;

		Optional<P> optPlayer = plugin.playerMapper().getPlayer(uuid);
		if (optPlayer.isEmpty()) return;
		P player = optPlayer.get();

		// clients whose mod holds every shader need nothing from us
		if (!needsPack(player)) return;

		Audience audience = plugin.playerMapper().asAudience(player);
		// ensure the translated version is sent
		Locale locale = plugin.playerMapper().getLocale(player).orElse(Locale.US);
		Component prompt = GlobalTranslator.render(Component.translatable("cc.shader-pack.prompt"), locale);

		audience.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
			.packs(pack)
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
		if (status == ResourcePackStatus.FAILED_DOWNLOAD || status == ResourcePackStatus.INVALID_URL)
			warnAboutDownload(status);
		plugin.updateConditionalEffectVisibility(uuid);
	}

	private void warnAboutDownload(ResourcePackStatus status) {
		ResourcePackInfo pack = this.pack;
		if (pack == null || warnedAboutDownload) return;
		warnedAboutDownload = true;
		// a mismatched hash is reported as a failed download too
		plugin.getSLF4JLogger().warn("A player failed to download the shader pack from ({})", status);
	}
}
