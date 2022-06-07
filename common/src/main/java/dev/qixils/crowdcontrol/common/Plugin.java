package dev.qixils.crowdcontrol.common;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import dev.qixils.crowdcontrol.CrowdControl;
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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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
	 * The prefix to use in command output.
	 */
	String PREFIX = "CrowdControl";

	/**
	 * The permission node required to use administrative commands.
	 */
	String ADMIN_PERMISSION = "crowdcontrol.admin";

	/**
	 * Port that the {@link CrowdControl} service connects to or listen on.
	 */
	int DEFAULT_PORT = 58431;

	/**
	 * The message to send to a player when they join the server.
	 */
	Component JOIN_MESSAGE_1 = new TextBuilder(JOIN_MESSAGE_COLOR)
			.rawNext("This server is running ")
			.next("Crowd Control", TextColor.color(0xFAE100)) // picked a color from the CC logo/icon
			.rawNext(", developed by ")
			.next("qi", TextColor.color(0xFFC7B5))
			.next("xi", TextColor.color(0xFFDECA))
			.next("ls", TextColor.color(0xFFCEEA))
			.next(".dev", TextColor.color(0xFFB7E5))
			.rawNext(" in coordination with the ")
			.next("crowdcontrol.live", TextColor.color(0xFAE100))
			.rawNext(" team.")
			.build();

	/**
	 * A warning message sent to players when they join the server if they have no Twitch account
	 * linked.
	 */
	Component JOIN_MESSAGE_2 = new TextBuilder(TextColor.color(0xF1D4FC))
			.rawNext("Please link your Twitch account using ")
			.next("/account link <username>", NamedTextColor.GOLD)
			.rawNext(". You can ")
			.next("click here", TextDecoration.BOLD)
			.rawNext(" to do so.")
			.suggest("/account link ")
			.hover(Component.text("Click here to link your Twitch account").asHoverEvent())
			.build();

	/**
	 * A warning message sent to players when they join the server if global effects are
	 * completely unavailable.
	 */
	Component NO_GLOBAL_EFFECTS_MESSAGE = new TextBuilder(JOIN_MESSAGE_COLOR)
			.next("Warning: ", NamedTextColor.YELLOW, TextDecoration.BOLD)
			.rawNext("Effects that alter server settings (such as the server difficulty) are currently unavailable. " +
					"If this isn't intended then please open the plugin's config file and set ")
			.next("global", TextColor.color(0xEBBD8D))
			.rawNext(" to ")
			.next("true", TextColor.color(0xEBBD8D))
			.rawNext(" to enable these effects.")
			.build();

	/**
	 * The color used for displaying error messages on join.
	 */
	TextColor _ERROR_COLOR = TextColor.color(0xF78080);

	/**
	 * Error message displayed to non-admins when the service is not enabled.
	 */
	Component NO_CC_USER_ERROR = new TextBuilder(_ERROR_COLOR)
			.next("WARNING: ", NamedTextColor.RED)
			.rawNext("The Crowd Control plugin has failed to load. Please ask a server administrator to the console logs and address the error.")
			.build();

	/**
	 * Error message displayed to admins when the password is not set.
	 */
	Component NO_CC_OP_ERROR_NO_PASSWORD = new TextBuilder(_ERROR_COLOR)
			.next("WARNING: ", NamedTextColor.RED)
			.rawNext("The Crowd Control plugin has failed to load due to a password not being set. Please use ")
			.next("/password <password>", NamedTextColor.GOLD)
			.rawNext(" to set a password and ")
			.next("/crowdcontrol reconnect", NamedTextColor.GOLD)
			.next(" to properly load the plugin. And be careful not to show the password on stream!")
			.suggest("/password ")
			.hover(Component.text("Click here to set the password").asHoverEvent())
			.build();

	/**
	 * Error message displayed to admins when the error is unknown.
	 */
	Component NO_CC_UNKNOWN_ERROR = new TextBuilder(_ERROR_COLOR)
			.next("WARNING: ", NamedTextColor.RED)
			.rawNext("The Crowd Control plugin has failed to load. Please review the console logs and resolve the error.")
			.build();

	/**
	 * Registers the plugin's basic chat commands.
	 */
	default void registerChatCommands() {
		CommandManager<S> manager = getCommandManager();
		if (manager == null)
			throw new IllegalStateException("CommandManager is null");
		EntityMapper<S> mapper = commandSenderMapper();

		//// Account Command ////

		// base command
		Builder<S> account = manager.commandBuilder("account")
				.meta(CommandMeta.DESCRIPTION, "Manage your connected Twitch account(s)");

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
						audience.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.next(username, NamedTextColor.AQUA)
								.rawNext(" has been added to your linked Twitch accounts"));
					else
						audience.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.color(NamedTextColor.RED)
								.next("You have already linked the Twitch account ")
								.next(username, NamedTextColor.AQUA));
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
						audience.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.next(username, NamedTextColor.AQUA)
								.rawNext(" has been removed from your linked Twitch accounts"));
					else
						audience.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.color(NamedTextColor.RED)
								.next("You do not have a Twitch account linked named ")
								.next(username, NamedTextColor.AQUA));
				}));

		//// CrowdControl Command ////

		// base command
		Builder<S> ccCmd = manager.commandBuilder("crowdcontrol")
				.meta(CommandMeta.DESCRIPTION, "Manage the Crowd Control socket")
				.permission(mapper::isAdmin);

		// connect command
		final Component serviceNotDisconnected = TextBuilder.fromPrefix(PREFIX,
				"&cService is already running or attempting to establish a connection").build();
		final Component serviceStarted = TextBuilder.fromPrefix(PREFIX,
				"Service has been re-enabled and will be attempted in the background").build();
		manager.command(ccCmd.literal("connect")
				.meta(CommandMeta.DESCRIPTION, "Connect to the Crowd Control service")
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (getCrowdControl() != null)
						sender.sendMessage(serviceNotDisconnected);
					else {
						initCrowdControl();
						sender.sendMessage(serviceStarted);
					}
				}));
		// disconnect command
		final Component serviceNotRunning = TextBuilder.fromPrefix(PREFIX,
				"&cService is already disconnected").build();
		final Component serviceStopped = TextBuilder.fromPrefix(PREFIX,
				"Service has been disabled").build();
		manager.command(ccCmd.literal("disconnect")
				.meta(CommandMeta.DESCRIPTION, "Disconnect from the Crowd Control service")
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (getCrowdControl() == null)
						sender.sendMessage(serviceNotRunning);
					else {
						getCrowdControl().shutdown("Disconnected issued by server administrator");
						updateCrowdControl(null);
						sender.sendMessage(serviceStopped);
					}
				}));
		// reconnect command
		final Component serviceReset = TextBuilder.fromPrefix(PREFIX,
				"Service has been reset").build();
		manager.command(ccCmd.literal("reconnect")
				.meta(CommandMeta.DESCRIPTION, "Reconnect to the Crowd Control service")
				.handler(commandContext -> {
					Audience audience = mapper.asAudience(commandContext.getSender());
					CrowdControl cc = getCrowdControl();
					if (cc != null)
						cc.shutdown("Reconnect issued by server administrator");
					initCrowdControl();

					audience.sendMessage(serviceReset);
				}));
		// status command
		final Component notRunning = TextBuilder.fromPrefix(PREFIX, "The service is not currently running").build();
		final Component isRunning = TextBuilder.fromPrefix(PREFIX, "The service is currently running").build();
		manager.command(ccCmd.literal("status")
				.meta(CommandMeta.DESCRIPTION, "Get the status of the Crowd Control service")
				.handler(commandContext -> mapper.asAudience(commandContext.getSender()).sendMessage(
						getCrowdControl() == null ? notRunning : isRunning)));

		//// Password Command ////
		final Component passwordSuccessMessage = TextBuilder.fromPrefix(Plugin.PREFIX)
				.rawNext("The password has been updated. Please use ")
				.next("/crowdcontrol reconnect", NamedTextColor.YELLOW)
				.rawNext(" or click here to apply this change.")
				.suggest("/crowdcontrol reconnect")
				.hover(new TextBuilder().rawNext("Click here to run ").next("/crowdcontrol reconnect", NamedTextColor.YELLOW))
				.build();
		final Component passwordFailureMessage = TextBuilder.fromPrefix(Plugin.PREFIX)
				.next("This command can only be used when running in server mode.", NamedTextColor.RED)
				.build();
		manager.command(manager.commandBuilder("password")
				.meta(CommandMeta.DESCRIPTION, "Sets the password required for Crowd Control clients to connect to the server")
				.permission(mapper::isAdmin)
				.argument(StringArgument.<S>newBuilder("password").greedy().asRequired())
				.handler(commandContext -> {
					Audience sender = mapper.asAudience(commandContext.getSender());
					if (!isServer()) {
						sender.sendMessage(passwordFailureMessage);
						return;
					}
					String password = commandContext.get("password");
					setPassword(password);
					sender.sendMessage(passwordSuccessMessage);
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
	 * Fetches the username of a player.
	 *
	 * @param player the player to fetch the username of
	 * @return the username of the player
	 */
	@CheckReturnValue
	default @NotNull String getUsername(@NotNull P player) {
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
	AbstractCommandRegister<P, ?, ?> commandRegister();

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
	default void sendEmbeddedMessagePacket(@NotNull SocketManager service, @NotNull String message) {
		getScheduledExecutor().schedule(() -> {
			getSLF4JLogger().debug("sending packet {} to {}", message, service);
			Response response = service.buildResponse(0)
					.packetType(PacketType.EFFECT_RESULT)
					.type(ResultType.SUCCESS)
					.message(message)
					.build();
			getSLF4JLogger().debug("final packet: {}", response.toJSON());
			response.send();
		}, 3, TimeUnit.SECONDS);
	}

	/**
	 * Sends a packet with an embedded message for the C# client.
	 *
	 * @param message the message to send
	 */
	default void sendEmbeddedMessagePacket(@NotNull String message) {
		SocketManager service = getCrowdControl();
		if (service == null) {
			getSLF4JLogger().warn("Attempted to send embedded message packet but the service is unavailable");
			return;
		}
		sendEmbeddedMessagePacket(service, message);
	}

	/**
	 * Performs actions that are reliant on the initialization of a {@link CrowdControl} instance.
	 *
	 * @param service the initialized {@link CrowdControl} instance
	 */
	default void postInitCrowdControl(@NotNull CrowdControl service) {
		service.addConnectListener(connectingService -> sendEmbeddedMessagePacket(service, "_mc_cc_server_status_" + new ServerStatus(
				globalEffectsUsable(),
				supportsClientOnly(),
				commandRegister().getCommands().stream().map(Command::getEffectName).collect(Collectors.toList())
		).toJSON()));
	}

	/**
	 * Updates the visibility of a collection of {@link Command effect} IDs.
	 *
	 * @param effectIds IDs of the effect to update
	 * @param visible   effects' new visibility
	 */
	default void updateEffectIdVisibility(@NotNull Collection<String> effectIds, boolean visible) {
		StringBuilder message = new StringBuilder("_mc_cc_");
		if (visible)
			message.append("show");
		else
			message.append("hide");
		message.append("_effects_:");
		message.append(String.join(",", effectIds));
		sendEmbeddedMessagePacket(message.toString());
	}

	/**
	 * Updates the visibility of an {@link Command effect} ID.
	 *
	 * @param effectId ID of the effect to update
	 * @param visible  effect's new visibility
	 */
	default void updateEffectIdVisibility(@NotNull String effectId, boolean visible) {
		updateEffectIdVisibility(Collections.singletonList(effectId), visible);
	}

	/**
	 * Updates the visibility of a collection of {@link Command effects}.
	 *
	 * @param effects the effect to update
	 * @param visible effects' new visibility
	 */
	default void updateEffectVisibility(@NotNull Collection<Command<?>> effects, boolean visible) {
		updateEffectIdVisibility(effects.stream().map(Command::getEffectName).collect(Collectors.toList()), visible);
	}

	/**
	 * Updates the visibility of an {@link Command effect}.
	 *
	 * @param effect  the effect to update
	 * @param visible effect's new visibility
	 */
	default void updateEffectVisibility(@NotNull Command<?> effect, boolean visible) {
		updateEffectVisibility(Collections.singletonList(effect), visible);
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
		if (!isGlobal() && isServer() && getPlayerManager().getLinkedAccounts(mapper.getUniqueId(player).get()).size() == 0)
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
	Executor getSyncExecutor();

	/**
	 * Gets the executor which runs code asynchronously (i.e. off the server's main thread).
	 *
	 * @return asynchronous executor
	 */
	Executor getAsyncExecutor();
}
