package dev.qixils.crowdcontrol.common;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.qixils.crowdcontrol.TrackedEffect;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.http.ModrinthVersion;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.Application;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import io.leangen.geantyref.TypeToken;
import live.crowdcontrol.cc4j.*;
import live.crowdcontrol.cc4j.websocket.UserToken;
import live.crowdcontrol.cc4j.websocket.data.CCEffectReport;
import live.crowdcontrol.cc4j.websocket.data.IdentifierType;
import live.crowdcontrol.cc4j.websocket.data.ReportStatus;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.http.CustomEffect;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectBuilder;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectDuration;
import live.crowdcontrol.cc4j.websocket.http.CustomEffectsOperation;
import live.crowdcontrol.cc4j.websocket.payload.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
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
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.SoftLockConfig.*;
import static dev.qixils.crowdcontrol.common.util.CollectionUtil.initTo;
import static dev.qixils.crowdcontrol.common.util.OptionalUtil.stream;
import static live.crowdcontrol.cc4j.websocket.ConnectedPlayer.JACKSON;
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
	 * Valid ID for custom effects.
	 */
	public static final Pattern CUSTOM_EFFECTS_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

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
	public PermissionWrapper getUsePermission() {
		return PermissionWrapper.builder()
			.node("crowdcontrol.use")
			.description("Whether a player is allowed to receive effects")
			.defaultPermission(isAdminRequired() ? PermissionWrapper.DefaultPermission.OP : PermissionWrapper.DefaultPermission.ALL)
			.build();
	}

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
	protected boolean adminRequired = false;
	@NotNull
	protected HideNames hideNames = HideNames.NONE;
	@NotNull
	protected Collection<String> hosts = Collections.emptySet();
	@NotNull
	protected CustomEffectsConfig customEffectsConfig = new CustomEffectsConfig();
	@NotNull
	protected LimitConfig limitConfig = new LimitConfig();
	@NotNull
	protected SoftLockConfig softLockConfig = new SoftLockConfig();
	@NotNull
	protected final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);
	protected final Map<UUID, SemVer> clientVersions = new HashMap<>();
	protected final Map<UUID, Set<ExtraFeature>> extraFeatures = new HashMap<>();
	protected final Map<UUID, TrackedEffect> trackedEffects = new HashMap<>();
	protected SemVer latestModVersionCached = null;
	protected Instant latestModVersionCachedAt = Instant.EPOCH;
	protected @NotNull Path defaultDataFolder = Paths.get("config", "CrowdControlData");

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
	 * Whether admin permissions are required to use the mod.
	 *
	 * @return if admin is required
	 */
	public boolean isAdminRequired() {
		return adminRequired;
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
	 * Gets the plugin's {@link CustomEffectsConfig}.
	 *
	 * @return custom effects config parsed from the plugin's config file
	 */
	@NotNull
	public CustomEffectsConfig getCustomEffectsConfig() {
		return customEffectsConfig;
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

	public @NotNull Path getDataFolder() {
		return defaultDataFolder;
	}

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
	 *
	 * @param ccPlayer the player's session to start
	 */
	public void startSession(@NotNull CCPlayer ccPlayer) {
		UserToken user = ccPlayer.getUserToken();
		if (user == null) return; // !?

		// custom effects
		Map<String, CustomEffect> newEffects;
		var effects = Optional.ofNullable(crowdControl)
			.map(CrowdControl::getGamePack)
			.map(pack -> pack.getEffects().getGame())
			.filter(game -> !game.isEmpty());
		if (effects.isPresent()) {
			List<Command<P>> registeredEffects = commandRegister().getCommands();
			Set<String> knownEffects = effects.get().keySet().stream()
				.map(str -> str.toLowerCase(Locale.ENGLISH))
				.collect(Collectors.toSet());
			newEffects = registeredEffects.stream()
				.filter(command -> command.getPriority() > Byte.MIN_VALUE)
				.filter(command -> CUSTOM_EFFECTS_ID_PATTERN.matcher(command.getEffectName().toLowerCase(Locale.ENGLISH)).matches())
				.filter(command -> !knownEffects.contains(command.getEffectName().toLowerCase(Locale.ENGLISH)))
				.filter(command -> !command.isInactive() || getCustomEffectsConfig().autogenerated())
				.sorted((a, b) -> {
					if (a.getPriority() != b.getPriority()) return b.getPriority() - a.getPriority();
					return a.getExtensionName().computeSortValue().compareToIgnoreCase(b.getExtensionName().computeSortValue());
				})
				.limit(75)
				.map(command -> {
					List<String> category = new ArrayList<>();
					category.add("Custom Effects");
					if (command.getCategories() != null) category.addAll(command.getCategories());

					return Map.entry(
						command.getEffectName().toLowerCase(Locale.ENGLISH),
						CustomEffectBuilder.builder()
							.name(command.getExtensionName())
							.price(command.getPrice())
							.description(command.getDescription())
							.category(category)
							.image(command.getImage())
							.inactive(command.isInactive())
							.build()
					);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing));
		} else {
			newEffects = Collections.emptyMap();
		}
		getSLF4JLogger().info("Registering custom effects {}", newEffects.entrySet().stream().map(entry -> entry.getKey() + ':' + entry.getValue().name().getDisplayName()).toList());
		// note: we always invoke this so we can replace an older list if one existed
		ccPlayer.setCustomEffects(Collections.singletonList(new CustomEffectsOperation("replace-all", newEffects)));

		// actually start it
		ccPlayer.startSession(getConditionalEffectVisibility(user));
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

		Builder<S> accountCmd = manager.commandBuilder("account")
			.commandDescription(description("Manage your Crowd Control account"));

		// link command
		manager.command(accountCmd.literal("link")
			.commandDescription(description("Connect your Crowd Control account"))
			.permission(PredicatePermission.of(user -> mapper.hasPermission(user, getUsePermission()))) // TODO: maybe a more effective means to do this
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.sender());
				if (crowdControl == null) {
					// TODO: custom message
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				Optional<UUID> uuidOpt = mapper.tryGetUniqueId(commandContext.sender());
				if (uuidOpt.isEmpty()) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				CCPlayer player = crowdControl.getPlayer(uuidOpt.get());
				if (player == null) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				if (player.getToken() != null) {
					audience.sendMessage(output(translatable("cc.join.account-linked", NamedTextColor.RED)));
					return;
				}
				audience.sendMessage(output(translatable("cc.join.authenticating")));
				player.regenerateAuthCode();
			})
		);

		// unlink command
		manager.command(accountCmd.literal("unlink")
			.commandDescription(description("Connect your Crowd Control account"))
			.permission(PredicatePermission.of(user -> mapper.hasPermission(user, getUsePermission()))) // TODO: maybe a more effective means to do this
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.sender());
				if (crowdControl == null) {
					// TODO: custom message
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				Optional<UUID> uuidOpt = mapper.tryGetUniqueId(commandContext.sender());
				if (uuidOpt.isEmpty()) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				CCPlayer player = crowdControl.getPlayer(uuidOpt.get());
				if (player == null) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				if (player.getToken() == null) {
					audience.sendMessage(output(translatable("cc.join.account-unlinked", NamedTextColor.RED)));
					return;
				}
				audience.sendMessage(output(translatable("cc.join.unauthenticating")));
				player.stopSession().handle(($1, $2) -> {
					player.clearToken();
					audience.sendMessage(output(translatable("cc.join.unauthenticated")));
					return $1;
				});
			})
		);

		//// Session Command ////

		Builder<S> sessionCmd = manager.commandBuilder("session")
			.commandDescription(description("Manage your Crowd Control session"));

		// start command
		manager.command(sessionCmd.literal("start")
			.commandDescription(description("Starts your Crowd Control session"))
			.permission(PredicatePermission.of(user -> mapper.hasPermission(user, getUsePermission()))) // TODO: maybe a more effective means to do this
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.sender());
				if (crowdControl == null) {
					// TODO: custom message
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				Optional<UUID> uuidOpt = mapper.tryGetUniqueId(commandContext.sender());
				if (uuidOpt.isEmpty()) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				CCPlayer player = crowdControl.getPlayer(uuidOpt.get());
				if (player == null) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				UserToken token = player.getUserToken();
				if (token == null) {
					audience.sendMessage(output(translatable("cc.join.needs-authentication", NamedTextColor.RED)));
					return;
				}
				if (player.getGameSessionId() != null) {
					audience.sendMessage(output(translatable("cc.join.session-active", NamedTextColor.RED)));
					return;
				}
				audience.sendMessage(output(translatable("cc.join.starting")));
				startSession(player);
			})
		);

		// stop command
		manager.command(sessionCmd.literal("stop")
			.commandDescription(description("Stop your Crowd Control session"))
			.permission(PredicatePermission.of(user -> mapper.hasPermission(user, getUsePermission()))) // TODO: maybe a more effective means to do this
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.sender());
				if (crowdControl == null) {
					// TODO: custom message
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				Optional<UUID> uuidOpt = mapper.tryGetUniqueId(commandContext.sender());
				if (uuidOpt.isEmpty()) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				CCPlayer player = crowdControl.getPlayer(uuidOpt.get());
				if (player == null) {
					audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
					return;
				}
				UserToken token = player.getUserToken();
				if (token == null) {
					audience.sendMessage(output(translatable("cc.join.needs-authentication", NamedTextColor.RED)));
					return;
				}
				if (player.getGameSessionId() == null) {
					audience.sendMessage(output(translatable("cc.join.session-inactive", NamedTextColor.RED)));
					return;
				}
				player.stopSession();
				audience.sendMessage(output(translatable("cc.join.stopped")));
			})
		);

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
		// reloadconfig command
		manager.command(ccCmd.literal("reloadconfig")
			.commandDescription(description("Reloads the mod's config file"))
			.handler(ctx -> {
				Audience audience = mapper.asAudience(ctx.sender());
				loadConfig();
				audience.sendMessage(output(translatable("cc.command.crowdcontrol.reloadconfig.output")));
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
								new CustomEffectDuration(10)
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

	protected abstract ConfigurationLoader<?> getConfigLoader() throws IllegalStateException;

	/**
	 * Loads the configuration file.
	 */
	public void loadConfig() {
		ConfigurationNode config;
		try {
			config = getConfigLoader().load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		// soft-lock observer
		softLockConfig = new SoftLockConfig(
			config.node("soft-lock-observer", "period").getInt(DEF_PERIOD),
			config.node("soft-lock-observer", "deaths").getInt(DEF_DEATHS),
			config.node("soft-lock-observer", "search-horizontal").getInt(DEF_SEARCH_HORIZ),
			config.node("soft-lock-observer", "search-vertical").getInt(DEF_SEARCH_VERT)
		);

		// custom effects
		try {
			customEffectsConfig = config.node("custom-effects").get(CustomEffectsConfig.class, new CustomEffectsConfig());
		} catch (Exception e) {
			getSLF4JLogger().warn("!!!!!!!!!!!!!! Failed to load custom effects config", e);
		}

		// hosts
		TypeToken<Set<String>> hostToken = new TypeToken<>() {};
		try {
			hosts = Collections.unmodifiableSet(config.node("hosts").get(hostToken, new HashSet<>(hosts)));
		} catch (SerializationException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}

		// limit config
		boolean hostsBypass = config.node("limits", "hosts-bypass").getBoolean(limitConfig.hostsBypass());
		TypeToken<Map<String, Integer>> limitToken = new TypeToken<>() {};
		try {
			Map<String, Integer> itemLimits = config.node("limits", "items").get(limitToken, limitConfig.itemLimits());
			Map<String, Integer> entityLimits = config.node("limits", "entities").get(limitToken, limitConfig.entityLimits());
			limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);
		} catch (SerializationException e) {
			getSLF4JLogger().warn("Could not parse limits config", e);
		}

		// misc
		global = config.node("global").getBoolean(global);
		announce = config.node("announce").getBoolean(announce);
		adminRequired = config.node("admin-required").getBoolean(adminRequired);
		hideNames = HideNames.fromConfigCode(config.node("hide-names").getString(hideNames.getConfigCode()));
	}

	public void saveConfig() {
		try {
			// TODO: add comments
			// TODO: custom
			ConfigurationNode config = getConfigLoader().createNode();
			TypeToken<Set<String>> hostToken = new TypeToken<>() {};
			TypeToken<Map<String, Integer>> limitToken = new TypeToken<>() {};
			config.node("hosts").set(hostToken, new HashSet<>(hosts));
			config.node("limits", "hosts-bypass").set(limitConfig.hostsBypass());
			config.node("limits", "items").set(limitToken, limitConfig.itemLimits());
			config.node("limits", "entities").set(limitToken, limitConfig.entityLimits());
			config.node("global").set(global);
			config.node("announce").set(announce);
			config.node("hide-names").set(hideNames.getConfigCode());
			config.node("soft-lock-observer", "period").set(softLockConfig.getPeriod());
			config.node("soft-lock-observer", "deaths").set(softLockConfig.getDeaths());
			config.node("soft-lock-observer", "search-horizontal").set(softLockConfig.getSearchH());
			config.node("soft-lock-observer", "search-vertical").set(softLockConfig.getSearchV());
			config.node("custom-effects", "autogenerated").set(customEffectsConfig.autogenerated());
			getConfigLoader().save(config);
		} catch (ConfigurateException e) {
			throw new RuntimeException("Could not save plugin config", e);
		}
	}

	protected @NotNull Path fixConfigDirectory(@NotNull Path configDirectory) {
		if (configDirectory.getFileName().toString().equals("crowdcontrol.conf"))
			configDirectory = configDirectory.getParent();
		return configDirectory;
	}

	/**
	 * Creates a config loader given the directory in which plugin config files are stored.
	 *
	 * @param configDirectory path in which plugin config files are stored
	 * @return the loader for a config file
	 * @throws IllegalStateException copying the default config file failed
	 */
	@CheckReturnValue
	protected <T extends AbstractConfigurationLoader<@NotNull CommentedConfigurationNode>> T createConfigLoader(@NotNull Path configDirectory,
																												@NotNull String filename,
																												@NotNull AbstractConfigurationLoader.Builder<?, @NotNull T> builder) throws IllegalStateException {
		configDirectory = fixConfigDirectory(configDirectory);
		defaultDataFolder = configDirectory.resolve("CrowdControlData");

		if (!Files.exists(configDirectory)) {
			try {
				Files.createDirectories(configDirectory);
			} catch (Exception e) {
				throw new IllegalStateException("Could not create config directory", e);
			}
		}

		// move old config
		Path configPath = configDirectory.resolve(filename);
		if (filename.equals("crowdcontrol.conf")) {
			Path oldConfigPath = configDirectory.resolve("crowd-control.conf");
			if (Files.exists(oldConfigPath)) {
				try {
					Files.move(oldConfigPath, configPath);
				} catch (Exception e) {
					getSLF4JLogger().warn("Could not move old config file to new location", e);
				}
			}
		}

		if (!Files.exists(configPath)) {
			// read the default config
			String defaultFile = filename.endsWith(".conf") ? "assets/crowdcontrol/default.conf" : "config.yml";
			InputStream inputStream = getInputStream(defaultFile);
			if (inputStream == null)
				throw new IllegalStateException("Could not find default config file");
			// copy the default config to the config path
			try {
				Files.copy(inputStream, configPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not copy default config file to " + configPath, e);
			}
		}

		return builder
			.defaultOptions(opts -> opts.serializers(build -> build.registerAll(ConfigurateComponentSerializer.configurate().serializers())))
			.path(configPath)
			.build();
	}

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
					}
					if (visibility != TriState.FALSE && (minVersion = effect.getMinimumModVersion()).isGreaterThan(SemVer.ZERO)) {
						visibility = TriState.fromBoolean(clientVersion.isAtLeast(minVersion));
						getSLF4JLogger().debug("Client {} version is at least effect {} version {}? {}", clientVersion, effect.getEffectName(), minVersion, visibility);
					}
					if (visibility != TriState.FALSE && effect.isGlobal()) {
						visibility = TriState.fromBoolean(globalVisible);
					}
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
			if (entry.getKey() == ReportStatus.MENU_HIDDEN || entry.getKey() == ReportStatus.MENU_UNAVAILABLE) {
				getSLF4JLogger().debug("{} effects: {}", entry.getKey(), entry.getValue());
			}
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
		getSLF4JLogger().debug("Updating effect visibility for player {}...", ccPlayer);
		if (ccPlayer == null) return;
		getSLF4JLogger().debug("...with session {} & token {}...", ccPlayer.getGameSessionId(), ccPlayer.getUserToken());
		if (ccPlayer.getGameSessionId() == null) return;
		if (ccPlayer.getUserToken() == null) return;

		CCEffectReport[] reports = getConditionalEffectVisibility(ccPlayer.getUserToken());
		getSLF4JLogger().debug("...with {} reports", reports.length);
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

		Audience audience = mapper.asAudience(joiningPlayer);
		audience.sendMessage(JOIN_MESSAGE);

		if (!playerMapper().hasPermission(joiningPlayer, getUsePermission())) return;

		if (playerMapper().hasPermission(joiningPlayer, Plugin.ADMIN_PERMISSION)) {
			checkModVersion().thenAccept(latestModVersion -> {
				if (latestModVersion == null) return;
				if (!SemVer.MOD.isLessThan(latestModVersion)) return;
				audience.sendMessage(Component.translatable("cc.join.outdated").decorate(TextDecoration.ITALIC).color(JOIN_MESSAGE_COLOR).clickEvent(ClickEvent.openUrl("https://modrinth.com/mod/crowdcontrol")));
			});
		}

		CCPlayer ccPlayer = cc.addPlayer(uuid);
		ccPlayer.getEventManager().registerEventConsumer(CCEventType.MESSAGE, message -> {
			Optional<P> newPlayer = playerMapper().getPlayer(ccPlayer.getUuid());
			if (newPlayer.isEmpty()) return;
			Audience newAudience = playerMapper().asAudience(newPlayer.get());
			Component text = PREFIX_COMPONENT.append(Component.text(message.message()));
			if (message.level() == CCMessage.Level.ERROR) text = text.color(NamedTextColor.RED);
			else if (message.level() == CCMessage.Level.WARN) text = text.color(NamedTextColor.GOLD);
			newAudience.sendMessage(text);
		});
		ccPlayer.getEventManager().registerEventConsumer(CCEventType.GENERATED_AUTH_CODE, payload -> {
			String url = ccPlayer.getAuthUrl();
			if (url == null) return; // eh?
			// ensure player is still online
//			Optional<P> optPlayer = mapper.getPlayer(uuid);
//			if (optPlayer.isEmpty()) return;
//			P player = optPlayer.get();
			// send messages
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
			UserToken user = ccPlayer.getUserToken();
			if (user == null) return; // !?
			playerMapper().getPlayer(ccPlayer.getUuid()).ifPresent(p -> playerMapper().asAudience(p)
				.sendMessage(PREFIX_COMPONENT.append(translatable("cc.join.authenticated").args(text(user.getName())))));
			// start session
			getScheduledExecutor().schedule(
				() -> startSession(ccPlayer),
				3,
				TimeUnit.SECONDS
			);
		});
		ccPlayer.getEventManager().registerEventRunnable(CCEventType.SESSION_STARTED, () -> {
			updateConditionalEffectVisibility(ccPlayer); // just in case anything changed between then and now
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

	@NotNull
	public CompletableFuture<@Nullable SemVer> checkModVersion() {
		// TODO: cleanup

		if (Duration.between(latestModVersionCachedAt, Instant.now()).getSeconds() < Duration.ofHours(1).getSeconds())
			return CompletableFuture.completedFuture(latestModVersionCached);

		return CompletableFuture.supplyAsync(() -> {
			HttpURLConnection con = null;
			try {
				var mcVersionMetadata = getVersionMetadata();
				String loader = URLEncoder.encode("[\"" + mcVersionMetadata.getModLoaderExpected().toLowerCase(Locale.US) + "\"]", StandardCharsets.UTF_8);
				String version = URLEncoder.encode("[\"" + mcVersionMetadata.getMinecraftVersion() + "\"]", StandardCharsets.UTF_8);
				URL url = new URL("https://api.modrinth.com/v2/project/crowdcontrol/version?loaders=" + loader + "&game_versions=" + version);
				con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				con.setRequestProperty("User-Agent", "crowdcontrol4j");
				con.setRequestProperty("Content-Type", "application/json");
				con.setConnectTimeout(10000);
				con.setReadTimeout(10000);

				List<ModrinthVersion> versions = JACKSON.readValue(con.getInputStream(), new TypeReference<>() {
				});
				if (con.getResponseCode() != 200)
					throw new IllegalStateException("Server returned code " + con.getResponseCode());
				if (versions.isEmpty()) return null;
				String latestVersion = versions.getFirst().getVersionNumber();
				if (latestVersion == null) return null;
				latestModVersionCachedAt = Instant.now();
				latestModVersionCached = new SemVer(latestVersion);
				getSLF4JLogger().info("Latest mod version is {}", latestModVersionCached.toString());
				return latestModVersionCached;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (con != null) con.disconnect();
			}
		}).handle((latestModVersion, e) -> {
			if (e != null) getSLF4JLogger().warn("Failed to fetch latest version", e);
			return latestModVersion;
		});
	}
}
