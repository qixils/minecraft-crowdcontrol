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
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The generic plugin interface.
 *
 * @param <P> class used to represent online players
 * @param <S> class used to represent command senders in Cloud Command Framework
 */
public interface Plugin<P extends S, S extends Audience> {
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
	 * The first message to send to a player when they join the server.
	 */
	Component JOIN_MESSAGE_1 = new TextBuilder(TextColor.color(0xFCE9D4))
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
	 * The second message to send to a player when they join the server.
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
				.senderType(getPlayerClass())
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					UUID uuid = getUUID(sender);
					if (getPlayerMapper().linkPlayer(uuid, username))
						sender.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.next(username, NamedTextColor.AQUA)
								.rawNext(" has been added to your linked Twitch accounts"));
					else
						sender.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.color(NamedTextColor.RED)
								.next("You have already linked the Twitch account ")
								.next(username, NamedTextColor.AQUA));
				}));
		// unlink command
		manager.command(account.literal("unlink")
				.meta(CommandMeta.DESCRIPTION, "Unlink a Twitch account from your Minecraft account")
				.argument(usernameArg, usernameDesc)
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					UUID uuid = getUUID(sender);
					if (getPlayerMapper().unlinkPlayer(uuid, username))
						sender.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.next(username, NamedTextColor.AQUA)
								.rawNext(" has been removed from your linked Twitch accounts"));
					else
						sender.sendMessage(TextBuilder.fromPrefix(Plugin.PREFIX)
								.color(NamedTextColor.RED)
								.next("You do not have a Twitch account linked named ")
								.next(username, NamedTextColor.AQUA));
				}));

		//// CrowdControl Command ////

		// base command
		Builder<S> ccCmd = manager.commandBuilder("crowdcontrol")
				.meta(CommandMeta.DESCRIPTION, "Manage the Crowd Control socket")
				.permission(this::isAdmin);

		// connect command
		final Component serviceNotDisconnected = TextBuilder.fromPrefix(PREFIX,
				"&cService is already running or attempting to establish a connection").build();
		final Component serviceStarted = TextBuilder.fromPrefix(PREFIX,
				"Service has been re-enabled and will be attempted in the background").build();
		manager.command(ccCmd.literal("connect")
				.meta(CommandMeta.DESCRIPTION, "Connect to the Crowd Control service")
				.handler(commandContext -> {
					S sender = commandContext.getSender();
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
					S sender = commandContext.getSender();
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
					S sender = commandContext.getSender();
					CrowdControl cc = getCrowdControl();
					if (cc != null)
						cc.shutdown("Reconnect issued by server administrator");
					initCrowdControl();

					sender.sendMessage(serviceReset);
				}));
		// status command
		final Component notRunning = TextBuilder.fromPrefix(PREFIX, "The service is not currently running").build();
		final Component isRunning = TextBuilder.fromPrefix(PREFIX, "The service is currently running").build();
		manager.command(ccCmd.literal("status")
				.meta(CommandMeta.DESCRIPTION, "Get the status of the Crowd Control service")
				.handler(commandContext -> commandContext.getSender().sendMessage(
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
				.permission(this::isAdmin)
				.argument(StringArgument.<S>newBuilder("password").greedy().asRequired())
				.handler(commandContext -> {
					S sender = commandContext.getSender();
					if (!isServer()) {
						sender.sendMessage(passwordFailureMessage);
						return;
					}
					String password = commandContext.get("password");
					setPassword(password);
					sender.sendMessage(passwordSuccessMessage);
				})
		);

		new MinecraftExceptionHandler<S>().withDefaultHandlers().apply(manager, source -> source);
	}

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
	PlayerMapper<P> getPlayerMapper();

	/**
	 * Fetches all online players that should be affected by the provided {@link Request}.
	 *
	 * @param request the request to be processed
	 * @return a list of online players
	 */
	@CheckReturnValue
	default @NotNull List<P> getPlayers(@NotNull Request request) {
		return getPlayerMapper().getPlayers(request);
	}

	/**
	 * Fetches all online players that should be affected by global requests.
	 *
	 * @return a list of online players
	 */
	@CheckReturnValue
	@NotNull
	default List<@NotNull P> getAllPlayers() {
		return getPlayerMapper().getAllPlayers();
	}

	/**
	 * Fetches the config variable which determines if all requests should be treated as global.
	 *
	 * @return true if all requests should be treated as global
	 */
	@CheckReturnValue
	boolean isGlobal();

	/**
	 * Determines if a request should apply to all online players or only a select few.
	 *
	 * @param request the request to be processed
	 * @return true if the request should apply to all online players
	 */
	@CheckReturnValue
	boolean isGlobal(@NotNull Request request);

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
		return player.get(Identity.NAME).orElseThrow(() -> new IllegalStateException("Player object does not support Identity.NAME"));
	}

	/**
	 * Fetches the UUID of an entity.
	 *
	 * @param entity the entity to fetch the UUID of
	 * @return the UUID of the entity
	 */
	@CheckReturnValue
	default @NotNull Optional<UUID> getUUID(@NotNull S entity) {
		return entity.get(Identity.UUID);
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
	 * Updates the {@link CrowdControl} instance.
	 * <p>
	 * You must first ensure that you have shut down the existing
	 * {@link CrowdControl} {@link #getCrowdControl() instance}.
	 *
	 * @param crowdControl the new {@link CrowdControl} instance
	 */
	void updateCrowdControl(@Nullable CrowdControl crowdControl);

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
	 *
	 * @param password unencrypted password
	 * @throws IllegalArgumentException if the password is null
	 * @throws IllegalStateException if the plugin is not running in {@link #isServer() server mode}
	 */
	void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException;

	/**
	 * Determines if the provided object is an administrator. This is defined as the object having
	 * the {@link #ADMIN_PERMISSION} permission node or being a Minecraft operator.
	 *
	 * @param commandSource the command source to check
	 * @return true if the source is an administrator
	 */
	boolean isAdmin(@NotNull S commandSource);

	default void onPlayerJoin(P player) {
		player.sendMessage(JOIN_MESSAGE_1);
		if (!isGlobal() && isServer() && getPlayerMapper().getLinkedAccounts(getUUID(player)).size() == 0)
			player.sendMessage(JOIN_MESSAGE_2);
		if (getCrowdControl() == null) {
			if (isAdmin(player)) {
				if (isServer() && getPassword() == null)
					player.sendMessage(NO_CC_OP_ERROR_NO_PASSWORD);
				else
					player.sendMessage(NO_CC_UNKNOWN_ERROR);
			} else
				player.sendMessage(NO_CC_USER_ERROR);
		}
	}
}
