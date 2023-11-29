package dev.qixils.crowdcontrol.common;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Respondable;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.PacketType;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import dev.qixils.crowdcontrol.socket.SocketManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * The main class used by a Crowd Control implementation which defines numerous methods for
 * managing Crowd Control server/client connections and handling {@link Command}s.
 *
 * @param <P> class used to represent online players
 * @param <S> class used to represent command senders in Cloud Command Framework
 */
public interface Plugin<P, S> {

	/**
	 * The color to use for basic messages rendered to players when joining the server.
	 */
	TextColor JOIN_MESSAGE_COLOR = TextColor.color(0xFCE9D4);

	/**
	 * Text color to use for usernames.
	 */
	TextColor USER_COLOR = TextColor.color(0x9f44db);

	/**
	 * Text color to use for command names.
	 */
	TextColor CMD_COLOR = TextColor.color(0xb15be3);

	/**
	 * A less saturated version of {@link #CMD_COLOR}.
	 */
	TextColor DIM_CMD_COLOR = TextColor.color(0xA982C2);

	/**
	 * The color used for displaying error messages on join.
	 */
	TextColor _ERROR_COLOR = TextColor.color(0xF78080);

	/**
	 * The prefix to use in command output.
	 */
	String PREFIX = "CrowdControl";

	/**
	 * The prefix to use in command output as a {@link Component}.
	 */
	Component PREFIX_COMPONENT = text()
		.append(text('[', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
		.append(text(PREFIX, NamedTextColor.YELLOW))
		.append(text(']', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
		.appendSpace()
		.build();

	/**
	 * The default name of a viewer.
	 */
	Component VIEWER_NAME = translatable("cc.effect.viewer");

	/**
	 * The permission node required to use administrative commands.
	 */
	String ADMIN_PERMISSION = "crowdcontrol.admin";

	/**
	 * Port that the {@link CrowdControl} service connects to or listen on.
	 */
	int DEFAULT_PORT = 58431;

	/**
	 * Default password that clients must enter to connect to the {@link CrowdControl} service.
	 */
	String DEFAULT_PASSWORD = "crowdcontrol";

	/**
	 * Formats the provided text as an error message.
	 *
	 * @param text text to format
	 * @return formatted text
	 */
	static @NotNull Component error(@NotNull Component text) {
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
	static @NotNull Component warning(@NotNull Component text) {
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
	static @NotNull Component output(@NotNull Component text) {
		return PREFIX_COMPONENT.append(text);
	}

	/**
	 * The message to send to a player when they join the server.
	 */
	Component JOIN_MESSAGE_1 = translatable(
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
	 * A warning message sent to players when they join the server if they have no stream account linked.
	 */
	Component JOIN_MESSAGE_2 = translatable(
		"cc.join.link.text",
		TextColor.color(0xF1D4FC)
	)
		.hoverEvent(translatable("cc.join.link.hover"))
		.clickEvent(ClickEvent.suggestCommand("/account link "));

	/**
	 * A warning message sent to players when they join the server if global effects are
	 * completely unavailable.
	 */
	Component NO_GLOBAL_EFFECTS_MESSAGE = warning(translatable(
		"cc.error.no-global-effects",
		text("global", TextColor.color(0xF9AD9E)),
		text("true", TextColor.color(0xF9AD9E)),
		text("hosts", TextColor.color(0xF9AD9E))
	));

	/**
	 * Error message displayed to non-admins when the service is not enabled.
	 */
	Component NO_CC_USER_ERROR = translatable(
		"cc.error.prefix.critical",
		NamedTextColor.RED,
		translatable(
			"cc.error.user-error",
			_ERROR_COLOR
		)
	);

	/**
	 * Error message displayed to admins when the password is not set.
	 */
	Component NO_CC_OP_ERROR_NO_PASSWORD = warning(translatable(
			"cc.error.no-password.text",
			text("/password <password>", NamedTextColor.GOLD),
			text("/crowdcontrol reconnect", NamedTextColor.GOLD)
		)
			.clickEvent(ClickEvent.suggestCommand("/password "))
			.hoverEvent(translatable("cc.error.no-password.hover"))
	);

	/**
	 * Error message displayed to admins when the error is unknown.
	 */
	Component NO_CC_UNKNOWN_ERROR = error(translatable("cc.error.unknown"));

	/**
	 * Gets the provided command sender as a player.
	 *
	 * @param sender the command sender
	 * @return the player, or null if the sender is not a player
	 */
	default @Nullable P asPlayer(@NotNull S sender) {
		try {
			Class<P> playerClass = getPlayerClass();
			if (!playerClass.isInstance(sender))
				return null;
			return playerClass.cast(sender);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Registers the plugin's basic chat commands.
	 */
	default void registerChatCommands() {
		try {
			GlobalTranslator.translator().addSource(new KyoriTranslator("crowdcontrol", "i18n/CrowdControl", getClass(), Locale.US));
		} catch (Exception e) {
			getSLF4JLogger().error("Failed to initialize i18n", e);
		}

		CommandManager<S> manager = getCommandManager();
		if (manager == null)
			throw new IllegalStateException("CommandManager is null");
		EntityMapper<S> mapper = commandSenderMapper();

		// TODO: support i18n in cloud command descriptions

		//// Account Command ////

		// base command
		Builder<S> account = manager.commandBuilder("account")
			.meta(CommandMeta.DESCRIPTION, "Manage your connected stream account(s)")
			.permission(sender -> !isAdminRequired() || mapper.isAdmin(sender));

		// link command
		manager.command(account.literal("link")
			.meta(CommandMeta.DESCRIPTION, "Link a stream account to your Minecraft account")
			.argument(
				StringArgument.<S>builder("username")
					.single()
					.asRequired()
					.manager(manager)
					.build(),
				ArgumentDescription.of("The username of the stream account to link")
			)
			.handler(commandContext -> {
				String username = commandContext.get("username");
				S sender = commandContext.getSender();
				Audience audience = mapper.asAudience(sender);
				UUID uuid = mapper.tryGetUniqueId(sender).orElseThrow(() ->
					new IllegalArgumentException("Your UUID cannot be found. Please ensure you are running this command in-game."));
				if (getPlayerManager().linkPlayer(uuid, username)) {
					updateConditionalEffectVisibility(getCrowdControl());
					P player = asPlayer(sender);
					if (player != null)
						sendPlayerEvent(player, "playerJoined");
					audience.sendMessage(output(translatable(
						"cc.command.account.link.output",
						text(username, NamedTextColor.AQUA)
					)));
				} else {
					audience.sendMessage(output(translatable(
						"cc.command.account.link.error",
						NamedTextColor.RED,
						text(username, NamedTextColor.AQUA)
					)));
				}
			}));
		// unlink command
		manager.command(account.literal("unlink")
			.meta(CommandMeta.DESCRIPTION, "Unlink a stream account from your Minecraft account")
			.argument(
				StringArgument.<S>builder("username")
					.single()
					.asRequired()
					.manager(manager)
					.withSuggestionsProvider((ctx, input) -> {
						Optional<UUID> uuid = mapper.tryGetUniqueId(ctx.getSender());
						if (!uuid.isPresent()) return Collections.emptyList();
						Collection<String> linkedAccounts = getPlayerManager().getLinkedAccounts(uuid.get());
						if (linkedAccounts.isEmpty()) return Collections.emptyList();
						String lowerInput = input.toLowerCase(Locale.ENGLISH);
						Set<String> suggestions = new LinkedHashSet<>();
						for (String acc : linkedAccounts) {
							if (acc.startsWith(lowerInput))
								suggestions.add(acc);
						}
						for (String acc : linkedAccounts) {
							if (acc.contains(lowerInput))
								suggestions.add(acc);
						}
						return new ArrayList<>(suggestions);
					})
					.build(),
				ArgumentDescription.of("The username of the stream account to unlink")
			)
			.handler(commandContext -> {
				String username = commandContext.get("username");
				S sender = commandContext.getSender();
				Audience audience = mapper.asAudience(sender);
				UUID uuid = mapper.tryGetUniqueId(sender).orElseThrow(() ->
					new IllegalArgumentException("Your UUID cannot be found. Please ensure you are running this command in-game."));
				if (getPlayerManager().unlinkPlayer(uuid, username)) {
					updateConditionalEffectVisibility(getCrowdControl());
					audience.sendMessage(output(translatable(
						"cc.command.account.unlink.output",
						text(username, NamedTextColor.AQUA)
					)));
				} else {
					audience.sendMessage(output(translatable(
						"cc.command.account.unlink.error",
						NamedTextColor.RED,
						text(username, NamedTextColor.AQUA)
					)));
				}
			}));

		//// CrowdControl Command ////

		// base command
		Builder<S> ccCmd = manager.commandBuilder("crowdcontrol")
			.meta(CommandMeta.DESCRIPTION, "View information about and manage the Crowd Control service");

		// connect command
		manager.command(ccCmd.literal("connect")
			.meta(CommandMeta.DESCRIPTION, "Connect to the Crowd Control service")
			.permission(mapper::isAdmin)
			.handler(commandContext -> {
				Audience sender = mapper.asAudience(commandContext.getSender());
				if (getCrowdControl() != null)
					sender.sendMessage(output(translatable("cc.command.crowdcontrol.connect.error", NamedTextColor.RED)));
				else {
					initCrowdControl();
					sender.sendMessage(output(translatable("cc.command.crowdcontrol.connect.output")));
				}
			}));
		// disconnect command
		manager.command(ccCmd.literal("disconnect")
			.meta(CommandMeta.DESCRIPTION, "Disconnect from the Crowd Control service")
			.permission(mapper::isAdmin)
			.handler(commandContext -> {
				Audience sender = mapper.asAudience(commandContext.getSender());
				if (getCrowdControl() == null)
					sender.sendMessage(output(translatable("cc.command.crowdcontrol.disconnect.error", NamedTextColor.RED)));
				else {
					getCrowdControl().shutdown("Disconnected issued by server administrator");
					updateCrowdControl(null);
					sender.sendMessage(output(translatable("cc.command.crowdcontrol.disconnect.output")));
				}
			}));
		// reconnect command
		manager.command(ccCmd.literal("reconnect")
			.meta(CommandMeta.DESCRIPTION, "Reconnect to the Crowd Control service")
			.permission(mapper::isAdmin)
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.getSender());
				CrowdControl cc = getCrowdControl();
				if (cc != null)
					cc.shutdown("Reconnect issued by server administrator");
				initCrowdControl();

				audience.sendMessage(output(translatable("cc.command.crowdcontrol.reconnect.output")));
			}));
		// status command
		manager.command(ccCmd.literal("status")
			.meta(CommandMeta.DESCRIPTION, "Get the status of the Crowd Control service")
			.permission(mapper::isAdmin)
			.handler(commandContext -> {
				Audience audience = mapper.asAudience(commandContext.getSender());
				CrowdControl cc = getCrowdControl();
				if (cc == null) {
					audience.sendMessage(output(translatable("cc.command.crowdcontrol.status.offline")));
					return;
				}
				TextComponent.Builder msg = text()
					.append(PREFIX_COMPONENT)
					.append(translatable("cc.command.crowdcontrol.status.online"))
					.appendSpace();
				Set<Request.Source> sources = cc.getSources();
				if (sources.isEmpty())
					msg.append(translatable("cc.command.crowdcontrol.status.sources.none"));
				else {
					msg.append(translatable("cc.command.crowdcontrol.status.sources.header"));
					Component unknown = translatable("cc.command.crowdcontrol.status.sources.unknown", NamedTextColor.GRAY);
					for (Request.Source source : sources) {
						Component ip = ofNullable(source.ip()).map(InetAddress::toString).<Component>map(Component::text).orElse(unknown);
						Component login = ofNullable(source.login()).<Component>map(Component::text).orElse(unknown);
						Component service = ofNullable(source.target()).map(Request.Target::getService).<Component>map(Component::text).orElse(unknown);
						Component name = ofNullable(source.target()).map(Request.Target::getName).<Component>map(Component::text).orElse(unknown);
						msg.appendNewline().append(translatable("cc.command.crowdcontrol.status.sources.entry", ip, login, service, name));
					}
				}
				audience.sendMessage(msg);
			}));
		// version command
		manager.command(ccCmd.literal("version")
			.meta(CommandMeta.DESCRIPTION, "Get the version of the server's and players' Crowd Control mod")
			.handler(ctx -> {
				Audience audience = mapper.asAudience(ctx.getSender());
				Component message = output(translatable("cc.command.crowdcontrol.version.server", text(SemVer.MOD.toString())));
				if (canGetModVersion()) {
					audience.sendMessage(message.appendSpace().append(translatable("cc.command.crowdcontrol.version.clients.header")));
					for (P player : getAllPlayers()) { // TODO: get all players real
						Optional<SemVer> version = getModVersion(player);
						if (version.isPresent()) {
							String key = "cc.command.crowdcontrol.version.client." + (version.get().equals(SemVer.MOD) ? "match" : "mismatch");
							audience.sendMessage(translatable(key, text(playerMapper().getUsername(player)), text(version.get().toString())));
						} else
							audience.sendMessage(translatable("cc.command.crowdcontrol.version.client.unknown", text(playerMapper().getUsername(player))));
					}
				} else {
					audience.sendMessage(message.appendSpace().append(translatable("cc.command.crowdcontrol.version.clients.unknown")));
				}
			}));
		// execute command
		if (SemVer.MOD.isSnapshot()) { // TODO: make command generally available
			manager.command(ccCmd.literal("execute")
				.meta(CommandMeta.DESCRIPTION, "Executes the effect with the given ID")
				.permission(mapper::isAdmin)
				.argument(
					StringArgument.<S>builder("effect")
						.single()
						.asRequired()
						.manager(manager)
						.withSuggestionsProvider((ctx, input) -> {
							List<Command<P>> effects = commandRegister().getCommands();
							String lowerInput = input.toLowerCase(Locale.ENGLISH);
							Set<String> suggestions = new LinkedHashSet<>();
							for (Command<P> effect : effects) {
								String effectName = effect.getEffectName().toLowerCase(Locale.ENGLISH);
								if (effectName.startsWith(lowerInput))
									suggestions.add(effectName);
							}
							for (Command<P> effect : effects) {
								String effectName = effect.getEffectName().toLowerCase(Locale.ENGLISH);
								if (effectName.contains(lowerInput))
									suggestions.add(effectName);
							}
							return new ArrayList<>(suggestions);
						})
						.build(),
					ArgumentDescription.of("The username of the stream account to unlink")
				)
				.handler(commandContext -> {
					// TODO: allow targeting multiple players
					S sender = commandContext.getSender();
					Audience audience = mapper.asAudience(sender);
					P player = asPlayer(sender);
					if (player == null) {
						audience.sendMessage(output(translatable("cc.command.cast-error", NamedTextColor.RED)));
						return;
					}
					Command<P> effect = commandRegister().getCommandByName(commandContext.get("effect"));
					@SuppressWarnings("ArraysAsListWithZeroOrOneArgument") // need mutable list
					List<P> players = Arrays.asList(player);
					effect.execute(players, new Request.Builder().id(1).effect(effect.getEffectName()).viewer(playerMapper().getUsername(player)).build());
				})
			);
		}

		//// Password Command ////
		manager.command(manager.commandBuilder("password")
			.meta(CommandMeta.DESCRIPTION, "Sets the password required for Crowd Control clients to connect to the server")
			.permission(mapper::isAdmin)
			.argument(StringArgument.<S>builder("password").greedy().asRequired())
			.handler(commandContext -> {
				Audience sender = mapper.asAudience(commandContext.getSender());
				String password = commandContext.get("password");
				setPassword(password);
				sender.sendMessage(output(translatable(
						"cc.command.password.output",
						text("/crowdcontrol reconnect", NamedTextColor.YELLOW)
					)
						.hoverEvent(translatable(
							"cc.command.password.output.hover",
							text("/crowdcontrol reconnect", NamedTextColor.YELLOW)
						))
						.clickEvent(ClickEvent.runCommand("/crowdcontrol reconnect"))
				));
			})
		);

		new MinecraftExceptionHandler<S>()
			.withDefaultHandlers()
			.withDecorator(component -> output(component).color(NamedTextColor.RED))
			.apply(manager, mapper::asAudience);
	}

	/**
	 * Gets the {@link EntityMapper} for this implementation's player object.
	 *
	 * @return player entity mapper
	 */
	PlayerEntityMapper<P> playerMapper();

	/**
	 * Gets the {@link EntityMapper} for this implementation's command sender object.
	 *
	 * @return command sender mapper
	 */
	EntityMapper<S> commandSenderMapper();

	/**
	 * Gets the player class utilized by this implementation.
	 *
	 * @return player class
	 */
	@NotNull
	@CheckReturnValue
	Class<P> getPlayerClass();

	/**
	 * Gets the command sender class utilized by this implementation.
	 *
	 * @return command sender class
	 */
	@NotNull
	@CheckReturnValue
	Class<S> getCommandSenderClass();

	/**
	 * Gets the object that maps {@link Request}s to the players it should affect.
	 *
	 * @return mapper object
	 */
	@NotNull
	@CheckReturnValue
	PlayerManager<P> getPlayerManager();

	/**
	 * Fetches all online players that should be affected by the provided {@link Request}.
	 *
	 * @param request the request to be processed
	 * @return a list of online players
	 */
	@CheckReturnValue
	default @NotNull List<P> getPlayers(@NotNull Request request) {
		return getPlayerManager().getPlayers(request);
	}

	/**
	 * Fetches all online players that should be affected by global requests.
	 *
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	default List<@NotNull P> getAllPlayers() {
		return getPlayerManager().getAllPlayers();
	}

	/**
	 * Loads the configuration file.
	 */
	void loadConfig();

	/**
	 * Fetches the config variable which determines if all requests should be treated as global.
	 *
	 * @return true if all requests should be treated as global
	 */
	@CheckReturnValue
	boolean isGlobal();

	/**
	 * Determines whether it's possible for global effects to execute.
	 *
	 * @return true if global effects could execute
	 */
	default boolean globalEffectsUsable() {
		return isGlobal() || (!getHosts().isEmpty() && getAllPlayers().stream().anyMatch(this::isHost));
	}

	/**
	 * Determines if a request should apply to all online players or only a select few.
	 *
	 * @param request the request to be processed
	 * @return true if the request should apply to all online players
	 */
	@CheckReturnValue
	default boolean isGlobal(@NotNull Request request) {
		return isGlobal() || request.isGlobal();
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
	@NotNull Collection<String> getHosts();

	/**
	 * Whether the specified player is known to be a server host.
	 *
	 * @param player player to check
	 * @return whether the player is a server host
	 */
	default boolean isHost(@NotNull P player) {
		Collection<String> hosts = getHosts();
		getSLF4JLogger().debug("Checking if {} matches a host known in {}", playerMapper().getUsername(player), hosts);
		if (hosts.isEmpty())
			return false;

		PlayerEntityMapper<P> mapper = playerMapper();
		Optional<UUID> uuid = mapper.tryGetUniqueId(player);
		if (uuid.isPresent()) {
			String uuidStr = uuid.get().toString().toLowerCase(Locale.ENGLISH);
			getSLF4JLogger().debug("Checking for UUID {}", uuidStr);
			if (hosts.contains(uuidStr) || hosts.contains(uuidStr.replace("-", "")))
				return true;
		}

		String username = mapper.getUsername(player).toLowerCase(Locale.ENGLISH);
		getSLF4JLogger().debug("Checking for username {}", username);
		if (hosts.contains(username))
			return true;

		if (uuid.isPresent()) {
			getSLF4JLogger().debug("Checking accounts linked to player");
			PlayerManager<P> manager = getPlayerManager();
			return manager.getLinkedAccounts(uuid.get()).stream().anyMatch(hosts::contains);
		}

		getSLF4JLogger().debug("No matches found");
		return false;
	}

	/**
	 * Determines whether a player must be an {@link EntityMapper#isAdmin(Object) admin} to use the
	 * {@code /account} command.
	 *
	 * @return true if the player must be an admin to use the /account command
	 */
	@CheckReturnValue
	boolean isAdminRequired();

	/**
	 * Whether to announce the execution of effects in chat.
	 *
	 * @return true if the plugin should announce the execution of effects in chat
	 */
	@CheckReturnValue
	boolean announceEffects();

	/**
	 * Sets whether to announce the execution of effects in chat.
	 *
	 * @param announceEffects true if the plugin should announce the execution of effects in chat
	 */
	void setAnnounceEffects(boolean announceEffects);

	/**
	 * Returns the plugin's text utility class.
	 */
	@CheckReturnValue
	@NotNull TextUtil getTextUtil();

	/**
	 * Registers a {@link Command} with the plugin.
	 *
	 * @param name    the name of the command
	 * @param command the command to register
	 */
	void registerCommand(@NotNull String name, @NotNull Command<P> command);

	/**
	 * Returns the object that manages the registering of effects/commands.
	 * Not to be confused with the {@link #getCommandManager() chat command manager}.
	 *
	 * @return command registry manager
	 */
	@NotNull
	AbstractCommandRegister<P, ?> commandRegister();

	/**
	 * Gets the {@link ScheduledExecutorService} used by the plugin.
	 *
	 * @return the executor service
	 */
	@NotNull
	ScheduledExecutorService getScheduledExecutor();

	/**
	 * Gets the {@link CrowdControl} instance.
	 *
	 * @return crowd control instance
	 */
	@Nullable
	@CheckReturnValue
	CrowdControl getCrowdControl();

	/**
	 * (Re)initializes the {@link CrowdControl} instance.
	 */
	void initCrowdControl();

	/**
	 * Sends a packet to trigger a remote function on the Crowd Control service.
	 *
	 * @param service the service to send the packet to
	 * @param method  the name of the remote function to call
	 * @param args    the arguments to pass to the remote function
	 */
	default void sendEmbeddedMessagePacket(@Nullable SocketManager service, @NotNull String method, @Nullable Object @Nullable ... args) {
		if (service == null)
			service = getCrowdControl();
		if (service == null) {
			getSLF4JLogger().warn("Attempted to send embedded message packet but the service is unavailable");
			return;
		}
		try {
			getSLF4JLogger().debug("sending packet {} {} to {}", method, Arrays.toString(args), service);
			Response response = service.buildResponse()
				.packetType(PacketType.REMOTE_FUNCTION)
				.method(method)
				.addArguments(args)
				.build();
			getSLF4JLogger().debug("final packet: {}", response.toJSON());
			response.send();
		} catch (Exception e) {
			getSLF4JLogger().error("Failed to send embedded message packet", e);
		}
	}

	/**
	 * Sends a packet to trigger a remote function on the Crowd Control service.
	 *
	 * @param method the name of the remote function to call
	 * @param args   the arguments to pass to the remote function
	 */
	default void sendEmbeddedMessagePacket(@NotNull String method, @Nullable Object @Nullable ... args) {
		sendEmbeddedMessagePacket(null, method, args);
	}

	/**
	 * Gets a map of event types to the {@link SocketManager}s that have received the event.
	 *
	 * @return the map of event types to {@link SocketManager}s
	 */
	Map<String, List<SocketManager>> getSentEvents();

	/**
	 * Sends a player event packet.
	 *
	 * @param service   the service to send the packet to
	 * @param eventType the type of event to send
	 * @param force     whether to send the event even if the player is not necessarily connected
	 */
	default void sendPlayerEvent(@Nullable SocketManager service, @NotNull String eventType, boolean force) {
		if (service == null) {
			getSLF4JLogger().warn("Attempted to send player event packet but the service is unavailable");
			return;
		}
		if (getSentEvents().getOrDefault(eventType, Collections.emptyList()).contains(service))
			return;
		String login = ofNullable(service.getSource()).map(Request.Source::login).orElse(null);
		Response.Builder builder = service.buildResponse()
			.packetType(PacketType.GENERIC_EVENT)
			.eventType(eventType)
			.internal(true);
		boolean success = force;
		if (force) {
			getSLF4JLogger().info("Sending {} packet for {} to {}", eventType, login, service.getDisplayName());
			builder.putData("player", login).send();
		} else {
			getSLF4JLogger().info("Sources for service {}: {}", service.getDisplayName(), service.getSources());
			Optional<P> optPlayer = ofNullable(login).flatMap(playerMapper()::getPlayerByLogin);
			if (optPlayer.isPresent()) {
				success = true;
				P player = optPlayer.get();
				getSLF4JLogger().info("Sending {} packet for {} to {}", eventType, playerMapper().getUsername(player), service.getDisplayName());
				builder.putData("player", playerMapper().getUniqueId(player).toString().replace("-", "").toLowerCase(Locale.ENGLISH)).send();
			}
		}
		if (success)
			getSentEvents().computeIfAbsent(eventType, key -> new ArrayList<>()).add(service);
	}

	/**
	 * Finds the {@link SocketManager}s for a player and sends a player event packet to each.
	 *
	 * @param player    the player to send the event for
	 * @param eventType the type of event to send
	 */
	default void sendPlayerEvent(@NotNull P player, @NotNull String eventType) {
		for (SocketManager service : getSocketManagersFor(player)) {
			sendPlayerEvent(service, eventType, true);
		}
	}

	/**
	 * Performs actions that are reliant on the initialization of a {@link CrowdControl} instance.
	 *
	 * @param service the initialized {@link CrowdControl} instance
	 */
	default void postInitCrowdControl(@NotNull CrowdControl service) {
		Object[] effects = commandRegister().getCommands().stream().map(command -> command.getEffectName().toLowerCase(Locale.US)).toArray();
		service.addConnectListener(connectingService -> getScheduledExecutor().schedule(() -> {
			sendEmbeddedMessagePacket(connectingService, "known_effects", effects);
			updateConditionalEffectVisibility(connectingService);
			sendPlayerEvent(connectingService, "playerJoined", isGlobal());
		}, 1, TimeUnit.SECONDS));
	}

	/**
	 * Updates the status of effects.
	 *
	 * @param respondable an object that can be responded to
	 * @param status      the new status
	 * @param ids         the IDs to update
	 */
	default void updateEffectStatus(@Nullable Respondable respondable, @NotNull ResultType status, @NotNull String @NotNull ... ids) {
		if (!status.isStatus())
			throw new IllegalArgumentException("status must be a status type (not a result type)");
		if (respondable == null)
			return;
		getSLF4JLogger().debug("Updating status of effects {} to {}", Arrays.toString(ids), status);
		Response response = respondable.buildResponse()
			.packetType(PacketType.EFFECT_STATUS)
			.ids(Arrays.stream(ids).map(id -> id.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList()))
			.type(status)
			.build();
		response.send();
	}

	/**
	 * Updates the status of effects.
	 *
	 * @param respondable an object that can be responded to
	 * @param status      the new status
	 * @param ids         the IDs to update
	 */
	default void updateEffectStatus(Respondable respondable, @NotNull ResultType status, @NotNull Command<?> @NotNull ... ids) {
		updateEffectStatus(respondable, status, Arrays.stream(ids).map(Command::getEffectName).toArray(String[]::new));
	}

	/**
	 * Updates the status of effects.
	 *
	 * @param respondable an object that can be responded to
	 * @param status      the new status
	 * @param ids         the IDs to update
	 */
	default void updateEffectIdStatus(Respondable respondable, @NotNull ResultType status, @NotNull Collection<String> ids) {
		updateEffectStatus(respondable, status, ids.toArray(new String[0]));
	}

	/**
	 * Updates the visibility of a collection of {@link Command effect} IDs.
	 *
	 * @param respondable an object that can be responded to
	 * @param visible     effects' new visibility
	 * @param ids         the IDs to update
	 */
	default void updateEffectIdVisibility(Respondable respondable, boolean visible, @NotNull Collection<String> ids) {
		updateEffectStatus(respondable, visible ? ResultType.VISIBLE : ResultType.NOT_VISIBLE, ids.toArray(new String[0]));
	}

	/**
	 * Updates the visibility of an {@link Command effect} ID.
	 *
	 * @param respondable an object that can be responded to
	 * @param visible     effect's new visibility
	 * @param ids         the IDs to update
	 */
	default void updateEffectIdVisibility(Respondable respondable, boolean visible, @NotNull String @NotNull ... ids) {
		updateEffectIdVisibility(respondable, visible, Arrays.asList(ids));
	}

	/**
	 * Updates the visibility of a collection of {@link Command effects}.
	 *
	 * @param respondable an object that can be responded to
	 * @param visible     effects' new visibility
	 * @param ids         the IDs to update
	 */
	default void updateEffectVisibility(Respondable respondable, boolean visible, @NotNull Collection<Command<?>> ids) {
		updateEffectIdVisibility(respondable, visible, ids.stream().map(Command::getEffectName).collect(Collectors.toList()));
	}

	/**
	 * Updates the visibility of an {@link Command effect}.
	 *
	 * @param respondable an object that can be responded to
	 * @param visible     effect's new visibility
	 * @param ids         the IDs to update
	 */
	default void updateEffectVisibility(Respondable respondable, boolean visible, @NotNull Command<?> @NotNull ... ids) {
		updateEffectVisibility(respondable, visible, Arrays.asList(ids));
	}

	/**
	 * Updates the {@link CrowdControl} instance.
	 * <p>
	 * You must first ensure that you have shut down the existing
	 * {@link CrowdControl} {@link #getCrowdControl() instance}.
	 *
	 * @param crowdControl the new {@link CrowdControl} instance
	 */
	void updateCrowdControl(@Nullable CrowdControl crowdControl);

	/**
	 * Restarts the {@link CrowdControl} instance.
	 */
	default void restartCrowdControl() {
		CrowdControl cc = getCrowdControl();
		if (cc != null)
			cc.shutdown("Service is restarting");
		updateCrowdControl(null);
		initCrowdControl();
	}

	/**
	 * Whether this plugin implementation supports {@link ClientOnly} effects.
	 *
	 * @return whether client effects are supported
	 */
	default boolean supportsClientOnly() {
		return false;
	}

	/**
	 * Gets the plugin's {@link CommandManager}.
	 *
	 * @return command manager instance
	 */
	@Nullable
	@CheckReturnValue
	CommandManager<S> getCommandManager();

	/**
	 * Gets the password required for clients to connect to the server.
	 * <p>
	 * If the password is not set, this will return null.
	 *
	 * @return unencrypted password
	 */
	@Nullable
	@CheckReturnValue
	String getPassword();

	/**
	 * Gets the password required for clients to connect to the server.
	 * <p>
	 * If the password is not set, this will return an empty string.
	 *
	 * @return unencrypted password
	 */
	@NotNull
	@CheckReturnValue
	default String getPasswordOrEmpty() {
		return ExceptionUtil.validateNotNullElse(getPassword(), "");
	}

	/**
	 * Sets the password required for clients to connect to the server.
	 * <p>
	 * {@link #restartCrowdControl()} must be called for this to take effect.
	 *
	 * @param password unencrypted password
	 * @throws IllegalArgumentException if the password is null
	 */
	void setPassword(@NotNull String password) throws IllegalArgumentException;

	/**
	 * Updates the visibility of conditional effects (i.e. client effects & global effects).
	 *
	 * @param service the service to send the packets to
	 */
	default void updateConditionalEffectVisibility(@Nullable SocketManager service) {
		if (service == null)
			return;
		boolean clientVisible = getModdedPlayerCount() > 0;
		boolean globalVisible = globalEffectsUsable();
		getSLF4JLogger().debug("Updating conditional effects: clientVisible={}, globalVisible={}", clientVisible, globalVisible);
		Map<ResultType, Set<String>> effects = new HashMap<>();
		for (Command<?> effect : commandRegister().getCommands()) {
			String id = effect.getEffectName().toLowerCase(Locale.ENGLISH);
			TriState visibility = effect.isVisible();
			if (visibility != TriState.FALSE) {
				if (effect.isClientOnly())
					visibility = TriState.fromBoolean(clientVisible);
				else if (effect.isGlobal())
					visibility = TriState.fromBoolean(globalVisible);
			}
			if (visibility != TriState.UNKNOWN)
				effects.computeIfAbsent(visibility == TriState.TRUE ? ResultType.VISIBLE : ResultType.NOT_VISIBLE, k -> new HashSet<>()).add(id);

			TriState selectable = effect.isSelectable();
			if (selectable != TriState.UNKNOWN && visibility != TriState.FALSE)
				effects.computeIfAbsent(selectable == TriState.TRUE ? ResultType.SELECTABLE : ResultType.NOT_SELECTABLE, k -> new HashSet<>()).add(id);
		}
		for (Map.Entry<ResultType, Set<String>> entry : effects.entrySet())
			updateEffectIdStatus(service, entry.getKey(), entry.getValue());
	}

	/**
	 * Renders messages to a player. This should be called by an event handler that listens for
	 * players joining the server.
	 *
	 * @param joiningPlayer player to send messages to
	 */
	default void onPlayerJoin(P joiningPlayer) {
		PlayerEntityMapper<P> mapper = playerMapper();
		UUID uuid = mapper.tryGetUniqueId(joiningPlayer).orElse(null);
		if (uuid == null) {
			getSLF4JLogger().warn("Player {} has no UUID", mapper.getUsername(joiningPlayer));
			return;
		}
		sendPlayerEvent(joiningPlayer, "playerJoined");
		getScheduledExecutor().schedule(() -> {
			// ensure player is still online
			Optional<P> optPlayer = mapper.getPlayer(uuid);
			if (!optPlayer.isPresent())
				return;
			P player = optPlayer.get();
			// send messages
			Audience audience = mapper.asAudience(player);
			audience.sendMessage(JOIN_MESSAGE_1);
			if (!isGlobal() && !hasLinkedAccount(joiningPlayer) && (!isAdminRequired() || playerMapper().isAdmin(player)))
				audience.sendMessage(JOIN_MESSAGE_2);
			if (!globalEffectsUsable())
				audience.sendMessage(NO_GLOBAL_EFFECTS_MESSAGE);
			CrowdControl cc = getCrowdControl();
			if (cc == null) {
				if (mapper.isAdmin(player)) {
					if (getPasswordOrEmpty().isEmpty())
						audience.sendMessage(NO_CC_OP_ERROR_NO_PASSWORD);
					else
						audience.sendMessage(NO_CC_UNKNOWN_ERROR);
				} else
					audience.sendMessage(NO_CC_USER_ERROR);
			}
			// update conditional effects
			updateConditionalEffectVisibility(cc);
		}, 1, TimeUnit.SECONDS);
	}

	/**
	 * Handles various behavior related to the departure of a player. This should be called by an
	 * event handler that listens for players leaving the server.
	 *
	 * @param player player that left
	 */
	default void onPlayerLeave(P player) {
		updateConditionalEffectVisibility(getCrowdControl());
	}

	/**
	 * Gets the plugin's SLF4J logger.
	 *
	 * @return slf4j logger
	 */
	@NotNull
	Logger getSLF4JLogger();

	/**
	 * Gets the executor which runs code synchronously (i.e. on the server's main thread).
	 *
	 * @return synchronous executor
	 */
	@NotNull
	Executor getSyncExecutor();

	/**
	 * Gets the executor which runs code asynchronously (i.e. off the server's main thread).
	 *
	 * @return asynchronous executor
	 */
	@NotNull
	Executor getAsyncExecutor();

	/**
	 * Gets the plugin's {@link LimitConfig}.
	 *
	 * @return limit config parsed from the plugin's config file
	 */
	@NotNull
	LimitConfig getLimitConfig();

	/**
	 * Gets the plugin's {@link SoftLockConfig}.
	 *
	 * @return soft-lock config parsed from the plugin's config file
	 */
	@NotNull
	SoftLockConfig getSoftLockConfig();

	/**
	 * Gets the server's console {@link Audience}.
	 *
	 * @return console audience
	 */
	@NotNull
	Audience getConsole();

	/**
	 * Gets the plugin's {@link CCPlayer wrapper} for a player.
	 *
	 * @param player player to get the wrapper for
	 * @return wrapper for the player
	 */
	@NotNull
	CCPlayer getPlayer(@NotNull P player);

	/**
	 * Gets the {@link HideNames} config.
	 *
	 * @return hide names config
	 */
	@NotNull
	HideNames getHideNames();

	/**
	 * Sets the {@link HideNames} config.
	 *
	 * @param hideNames hide names config
	 */
	void setHideNames(@NotNull HideNames hideNames);

	/**
	 * Gets the viewer who triggered an effect as a component, or null if names are hidden.
	 *
	 * @param request the effect request
	 * @param chat    whether the returned component will be used in chat
	 * @return the viewer as a component, or null if names are hidden
	 */
	@Nullable
	default Component getViewerComponentOrNull(@NotNull Request request, boolean chat) {
		return getViewerComponentOrNull(getHideNames(), request, chat);
	}

	/**
	 * Gets the viewer who triggered an effect as a component.
	 *
	 * @param request the effect request
	 * @param chat    whether the returned component will be used in chat
	 * @return the viewer as a component
	 */
	@NotNull
	default Component getViewerComponent(@NotNull Request request, boolean chat) {
		return getViewerComponent(getHideNames(), request, chat);
	}

	/**
	 * Gets the viewer who triggered an effect as a component, or null if names are hidden.
	 *
	 * @param hidesNames the {@link HideNames} config
	 * @param request    the effect request
	 * @param chat       whether the returned component will be used in chat
	 * @return the viewer as a component, or null if names are hidden
	 */
	@Nullable
	static Component getViewerComponentOrNull(@NotNull HideNames hidesNames, @NotNull Request request, boolean chat) {
		if ((!chat && hidesNames.isHideOther()) || (chat && hidesNames.isHideChat()))
			return null;
		return text(request.getViewer());
	}

	/**
	 * Gets the viewer who triggered an effect as a component.
	 *
	 * @param hidesNames the {@link HideNames} config
	 * @param request    the effect request
	 * @param chat       whether the returned component will be used in chat
	 * @return the viewer as a component
	 */
	@NotNull
	static Component getViewerComponent(@NotNull HideNames hidesNames, @NotNull Request request, boolean chat) {
		return ExceptionUtil.validateNotNullElse(getViewerComponentOrNull(hidesNames, request, chat), VIEWER_NAME);
	}

	/**
	 * Returns whether {@link #getModVersion(Object)} is expected to be able to produce a value.
	 * This is {@code false} on mod implementations that do not support mod version checking and client-side effects.
	 *
	 * @return whether {@link #getModVersion(Object)} is expected to be able to produce a value
	 */
	default boolean canGetModVersion() {
		return false;
	}

	/**
	 * Returns the version of the mod that the provided player is using.
	 * May be empty if the player is not using the mod locally.
	 *
	 * @param player the player to check
	 * @return the version of the mod that the player is using
	 */
	default @NotNull Optional<SemVer> getModVersion(@NotNull P player) {
		return Optional.empty();
	}

	/**
	 * Returns the number of players that are currently using the mod.
	 *
	 * @return the number of players that are currently using the mod
	 */
	default int getModdedPlayerCount() {
		return 0;
	}

	/**
	 * Gets whether to try auto-linking accounts based on IP address.
	 *
	 * @return whether to try auto-linking accounts based on IP address
	 */
	default boolean isAutoDetectIP() {
		return true;
	}

	/**
	 * Gets the SocketManager associated with the provided player.
	 *
	 * @param player the player to get the SocketManager for
	 * @return the SocketManager associated with the provided player
	 */
	default @NotNull List<SocketManager> getSocketManagersFor(@NotNull P player) {
		CrowdControl cc = getCrowdControl();
		if (cc == null) return Collections.emptyList();
		// get player info
		UUID uuid = playerMapper().getUniqueId(player);
		String username = playerMapper().getUsername(player);
		InetAddress ip = playerMapper().getIP(player).orElse(null);
		// find managers
		List<SocketManager> managers = new ArrayList<>();
		for (SocketManager manager : cc.getConnections()) {
			// check for source
			Request.Source source = manager.getSource();
			if (source == null) {
				getSLF4JLogger().debug("Skipping SocketManager {} in search for player {}'s sockets for lack of source", manager, username);
				continue;
			}
			boolean found = false;
			// search for matching data from the app (user-provided name or app-guessed UUID)
			if (source.login() != null) {
				LoginData data = new LoginData(source.login());
				if (uuid.equals(data.getId()) || username.equalsIgnoreCase(data.getName()))
					found = true;
			}
			// else search for matching data from the game (user-provided stream account)
			if (!found && source.target() != null) {
				String name = source.target().getName();
				String login = source.target().getLogin();
				for (String account : getPlayerManager().getLinkedAccounts(uuid)) {
					if (account.equalsIgnoreCase(name) || account.equalsIgnoreCase(login)) {
						found = true;
						break;
					}
				}
			}
			// else check if CC app client's IP matches the player's IP
			if (!found && isAutoDetectIP() && source.ip() != null && source.ip().equals(ip)) {
				found = true;
			}
			// add manager if found
			if (found) {
				getSLF4JLogger().debug("Found SocketManager {} for player {}", manager, username);
				managers.add(manager);
			} else {
				getSLF4JLogger().debug("Skipping SocketManager {} in search for player {}'s sockets for lack of matching data", manager, username);
			}
		}
		return managers;
	}

	/**
	 * Determines if the provided player has an account linked.
	 *
	 * @param player the player to check
	 * @return true if the player has an account linked, false otherwise
	 */
	default boolean hasLinkedAccount(@NotNull P player) {
		if (getPlayerManager().getLinkedAccounts(playerMapper().getUniqueId(player)).size() > 0)
			return true;
		return getSocketManagersFor(player).size() > 0;
	}
}
