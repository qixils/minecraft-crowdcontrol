package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.TrackedEffect;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.Application;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import live.crowdcontrol.cc4j.CCEventType;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.UserToken;
import live.crowdcontrol.cc4j.websocket.data.CCEffectReport;
import live.crowdcontrol.cc4j.websocket.data.IdentifierType;
import live.crowdcontrol.cc4j.websocket.data.ReportStatus;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.incendo.cloud.Command.Builder;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.PredicatePermission;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.util.CollectionUtil.initTo;
import static dev.qixils.crowdcontrol.common.util.OptionalUtil.stream;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static org.incendo.cloud.description.Description.description;

/**
 * The main class used by a Crowd Control implementation which defines numerous methods for
 * managing Crowd Control server/client connections and handling {@link Command}s.
 *
 * @param <P> class used to represent online players
 * @param <S> class used to represent command senders in Cloud Command Framework
 */
public abstract class Plugin<P, S> {

	/**
	 * The color to use for basic messages rendered to players when joining the server.
	 */
	public static final TextColor JOIN_MESSAGE_COLOR = TextColor.color(0xFCE9D4);

	/**
	 * Text color to use for usernames.
	 */
	public static final TextColor USER_COLOR = TextColor.color(0x9f44db);

	/**
	 * Text color to use for command names.
	 */
	public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);

	/**
	 * A less saturated version of {@link #CMD_COLOR}.
	 */
	public static final TextColor DIM_CMD_COLOR = TextColor.color(0xA982C2);

	/**
	 * The color used for displaying error messages on join.
	 */
	public static final TextColor _ERROR_COLOR = TextColor.color(0xF78080);

	/**
	 * The prefix to use in command output.
	 */
	public static final String PREFIX = "CrowdControl";

	/**
	 * The mod ID / namespace to use in {@link net.kyori.adventure.key.Key Key}s.
	 */
	public static final String NAMESPACE = "crowdcontrol";

	/**
	 * Key for the Version Request packet.
	 */
	public static final Key VERSION_REQUEST_KEY = Key.key(NAMESPACE, "version-request");

	/**
	 * Key for the Version Response packet.
	 */
	public static final Key VERSION_RESPONSE_KEY = Key.key(NAMESPACE, "version-response");

	/**
	 * Key for the Shader packet.
	 */
	public static final Key SHADER_KEY = Key.key(NAMESPACE, "shader");

	/**
	 * Key for the Movement Status packet.
	 */
	public static final Key MOVEMENT_STATUS_KEY = Key.key(NAMESPACE, "movement-status");

	/**
	 * Key for the Extra Features packet.
	 */
	public static final Key EXTRA_FEATURE_KEY = Key.key(NAMESPACE, "extra-feature");

	/**
	 * Key for the Set Language packet.
	 */
	public static final Key SET_LANGUAGE_KEY = Key.key(NAMESPACE, "set-language");

	/**
	 * The prefix to use in command output as a {@link Component}.
	 */
	public static final Component PREFIX_COMPONENT = text()
		.append(text('[', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
		.append(text(PREFIX, NamedTextColor.YELLOW))
		.append(text(']', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
		.appendSpace()
		.build();

	/**
	 * The default name of a viewer.
	 */
	public static final Component VIEWER_NAME = translatable("cc.effect.viewer");

	/**
	 * The permission node required to receive effects.
	 */
	public static final PermissionWrapper USE_PERMISSION = PermissionWrapper.builder()
		.node("crowdcontrol.use")
		.description("Whether a player is allowed to receive effects")
		.defaultPermission(PermissionWrapper.DefaultPermission.ALL)
		.build();

	/**
	 * The permission node required to use administrative commands.
	 */
	public static final PermissionWrapper ADMIN_PERMISSION = PermissionWrapper.builder()
		.node("crowdcontrol.admin")
		.description("Whether a player is allowed to use administrative commands")
		.defaultPermission(PermissionWrapper.DefaultPermission.OP)
		.build();

	/**
	 * Formats the provided text as an error message.
	 *
	 * @param text text to format
	 * @return formatted text
	 */
	public static @NotNull Component error(@NotNull Component text) {
		if (text.decoration(TextDecoration.BOLD) == TextDecoration.State.NOT_SET)
			text = text.decoration(TextDecoration.BOLD, false);
		return translatable(
			"cc.error.prefix.critical",
			NamedTextColor.RED,
			EnumSet.of(TextDecoration.BOLD),
			text.colorIfAbsent(_ERROR_COLOR)
		);
	}

	/**
	 * Formats the provided text as a warning message.
	 *
	 * @param text text to format
	 * @return formatted text
	 */
	public static @NotNull Component warning(@NotNull Component text) {
		if (text.decoration(TextDecoration.BOLD) == TextDecoration.State.NOT_SET)
			text = text.decoration(TextDecoration.BOLD, false);
		return translatable(
			"cc.error.prefix.warning",
			NamedTextColor.RED,
			EnumSet.of(TextDecoration.BOLD),
			text.colorIfAbsent(_ERROR_COLOR)
		);
	}

	/**
	 * Formats the provided text as a command output.
	 *
	 * @param text text to format
	 * @return formatted text
	 */
	public static @NotNull Component output(@NotNull Component text) {
		return PREFIX_COMPONENT.append(text);
	}

	/**
	 * The message to send to a player when they join the server.
	 */
	public static final Component JOIN_MESSAGE = translatable(
		"cc.join.info",
		JOIN_MESSAGE_COLOR,
		// TODO: move these args back into the i18n file?
		text("Crowd Control", TextColor.color(0xFAE100)),
		text("qi", TextColor.color(0xFFC7B5))
			.append(text("xi", TextColor.color(0xFFDECA)))
			.append(text("ls", TextColor.color(0xFFCEEA)))
			.append(text(".dev", TextColor.color(0xFFB7E5))),
		text("crowdcontrol.live", TextColor.color(0xFAE100))
	);

	/**
	 * A warning message sent to players when they join the server if global effects are
	 * completely unavailable.
	 */
	public static final Component NO_GLOBAL_EFFECTS_MESSAGE = warning(translatable(
		"cc.error.no-global-effects",
		text("global", TextColor.color(0xF9AD9E)),
		text("true", TextColor.color(0xF9AD9E)),
		text("hosts", TextColor.color(0xF9AD9E))
	));

	@NotNull
	protected final Class<P> playerClass;
	@NotNull
	protected final Class<S> commandSenderClass;
	@Nullable
	protected CrowdControl crowdControl = null;
	protected boolean global = false;
	protected boolean announce = true;
	@NotNull
	protected HideNames hideNames = HideNames.NONE;
	@NotNull
	protected Collection<String> hosts = Collections.emptySet();
	@NotNull
	protected LimitConfig limitConfig = new LimitConfig();
	@NotNull
	protected SoftLockConfig softLockConfig = new SoftLockConfig();
	@NotNull
	protected final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	protected final Map<UUID, SemVer> clientVersions = new HashMap<>();
	protected final Map<UUID, Set<ExtraFeature>> extraFeatures = new HashMap<>();
	protected final Map<UUID, TrackedEffect> trackedEffects = new HashMap<>();

	protected Plugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
		this.playerClass = playerClass;
		this.commandSenderClass = commandSenderClass;
	}

	/**
	 * Gets the {@link CrowdControl} instance.
	 *
	 * @return crowd control instance
	 */
	@Nullable
	@CheckReturnValue
	public CrowdControl getCrowdControl() {
		return crowdControl;
	}

	/**
	 * Gets the player class utilized by this implementation.
	 *
	 * @return player class
	 */
	@NotNull
	@CheckReturnValue
	public Class<P> getPlayerClass() {
		return playerClass;
	}

	/**
	 * Gets the command sender class utilized by this implementation.
	 *
	 * @return command sender class
	 */
	@NotNull
	@CheckReturnValue
	public Class<S> getCommandSenderClass() {
		return commandSenderClass;
	}

	/**
	 * Fetches the config variable which determines if all requests should be treated as global.
	 *
	 * @return true if all requests should be treated as global
	 */
	@CheckReturnValue
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Returns a collection of strings representing the names of hosts.
	 * <p>
	 * "Hosts" are defined as streamers whose incoming requests should apply to all online players
	 * instead of just that streamer.
	 *
	 * @return a collection of strings possibly containing stream usernames, IDs, Minecraft
	 * usernames, or Minecraft UUIDs
	 */
	@CheckReturnValue
	@NotNull
	public Collection<String> getHosts() {
		return hosts;
	}

	/**
	 * Whether to announce the execution of effects in chat.
	 *
	 * @return true if the plugin should announce the execution of effects in chat
	 */
	@CheckReturnValue
	public boolean announceEffects() {
		return announce;
	}

	/**
	 * Sets whether to announce the execution of effects in chat.
	 *
	 * @param announceEffects true if the plugin should announce the execution of effects in chat
	 */
	public void setAnnounceEffects(boolean announceEffects) {
		this.announce = announceEffects;
	}

	/**
	 * Gets the {@link ScheduledExecutorService} used by the plugin.
	 *
	 * @return the executor service
	 */
	@NotNull
	public ScheduledExecutorService getScheduledExecutor() {
		return scheduledExecutor;
	}

	/**
	 * Gets the {@link HideNames} config.
	 *
	 * @return hide names config
	 */
	@NotNull
	public HideNames getHideNames() {
		return hideNames;
	}

	/**
	 * Sets the {@link HideNames} config.
	 *
	 * @param hideNames hide names config
	 */
	public void setHideNames(@NotNull HideNames hideNames) {
		this.hideNames = hideNames;
	}

	/**
	 * Gets the plugin's {@link LimitConfig}.
	 *
	 * @return limit config parsed from the plugin's config file
	 */
	@NotNull
	public LimitConfig getLimitConfig() {
		return limitConfig;
	}

	/**
	 * Gets the plugin's {@link SoftLockConfig}.
	 *
	 * @return soft-lock config parsed from the plugin's config file
	 */
	@NotNull
	public SoftLockConfig getSoftLockConfig() {
		return softLockConfig;
	}

	/**
	 * Gets the {@link EntityMapper} for this implementation's player object.
	 *
	 * @return player entity mapper
	 */
	public abstract PlayerEntityMapper<P> playerMapper();

	/**
	 * Gets the {@link EntityMapper} for this implementation's command sender object.
	 *
	 * @return command sender mapper
	 */
	public abstract EntityMapper<S> commandSenderMapper();

	/**
	 * Gets the object that maps {@link PublicEffectPayload}s to the players it should affect.
	 *
	 * @return mapper object
	 */
	@NotNull
	@CheckReturnValue
	public abstract PlayerManager<P> getPlayerManager();

	/**
	 * Returns the plugin's text utility class.
	 */
	@CheckReturnValue
	@NotNull
	public abstract TextUtil getTextUtil();

	/**
	 * Returns the object that manages the registering of effects/commands.
	 * Not to be confused with the {@link #getCommandManager() chat command manager}.
	 *
	 * @return command registry manager
	 */
	@NotNull
	public abstract AbstractCommandRegister<P, ?> commandRegister();

	public abstract @NotNull Path getDataFolder();

	/**
	 * Gets the plugin's {@link CommandManager}.
	 *
	 * @return command manager instance
	 */
	@Nullable
	@CheckReturnValue
	public abstract CommandManager<S> getCommandManager();

	/**
	 * Gets the plugin's SLF4J logger.
	 *
	 * @return slf4j logger
	 */
	@NotNull
	public abstract Logger getSLF4JLogger();

	/**
	 * Gets the executor which runs code synchronously (i.e. on the server's main thread).
	 *
	 * @return synchronous executor
	 */
	@NotNull
	public abstract Executor getSyncExecutor();

	/**
	 * Gets the executor which runs code asynchronously (i.e. off the server's main thread).
	 *
	 * @return asynchronous executor
	 */
	@NotNull
	public abstract Executor getAsyncExecutor();

	/**
	 * Gets the server's console {@link Audience}.
	 *
	 * @return console audience
	 */
	@NotNull
	public abstract Audience getConsole();

	/**
	 * Gets the metadata of the Minecraft server.
	 *
	 * @return server version
	 */
	@NotNull
	public abstract VersionMetadata getVersionMetadata();

	/**
	 * Gets the plugin's {@link MCCCPlayer wrapper} for a player.
	 *
	 * @param player player to get the wrapper for
	 * @return wrapper for the player
	 */
	@NotNull
	public abstract MCCCPlayer getPlayer(@NotNull P player);

	/**
	 * Attempts to cast the provided object as a player.
	 * This should not be directly extended, instead see {@link #asPlayer(Object)}.
	 *
	 * @param obj object to cast
	 * @return casted player, or null if the object is not a player
	 */
	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public @Nullable P objAsPlayer(@NotNull Object obj) {
		try {
			Class<P> playerClass = getPlayerClass();
			if (!playerClass.isInstance(obj))
				return null;
			return playerClass.cast(obj);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets the provided command sender as a player.
	 *
	 * @param sender the command sender
	 * @return the player, or null if the sender is not a player
	 */
	public @Nullable P asPlayer(@NotNull S sender) {
		return objAsPlayer(sender);
	}

	/**
	 * Registers the plugin's basic chat commands.
	 */
	public void registerChatCommands() {
		try {
			GlobalTranslator.translator().addSource(new KyoriTranslator("crowdcontrol", "CrowdControl", this, Locale.US));
		} catch (Exception e) {
			getSLF4JLogger().error("Failed to initialize i18n", e);
		}

		CommandManager<S> manager = getCommandManager();
		if (manager == null)
			throw new IllegalStateException("CommandManager is null");
		EntityMapper<S> mapper = commandSenderMapper();

		// TODO: support i18n in cloud command descriptions

		//// Account Command ////

		//// CrowdControl Command ////

		// base command
		Builder<S> ccCmd = manager.commandBuilder("crowdcontrol")
			.commandDescription(description("View information about and manage the Crowd Control service"));

		// status command
		manager.command(ccCmd.literal("status")
			.commandDescription(description("Get the status of the Crowd Control service"))
			.permission(PredicatePermission.of(mapper::isAdmin))
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.sender());
				if (crowdControl == null) {
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				TextComponent.Builder msg = text()
					.append(PREFIX_COMPONENT)
					.append(translatable("cc.command.crowdcontrol.status.online"))
					.appendSpace();
				boolean added = false;
				for (P player : getPlayerManager().getAllPlayersFull()) {
					CCPlayer ccPlayer = optionalCCPlayer(player).orElse(null);
					if (ccPlayer == null) continue;
					UserToken user = ccPlayer.getUserToken();
					if (user == null) continue;

					if (!added)
						msg.append(translatable("cc.command.crowdcontrol.status.sources.header"));
					added = true;

					msg.appendNewline().append(translatable(
						String.format(
							"cc.command.crowdcontrol.status.sources.%s",
							ccPlayer.getGameSessionId() == null ? "offline" : "live"
						),
						text(playerMapper().getUsername(player)),
						text(user.getName())
					));
				}
				audience.sendMessage(msg);
			}));
		// version command
		manager.command(ccCmd.literal("version")
			.commandDescription(description("Get the version of the server's and players' Crowd Control mod"))
			.handler(ctx -> {
				Audience audience = mapper.asAudience(ctx.sender());
				Component message = output(translatable("cc.command.crowdcontrol.version.server", text(SemVer.MOD.toString())));
				audience.sendMessage(message.appendSpace().append(translatable("cc.command.crowdcontrol.version.clients.header")));
				for (P player : getPlayerManager().getAllPlayersFull()) {
					Optional<SemVer> version = getModVersion(player);
					if (version.isPresent()) {
						String key = "cc.command.crowdcontrol.version.client." + (version.get().equals(SemVer.MOD) ? "match" : "mismatch");
						audience.sendMessage(translatable(key, text(playerMapper().getUsername(player)), text(version.get().toString())));
					} else
						audience.sendMessage(translatable("cc.command.crowdcontrol.version.client.unknown", text(playerMapper().getUsername(player))));
					// TODO: extra features
				}
			}));
		// execute command
		if (SemVer.MOD.isSnapshot()) { // TODO: make command generally available
			manager.command(ccCmd.literal("execute")
				.commandDescription(description("Executes the effect with the given ID"))
				.permission(PredicatePermission.of(mapper::isAdmin))
				.argument(
					StringParser
						.stringComponent(StringParser.StringMode.SINGLE)
						.name("effect")
						.description(description("ID of the effect to execute"))
						.required()
						.suggestionProvider((context, input) -> CompletableFuture.completedFuture(commandRegister()
							.getCommands()
							.stream()
							.filter(command -> command.getEffectName().toLowerCase().contains(input.lastRemainingToken().toLowerCase()))
							.map(command -> Suggestion.suggestion(command.getEffectName()))
							.sorted(Comparator.comparing(Suggestion::suggestion))
							.collect(Collectors.toList())))
				)
				.handler(commandContext -> {
					// TODO: allow targeting multiple players
					S sender = commandContext.sender();
					Audience audience = mapper.asAudience(sender);
					P player = asPlayer(sender);
					if (player == null) {
						audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
						return;
					}
					Command<P> effect = commandRegister().getCommandByName(commandContext.get("effect"));
					List<P> players = new ArrayList<>(Collections.singletonList(player));
					// TODO: add simpler constructors
					effect.execute(
						() -> players,
						new PublicEffectPayload(
							UUID.randomUUID(),
							0L,
							new CCEffectDescription(
								effect.getEffectName(),
								"game",
								new CCName(getTextUtil().asPlain(effect.getDisplayName())),
								null,
								null,
								null,
								false,
								false,
								false,
								false,
								false,
								false,
								null,
								null,
								null,
								10
							),
							new CCUserRecord(
								"ccuid-01j7cnrvpbh5aw45pwpe1vqvdw",
								"lexikiq",
								ProfileType.TWITCH,
								"106025167",
								""
							),
							null,
							null,
							false,
							1
						),
						optionalCCPlayer(player).orElseThrow()
					);
				})
			);
		}

		MinecraftExceptionHandler.<S>create(mapper::asAudience)
			.defaultHandlers()
			.decorator(component -> output(component).color(NamedTextColor.RED))
			.registerTo(manager);
	}

	/**
	 * Loads the configuration file.
	 */
	public abstract void loadConfig();

	/**
	 * Determines whether it's possible for global effects to execute for the specified player.
	 *
	 * @param player player to check
	 * @return true if global effects could execute
	 */
	public boolean globalEffectsUsableFor(@NotNull P player) {
		if (isGlobal()) return true;

		return isHost(player);
	}

	/**
	 * Whether the specified player is known to be a server host.
	 * Used for managing item and entity limits, even with global effects enabled.
	 *
	 * @param player player to check
	 * @return whether the player is a server host
	 */
	public boolean isHost(@NotNull P player) {
		Collection<String> hosts = getHosts();
		if (hosts.isEmpty()) return false;

		Optional<UUID> optUuid = playerMapper().tryGetUniqueId(player);
		if (optUuid.isPresent()) {
			String uuidStr = optUuid.get().toString().replace("-", "");
			if (hosts.stream().anyMatch(host -> host.replace("-", "").equalsIgnoreCase(uuidStr))) return true;
		}

		String name = playerMapper().getUsername(player);
		if (hosts.stream().anyMatch(host -> host.equalsIgnoreCase(name))) return true;

		UserToken userToken = optUuid.flatMap(this::optionalCCPlayer).map(CCPlayer::getUserToken).orElse(null);
		if (userToken == null) return false;

		String userId = userToken.getId().replaceFirst("^ccuid-", "");
		if (hosts.stream().anyMatch(host -> host.replaceFirst("^ccuid-", "").equalsIgnoreCase(userId))) return true;

		String ccName = userToken.getName();
		if (hosts.stream().anyMatch(host -> host.equalsIgnoreCase(ccName))) return true;

		String platformId = userToken.getOriginId();
		return hosts.stream().anyMatch(host -> host.equalsIgnoreCase(platformId));
	}

	/**
	 * Registers a {@link Command} with the plugin.
	 *
	 * @param name    the name of the command
	 * @param command the command to register
	 */
	public void registerCommand(@NotNull String name, @NotNull Command<P> command) {
		name = name.toLowerCase(Locale.ENGLISH);
		if (crowdControl == null)
			throw new IllegalStateException("CrowdControl is not initialized");
		try {
			if (crowdControl.addEffect(name, command))
				getSLF4JLogger().debug("Registered CC command '{}'", name);
			else
				getSLF4JLogger().warn("Command '{}' rejected, duplicate?", name);
		} catch (IllegalArgumentException e) {
			getSLF4JLogger().warn("Failed to register command '{}'", name, e);
		}
	}

	public void trackEffect(@NotNull UUID requestId, @NotNull TrackedEffect effect) {
		trackedEffects.put(requestId, effect);
	}

	/**
	 * Gets the {@link CrowdControl} instance as an optional.
	 *
	 * @return crowd control instance
	 */
	@NotNull
	@CheckReturnValue
	public Optional<CrowdControl> optionalCrowdControl() {
		return Optional.ofNullable(crowdControl);
	}

	/**
	 * Gets the {@link CCPlayer} for the provided player.
	 *
	 * @param player player id
	 * @return crowd control player
	 */
	@NotNull
	@CheckReturnValue
	public Optional<CCPlayer> optionalCCPlayer(@NotNull UUID player) {
		return optionalCrowdControl().map(cc -> cc.getPlayer(player));
	}

	/**
	 * Gets the {@link CCPlayer} for the provided player.
	 *
	 * @param player player
	 * @return crowd control player
	 */
	@NotNull
	@CheckReturnValue
	public Optional<CCPlayer> optionalCCPlayer(@NotNull P player) {
		return optionalCCPlayer(playerMapper().getUniqueId(player));
	}

	/**
	 * (Re)initializes the {@link CrowdControl} instance.
	 */
	public void initCrowdControl() {
		loadConfig();
		crowdControl = new CrowdControl("Minecraft", "Minecraft", Application.APPLICATION_ID, Application.APPLICATION_SECRET, getDataFolder());
		commandRegister().register();
		// re-trigger player join for any missed players
		getPlayerManager().getAllPlayersFull().forEach(this::onPlayerJoin);
	}

	public CompletableFuture<?> shutdown() {
		if (crowdControl != null) {
			getPlayerManager().getAllPlayersFull().forEach(this::onPlayerLeave);
			crowdControl.close();
			crowdControl = null;
		}
		scheduledExecutor.shutdown();
		return CompletableFuture.supplyAsync(() -> {
			try {
				return scheduledExecutor.awaitTermination(3, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Gets the visibility of conditional effects (i.e. client effects & global effects).
	 *
	 * @param user the player to generate the packets for
	 */
	@CheckReturnValue
	public @NotNull CCEffectReport @NotNull [] getConditionalEffectVisibility(IUserRecord user) {
		// TODO: some scenarios here we might want to return a full UNAVAILABLE ? or stop session or something?
		if (user == null) return new CCEffectReport[0];
		if (crowdControl == null) return new CCEffectReport[0];

		List<P> potentialPlayers = getPlayerManager().getPotentialPlayers(user).collect(Collectors.toList());
		if (potentialPlayers.isEmpty()) return new CCEffectReport[0];

		SemVer clientVersion = potentialPlayers.stream().map(player -> getModVersion(player).orElse(SemVer.ZERO)).max(SemVer::compareTo).orElse(SemVer.ZERO);
		boolean globalVisible = potentialPlayers.stream().anyMatch(this::globalEffectsUsableFor);
		getSLF4JLogger().debug("Updating conditional effects: clientVersion={}, globalVisible={}", clientVersion, globalVisible);
		Map<ReportStatus, List<String>> effects = new HashMap<>();

		// TODO: probably don't need to recompute this every time!
		//  this would be a great case for AbstractPlugin
		List<Command<P>> registeredEffects = commandRegister().getCommands();
		List<String> registeredEffectIds = registeredEffects.stream()
			.map(Command::getEffectName)
			.map(name -> name.toLowerCase(Locale.ENGLISH))
			.collect(Collectors.toList());
		List<String> unknownEffects = stream(
			Optional.ofNullable(crowdControl.getGamePack())
			.map(pack -> pack.getEffects().getGame())
		)
			.flatMap(map -> map.keySet().stream())
			.filter(id -> !registeredEffectIds.contains(id))
			.collect(Collectors.toList());

		if (!unknownEffects.isEmpty()) {
			effects.put(ReportStatus.MENU_HIDDEN, unknownEffects);
		}

		for (Command<P> effect : commandRegister().getCommands()) {
			String id = effect.getEffectName().toLowerCase(Locale.ENGLISH);
			TriState visibility;
			try {
				visibility = effect.isVisible(user, potentialPlayers);
				if (visibility != TriState.FALSE) {
					Set<ExtraFeature> extraFeatures;
					SemVer minVersion;
					// this assumes that if a player has a feature available then they also have a client available
					if (!(extraFeatures = effect.requiredExtraFeatures()).isEmpty()) {
						boolean available = potentialPlayers.stream().anyMatch(player -> extraFeatures.stream().allMatch(feature -> isFeatureAvailable(feature, player)));
						visibility = TriState.fromBoolean(available);
					} else if ((minVersion = effect.getMinimumModVersion()).isAtLeast(SemVer.ZERO)) {
						visibility = TriState.fromBoolean(clientVersion.isAtLeast(minVersion));
					} else if (effect.isGlobal())
						visibility = TriState.fromBoolean(globalVisible);
				}
			} catch (Exception e) {
				getSLF4JLogger().error("Hiding faulty effect {}", id, e);
				visibility = TriState.FALSE;
			}

			if (visibility != TriState.UNKNOWN)
				effects.computeIfAbsent(visibility == TriState.TRUE ? ReportStatus.MENU_VISIBLE : ReportStatus.MENU_HIDDEN, k -> new ArrayList<>()).add(id);

			TriState selectable;
			try {
				selectable = effect.isSelectable(user, potentialPlayers);
			} catch (Exception e) {
				getSLF4JLogger().error("Disabling faulty effect {}", id, e);
				selectable = TriState.FALSE;
			}

			if (selectable != TriState.UNKNOWN && visibility != TriState.FALSE)
				effects.computeIfAbsent(selectable == TriState.TRUE ? ReportStatus.MENU_AVAILABLE : ReportStatus.MENU_UNAVAILABLE, k -> new ArrayList<>()).add(id);
		}

		CCEffectReport[] reports = new CCEffectReport[effects.size()];
		int i = 0;
		for (Map.Entry<ReportStatus, List<String>> entry : effects.entrySet()) {
			reports[i++] = new CCEffectReport(
				IdentifierType.EFFECT,
				entry.getKey(),
				entry.getValue()
			);
		}

		return reports;
	}

	/**
	 * Updates the visibility of conditional effects (i.e. client effects & global effects).
	 *
	 * @param ccPlayer the player to send the packets to
	 */
	public void updateConditionalEffectVisibility(CCPlayer ccPlayer) {
		if (ccPlayer == null) return;
		if (ccPlayer.getGameSessionId() == null) return;
		if (ccPlayer.getUserToken() == null) return;

		CCEffectReport[] reports = getConditionalEffectVisibility(ccPlayer.getUserToken());
		if (reports.length == 0) return;

		ccPlayer.sendReport(reports);
	}

	/**
	 * Updates the visibility of conditional effects (i.e. client effects & global effects).
	 *
	 * @param player the player to send the packets to
	 */
	public void updateConditionalEffectVisibility(P player) {
		updateConditionalEffectVisibility(optionalCCPlayer(player).orElse(null));
	}

	public void updateConditionalEffectVisibility(UUID player) {
		updateConditionalEffectVisibility(optionalCCPlayer(player).orElse(null));
	}

	public void updateConditionalEffectVisibility() {
		if (crowdControl == null) return;
		crowdControl.getPlayers().forEach(this::updateConditionalEffectVisibility);
	}

	/**
	 * Renders messages to a player. This should be called by an event handler that listens for
	 * players joining the server.
	 *
	 * @param joiningPlayer player to send messages to
	 */
	public void onPlayerJoin(P joiningPlayer) {
		CrowdControl cc = getCrowdControl();
		if (cc == null) return;

		PlayerEntityMapper<P> mapper = playerMapper();
		UUID uuid = mapper.tryGetUniqueId(joiningPlayer).orElse(null);
		if (uuid == null) {
			getSLF4JLogger().warn("Joining player {} has no UUID", mapper.getUsername(joiningPlayer));
			return;
		}

		mapper.asAudience(joiningPlayer).sendMessage(JOIN_MESSAGE);

		if (!playerMapper().hasPermission(joiningPlayer, Plugin.USE_PERMISSION)) return;

		CCPlayer ccPlayer = cc.addPlayer(uuid);
		ccPlayer.getEventManager().registerEventConsumer(CCEventType.GENERATED_AUTH_CODE, payload -> {
			String url = ccPlayer.getAuthUrl();
			if (url == null) return; // eh?
			// ensure player is still online
			Optional<P> optPlayer = mapper.getPlayer(uuid);
			if (optPlayer.isEmpty()) return;
			P player = optPlayer.get();
			// send messages
			Audience audience = mapper.asAudience(player);
			if (ccPlayer.getUserToken() == null) {
				audience.sendMessage(translatable("cc.join.link.text.1", TextColor.color(0xF1D4FC)));
				audience.sendMessage(translatable("cc.join.link.text.2", TextColor.color(0xE8CEF2)).arguments(Component.keybind("key.chat")));
				audience.sendMessage(translatable("cc.join.link.text.3", TextColor.color(0xE1C7EB)).clickEvent(ClickEvent.copyToClipboard(ccPlayer.getAuthUrl())).hoverEvent(translatable("cc.join.link.text.3.hover")));
				audience.sendMessage(translatable("cc.join.link.text.4", TextColor.color(0xD9C1E3), TextDecoration.ITALIC));
				audience.sendMessage(translatable("cc.join.link.text.5", TextColor.color(0xD2BADB), TextDecoration.ITALIC));
				audience.sendMessage(translatable("cc.join.link.text.6", TextColor.color(0xCBB4D4)));
				audience.sendMessage(translatable("cc.join.link.text.7", TextColor.color(0xC3ADCC), TextDecoration.ITALIC));
			}
			// TODO: restore? maybe go for less of a warning angle and more of an informational angle,
			//  like hey some effects can't be used because you aren't a host / aren't a client
//			if (!globalEffectsUsable() && mapper.isAdmin(player))
//				audience.sendMessage(NO_GLOBAL_EFFECTS_MESSAGE);
		});
		ccPlayer.getEventManager().registerEventRunnable(CCEventType.AUTHENTICATED, () -> {
			if (ccPlayer.getGameSessionId() != null) return;
			if (ccPlayer.getUserToken() == null) return; // !?
			playerMapper().getPlayer(ccPlayer.getUuid()).ifPresent(p -> playerMapper().asAudience(p)
				.sendMessage(PREFIX_COMPONENT.append(translatable("cc.join.authenticated").args(text(ccPlayer.getUserToken().getName())))));
			// start session
			ccPlayer.startSession(getConditionalEffectVisibility(ccPlayer.getUserToken()));
		});
		ccPlayer.getEventManager().registerEventRunnable(CCEventType.SESSION_STARTED, () -> {
			UserToken user = ccPlayer.getUserToken();
			if (user == null) return;
			playerMapper().getPlayer(ccPlayer.getUuid()).ifPresent(p -> playerMapper().asAudience(p)
				.sendMessage(PREFIX_COMPONENT
					.append(translatable("cc.join.session"))
					.clickEvent(ClickEvent.copyToClipboard(
						String.format("https://interact.crowdcontrol.live/#/%s/%s", user.getProfile().getValue(), user.getOriginId())))));
		});
		ccPlayer.getEventManager().registerEventConsumer(CCEventType.EFFECT_REQUEST, request -> {
			getSLF4JLogger().debug("New request {}", request.getRequestId());
		});
		ccPlayer.getEventManager().registerEventConsumer(CCEventType.EFFECT_RESPONSE, response -> {
			UUID requestId = response.getRequestId();
			getSLF4JLogger().debug("Effect response {}", requestId);
			ResponseStatus status = response.getStatus();
			TrackedEffect effect = trackedEffects.get(requestId);
			if (effect == null) return;

			Command<?> command;
			try {
				command = commandRegister().getCommandByName(effect.getRequest().getEffect().getEffectId());
			} catch (IllegalArgumentException e) {
				trackedEffects.remove(requestId);
				return;
			}

			switch (status) {
				// NOTE: TIMED_BEGIN also emits SUCCESS !
				case SUCCESS:
					List<Audience> audiences = new ArrayList<>(3);

					initTo(audiences, this::getConsole);
					if (announce) {
						initTo(audiences, effect::getAudience);
						initTo(audiences, () -> playerMapper().asAudience(getPlayerManager().getSpectators().collect(Collectors.toList())));
					}

					try {
						Audience.audience(audiences).sendMessage(Component.translatable(
							"cc.effect.used",
							getViewerComponent(effect.getRequest(), true).color(Plugin.USER_COLOR),
							command.getProcessedDisplayName(effect.getRequest()).colorIfAbsent(Plugin.CMD_COLOR)
						));
					} catch (Exception e) {
						LoggerFactory.getLogger("CrowdControl/Command").warn("Failed to announce effect", e);
					}
				case FAIL_TEMPORARY:
				case FAIL_PERMANENT:
				case TIMED_END:
				case UNKNOWN:
					trackedEffects.remove(requestId);
			}
		});
	}

	/**
	 * Handles various behavior related to the departure of a player. This should be called by an
	 * event handler that listens for players leaving the server.
	 *
	 * @param player player that left
	 */
	public void onPlayerLeave(P player) {
		PlayerEntityMapper<P> mapper = playerMapper();
		UUID uuid = mapper.tryGetUniqueId(player).orElse(null);
		if (uuid == null) {
			getSLF4JLogger().warn("Departing player {} has no UUID", mapper.getUsername(player));
			return;
		}
		clientVersions.remove(uuid);
		extraFeatures.remove(uuid);

		if (crowdControl == null) return;

		crowdControl.removePlayer(uuid);
	}

	/**
	 * Gets the viewer who triggered an effect as a component, or null if names are hidden.
	 *
	 * @param request the effect request
	 * @param chat    whether the returned component will be used in chat
	 * @return the viewer as a component, or null if names are hidden
	 */
	@Nullable
	public Component getViewerComponentOrNull(@NotNull PublicEffectPayload request, boolean chat) {
		if (request.isAnonymous()) return null;
		CCUserRecord viewer = request.getRequester();
		if (viewer == null) return null;
		String name = viewer.getName();
		if (name.isEmpty()) return null;
		if ((!chat && hideNames.isHideOther()) || (chat && hideNames.isHideChat())) return null;
		return text(name);
	}

	/**
	 * Gets the viewer who triggered an effect as a component.
	 *
	 * @param request the effect request
	 * @param chat    whether the returned component will be used in chat
	 * @return the viewer as a component
	 */
	@NotNull
	public Component getViewerComponent(@NotNull PublicEffectPayload request, boolean chat) {
		return ExceptionUtil.validateNotNullElse(getViewerComponentOrNull(request, chat), VIEWER_NAME);
	}

	/**
	 * Returns the version of the mod that the provided player is using.
	 * May be empty if the player is not using the mod locally.
	 *
	 * @param player the player to check
	 * @return the version of the mod that the player is using
	 */
	public @NotNull Optional<SemVer> getModVersion(@NotNull P player) {
		return Optional.ofNullable(clientVersions.get(playerMapper().getUniqueId(player)));
	}

	/**
	 * Returns the extra features of the mod that the provided player supports.
	 *
	 * @param player the player to check
	 * @return the supported features
	 */
	public @NotNull Set<ExtraFeature> getExtraFeatures(@NotNull P player) {
		return extraFeatures.getOrDefault(playerMapper().getUniqueId(player), Collections.emptySet());
	}

	/**
	 * Returns whether the provided feature is currently available to the specified player.
	 *
	 * @param player player
	 * @return whether the feature is supported
	 */
	public boolean isFeatureAvailable(@NotNull ExtraFeature feature, @NotNull P player) {
		return getExtraFeatures(player).contains(feature);
	}

	/**
	 * Gets a Path for one of the mod's resource files.
	 *
	 * @param asset filename
	 * @return path if found
	 */
	@ApiStatus.Internal
	public @Nullable Path getPath(@NotNull String asset) {
		try {
			//noinspection DataFlowIssue
			return Paths.get(getClass().getClassLoader().getResource(asset).toURI());
		} catch (Exception ignored) {
			return null;
		}
	}

	/**
	 * Gets one of the mod's resource files.
	 *
	 * @param asset filename
	 * @return input stream if found
	 */
	@ApiStatus.Internal
	public @Nullable InputStream getInputStream(@NotNull String asset) {
		return getClass().getClassLoader().getResourceAsStream(asset);
	}

	/**
	 * Gets whether the game is currently paused.
	 *
	 * @return is game paused
	 */
	public boolean isPaused() {
		return false;
	}
}
