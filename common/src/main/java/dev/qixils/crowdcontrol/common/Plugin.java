package dev.qixils.crowdcontrol.common;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.PacketType;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import dev.qixils.crowdcontrol.socket.SocketManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	Component PREFIX_COMPONENT = Component.text()
			.color(NamedTextColor.DARK_AQUA)
			.append(Component.text('[', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
			.append(Component.text(PREFIX, NamedTextColor.YELLOW))
			.append(Component.text(']', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
			.append(Component.space())
			.build();

	/**
	 * The permission node required to use administrative commands.
	 */
	String ADMIN_PERMISSION = "crowdcontrol.admin";

	/**
	 * Port that the {@link CrowdControl} service connects to or listen on.
	 */
	int DEFAULT_PORT = 58431;

	/**
	 * Formats the provided text as an error message.
	 *
	 * @param text text to format
	 * @return formatted text
	 */
	static @NotNull Component error(@NotNull Component text) {
		if (text.decoration(TextDecoration.BOLD) == TextDecoration.State.NOT_SET)
			text = text.decoration(TextDecoration.BOLD, false);
		return Component.translatable(
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
		return Component.translatable(
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
	Component JOIN_MESSAGE_1 = Component.translatable(
			"cc.join.info",
			JOIN_MESSAGE_COLOR,
			Component.text("Crowd Control", TextColor.color(0xFAE100)),
			Component.text("qi", TextColor.color(0xFFC7B5))
					.append(Component.text("xi", TextColor.color(0xFFDECA)))
					.append(Component.text("ls", TextColor.color(0xFFCEEA)))
					.append(Component.text(".dev", TextColor.color(0xFFB7E5))),
			Component.text("crowdcontrol.live", TextColor.color(0xFAE100))
	);

	/**
	 * A warning message sent to players when they join the server if they have no Twitch account
	 * linked.
	 */
	Component JOIN_MESSAGE_2 = Component.translatable(
			"cc.join.link.text",
			TextColor.color(0xF1D4FC),
			Component.text("/account link <username>", NamedTextColor.GOLD)
	)
			.hoverEvent(Component.translatable("cc.join.link.hover"))
			.clickEvent(ClickEvent.suggestCommand("/account link "));

	/**
	 * A warning message sent to players when they join the server if global effects are
	 * completely unavailable.
	 */
	Component NO_GLOBAL_EFFECTS_MESSAGE = warning(Component.translatable(
			"cc.error.no-global-effects",
			Component.text("global", TextColor.color(0xF9AD9E)),
			Component.text("true", TextColor.color(0xF9AD9E))
	));

	/**
	 * Error message displayed to non-admins when the service is not enabled.
	 */
	Component NO_CC_USER_ERROR = Component.translatable(
			"cc.error.prefix.critical",
			NamedTextColor.RED,
			Component.translatable(
					"cc.error.user-error",
					_ERROR_COLOR
			)
	);

	/**
	 * Error message displayed to admins when the password is not set.
	 */
	Component NO_CC_OP_ERROR_NO_PASSWORD = warning(Component.translatable(
			"cc.error.no-password.text",
			Component.text("/password <password>", NamedTextColor.GOLD),
			Component.text("/crowdcontrol reconnect", NamedTextColor.GOLD)
	)
			.clickEvent(ClickEvent.suggestCommand("/password "))
			.hoverEvent(Component.translatable("cc.error.no-password.hover"))
	);

	/**
	 * Error message displayed to admins when the error is unknown.
	 */
	Component NO_CC_UNKNOWN_ERROR = error(Component.translatable("cc.error.unknown"));

	/**
	 * Registers the plugin's basic chat commands.
	 */
	default void registerChatCommands() {
		KyoriTranslator.initialize();

		CommandManager<S> manager = getCommandManager();
		if (manager == null)
			throw new IllegalStateException("CommandManager is null");
		EntityMapper<S> mapper = commandSenderMapper();

		// TODO: support i18n in cloud command descriptions

		//// Account Command ////

		// base command
		Builder<S> account = manager.commandBuilder("account")
				.meta(CommandMeta.DESCRIPTION, "Manage your connected Twitch account(s)");
		if (isAdminRequired())
			account = account.permission(mapper::isAdmin);

		// username arg
		CommandArgument<S, String> usernameArg = StringArgument.<S>newBuilder("username")
				.single().asRequired().manager(manager).build();
		ArgumentDescription usernameDesc = ArgumentDescription.of("The username of the Twitch account to link");

		// link command
		manager.command(account.literal("link")
				.meta(CommandMeta.DESCRIPTION, "Link a Twitch account to your Minecraft account")
				.argument(usernameArg, usernameDesc)
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					Audience audience = mapper.asAudience(sender);
					UUID uuid = mapper.getUniqueId(sender).orElseThrow(() ->
							new IllegalArgumentException("Your UUID cannot be found. Please ensure you are running this command in-game."));
					if (getPlayerManager().linkPlayer(uuid, username))
						audience.sendMessage(output(Component.translatable(
								"cc.command.account.link.output",
								Component.text(username, NamedTextColor.AQUA)
						)));
					else
						audience.sendMessage(output(Component.translatable(
								"cc.command.account.link.error",
								NamedTextColor.RED,
								Component.text(username, NamedTextColor.AQUA)
						)));
				}));
		// unlink command
		manager.command(account.literal("unlink")
				.meta(CommandMeta.DESCRIPTION, "Unlink a Twitch account from your Minecraft account")
				.argument(usernameArg.copy(), usernameDesc)
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					Audience audience = mapper.asAudience(sender);
					UUID uuid = mapper.getUniqueId(sender).orElseThrow(() ->
							new IllegalArgumentException("Your UUID cannot be found. Please ensure you are running this command in-game."));
					if (getPlayerManager().unlinkPlayer(uuid, username))
						audience.sendMessage(output(Component.translatable(
								"cc.command.account.unlink.output",
								Component.text(username, NamedTextColor.AQUA)
						)));
					else
						audience.sendMessage(output(Component.translatable(
								"cc.command.account.unlink.error",
								NamedTextColor.RED,
								Component.text(username, NamedTextColor.AQUA)
						)));
				}));

		//// CrowdControl Command ////

		// base command
		Builder<S> ccCmd = manager.commandBuilder("crowdcontrol")
				.meta(CommandMeta.DESCRIPTION, "Manage the Crowd Control socket")
				.permission(mapper::isAdmin);

		// connect command
		manager.command(ccCmd.literal("connect")
				.meta(CommandMeta.DESCRIPTION, "Connect to the Crowd Control service")
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (getCrowdControl() != null)
						sender.sendMessage(output(Component.translatable("cc.command.crowdcontrol.connect.error", NamedTextColor.RED)));
					else {
						initCrowdControl();
						sender.sendMessage(output(Component.translatable("cc.command.crowdcontrol.connect.output")));
					}
				}));
		// disconnect command
		manager.command(ccCmd.literal("disconnect")
				.meta(CommandMeta.DESCRIPTION, "Disconnect from the Crowd Control service")
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (getCrowdControl() == null)
						sender.sendMessage(output(Component.translatable("cc.command.crowdcontrol.disconnect.error", NamedTextColor.RED)));
					else {
						getCrowdControl().shutdown("Disconnected issued by server administrator");
						updateCrowdControl(null);
						sender.sendMessage(output(Component.translatable("cc.command.crowdcontrol.disconnect.output")));
					}
				}));
		// reconnect command
		manager.command(ccCmd.literal("reconnect")
				.meta(CommandMeta.DESCRIPTION, "Reconnect to the Crowd Control service")
				.handler(commandContext -> {
					Audience audience = mapper.asAudience(commandContext.getSender());
					CrowdControl cc = getCrowdControl();
					if (cc != null)
						cc.shutdown("Reconnect issued by server administrator");
					initCrowdControl();

					audience.sendMessage(Component.translatable("cc.command.crowdcontrol.reconnect.output"));
				}));
		// status command
		manager.command(ccCmd.literal("status")
				.meta(CommandMeta.DESCRIPTION, "Get the status of the Crowd Control service")
				.handler(commandContext -> mapper.asAudience(commandContext.getSender()).sendMessage(
						Component.translatable("cc.command.crowdcontrol.status." + (getCrowdControl() != null)))));

		//// Password Command ////
		manager.command(manager.commandBuilder("password")
				.meta(CommandMeta.DESCRIPTION, "Sets the password required for Crowd Control clients to connect to the server")
				.permission(mapper::isAdmin)
				.argument(StringArgument.<S>newBuilder("password").greedy().asRequired())
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (!isServer()) {
						sender.sendMessage(output(Component.translatable("cc.command.password.error", NamedTextColor.RED)));
						return;
					}
					String password = commandContext.get("password");
					setPassword(password);
					sender.sendMessage(output(Component.translatable(
							"cc.command.password.output",
							Component.text("/crowdcontrol reconnect", NamedTextColor.YELLOW)
					)
							.hoverEvent(Component.translatable(
									"cc.command.password.output.hover",
									Component.text("/crowdcontrol reconnect", NamedTextColor.YELLOW)
							))
							.clickEvent(ClickEvent.runCommand("/crowdcontrol reconnect"))
					));
				})
		);

		new MinecraftExceptionHandler<S>()
				.withDefaultHandlers()
				.withDecorator(component -> TextBuilder.fromPrefix(PREFIX, component).color(NamedTextColor.RED).build())
				.apply(manager, mapper::asAudience);
	}

	/**
	 * Gets the {@link EntityMapper} for this implementation's player object.
	 *
	 * @return player entity mapper
	 */
	EntityMapper<P> playerMapper();

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
		return isGlobal() || !getHosts().isEmpty();
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
	 * @return a collection of strings possibly containing Twitch usernames, IDs, Minecraft
	 * usernames, or Minecraft UUIDs
	 */
	@CheckReturnValue
	@NotNull Collection<String> getHosts();

	/**
	 * Determines whether a player must be an {@link EntityMapper#isAdmin(Object) admin} to use the
	 * {@code /account} command.
	 *
	 * @return true if the player must be an admin to use the /account command
	 */
	@CheckReturnValue
	boolean isAdminRequired();

	/**
	 * Fetches the username of a player.
	 *
	 * @param player the player to fetch the username of
	 * @return the username of the player
	 */
	@CheckReturnValue
	default @NotNull String getUsername(@NotNull P player) {
		// TODO: this should be moved to the EntityMapper
		return playerMapper().asAudience(player).get(Identity.NAME).orElseThrow(() ->
				new UnsupportedOperationException("Player object does not support Identity.NAME"));
	}

	/**
	 * Whether to announce the execution of effects in chat.
	 *
	 * @return true if the plugin should announce the execution of effects in chat
	 */
	@CheckReturnValue
	boolean announceEffects();

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
	 * Sends a packet with an embedded message for the C# client.
	 *
	 * @param service the service to send the packet to
	 * @param message the message to send
	 */
	@SuppressWarnings("UnstableApiUsage") // I developed this damn API and I will use it as I please
	default void sendEmbeddedMessagePacket(@Nullable SocketManager service, @NotNull String message) {
		if (service == null)
			service = getCrowdControl();
		if (service == null) {
			getSLF4JLogger().warn("Attempted to send embedded message packet but the service is unavailable");
			return;
		}
		final @NotNull SocketManager finalService = service;
		getScheduledExecutor().schedule(() -> {
			getSLF4JLogger().debug("sending packet {} to {}", message, finalService);
			Response response = finalService.buildResponse(0)
					.packetType(PacketType.EFFECT_RESULT)
					.type(ResultType.SUCCESS)
					.message(message)
					.build();
			getSLF4JLogger().debug("final packet: {}", response.toJSON());
			response.send();
		}, 1, TimeUnit.SECONDS);
	}

	/**
	 * Sends a packet with an embedded message for the C# client.
	 *
	 * @param message the message to send
	 */
	default void sendEmbeddedMessagePacket(@NotNull String message) {
		sendEmbeddedMessagePacket(null, message);
	}

	/**
	 * Performs actions that are reliant on the initialization of a {@link CrowdControl} instance.
	 *
	 * @param service the initialized {@link CrowdControl} instance
	 */
	default void postInitCrowdControl(@NotNull CrowdControl service) {
		service.addConnectListener(connectingService -> sendEmbeddedMessagePacket(connectingService, "_mc_cc_server_status_" + new ServerStatus(
				globalEffectsUsable(),
				supportsClientOnly(),
				commandRegister().getCommands().stream().map(Command::getEffectName).collect(Collectors.toList())
		).toJSON()));
	}

	/**
	 * Updates the visibility of a collection of {@link Command effect} IDs.
	 *
	 * @param service   the service to send the packet to
	 * @param effectIds IDs of the effect to update
	 * @param visible   effects' new visibility
	 */
	default void updateEffectIdVisibility(@Nullable SocketManager service, @NotNull Collection<String> effectIds, boolean visible) {
		StringBuilder message = new StringBuilder("_mc_cc_");
		if (visible)
			message.append("show");
		else
			message.append("hide");
		message.append("_effects_:");
		message.append(String.join(",", effectIds));
		sendEmbeddedMessagePacket(service, message.toString());
	}

	/**
	 * Updates the visibility of an {@link Command effect} ID.
	 *
	 * @param service  the service to send the packet to
	 * @param effectId ID of the effect to update
	 * @param visible  effect's new visibility
	 */
	default void updateEffectIdVisibility(@Nullable SocketManager service, @NotNull String effectId, boolean visible) {
		updateEffectIdVisibility(service, Collections.singletonList(effectId), visible);
	}

	/**
	 * Updates the visibility of a collection of {@link Command effects}.
	 *
	 * @param service the service to send the packet to
	 * @param effects the effect to update
	 * @param visible effects' new visibility
	 */
	default void updateEffectVisibility(@Nullable SocketManager service, @NotNull Collection<Command<?>> effects, boolean visible) {
		updateEffectIdVisibility(service, effects.stream().map(Command::getEffectName).collect(Collectors.toList()), visible);
	}

	/**
	 * Updates the visibility of an {@link Command effect}.
	 *
	 * @param service the service to send the packet to
	 * @param effect  the effect to update
	 * @param visible effect's new visibility
	 */
	default void updateEffectVisibility(@Nullable SocketManager service, @NotNull Command<?> effect, boolean visible) {
		updateEffectVisibility(service, Collections.singletonList(effect), visible);
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
	 * Determines if the {@link CrowdControl} instance is running in server mode.
	 *
	 * @return true if the {@link CrowdControl} instance is running in server mode
	 */
	boolean isServer();

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
	 * If not running in {@link #isServer() server mode} or the password is not set, this will
	 * return null.
	 *
	 * @return SHA-512 hex-encoded hash of the password
	 */
	@Nullable
	@CheckReturnValue
	String getPassword();

	/**
	 * Sets the password required for clients to connect to the server.
	 * <p>
	 * {@link #restartCrowdControl()} must be called for this to take effect.
	 *
	 * @param password unencrypted password
	 * @throws IllegalArgumentException if the password is null
	 * @throws IllegalStateException    if the plugin is not running in {@link #isServer() server mode}
	 */
	void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException;

	/**
	 * Renders messages to a player. This should be called by an event handler that listens for
	 * players joining the server.
	 *
	 * @param player player to send messages to
	 */
	default void onPlayerJoin(P player) {
		EntityMapper<P> mapper = playerMapper();
		Audience audience = mapper.asAudience(player);
		audience.sendMessage(JOIN_MESSAGE_1);
		//noinspection OptionalGetWithoutIsPresent
		if (!isGlobal() && isServer() && getPlayerManager().getLinkedAccounts(mapper.getUniqueId(player).get()).size() == 0
				&& (!isAdminRequired() || playerMapper().isAdmin(player)))
			audience.sendMessage(JOIN_MESSAGE_2);
		if (!globalEffectsUsable())
			audience.sendMessage(NO_GLOBAL_EFFECTS_MESSAGE);
		if (getCrowdControl() == null) {
			if (mapper.isAdmin(player)) {
				if (isServer() && getPassword() == null)
					audience.sendMessage(NO_CC_OP_ERROR_NO_PASSWORD);
				else
					audience.sendMessage(NO_CC_UNKNOWN_ERROR);
			} else
				audience.sendMessage(NO_CC_USER_ERROR);
		}
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
}
