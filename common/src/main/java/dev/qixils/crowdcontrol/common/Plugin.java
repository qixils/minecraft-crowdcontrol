package dev.qixils.crowdcontrol.common;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command.Builder;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import com.google.gson.Gson;
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
	Component PREFIX_COMPONENT = Component.text()
			.color(NamedTextColor.DARK_AQUA)
			.append(Component.text('[', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
			.append(Component.text(PREFIX, NamedTextColor.YELLOW))
			.append(Component.text(']', NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
			.append(Component.space())
			.build();

	/**
	 * The default name of a viewer.
	 */
	Component VIEWER_NAME = Component.translatable("cc.effect.viewer");

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
			// TODO: move these args back into the i18n file
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
	 * Gets the provided command sender as a player.
	 *
	 * @param sender the command sender
	 * @return the player, or null if the sender is not a player
	 */
	default @Nullable P asPlayer(@NotNull S sender) {
		try {
			return getPlayerClass().cast(sender);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Registers the plugin's basic chat commands.
	 */
	default void registerChatCommands() {
		try {
			KyoriTranslator.initialize(Plugin.class.getClassLoader(), getClass().getClassLoader());
		} catch (Exception e) {
			System.out.println("Failed to initialize i18n");
			e.printStackTrace();
		}

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

		// link command
		manager.command(account.literal("link")
				.meta(CommandMeta.DESCRIPTION, "Link a Twitch account to your Minecraft account")
				.argument(
						StringArgument.<S>builder("username")
								.single()
								.asRequired()
								.manager(manager)
								.build(),
						ArgumentDescription.of("The username of the Twitch account to link")
				)
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					Audience audience = mapper.asAudience(sender);
					UUID uuid = mapper.tryGetUniqueId(sender).orElseThrow(() ->
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
						ArgumentDescription.of("The username of the Twitch account to unlink")
				)
				.handler(commandContext -> {
					String username = commandContext.get("username");
					S sender = commandContext.getSender();
					Audience audience = mapper.asAudience(sender);
					UUID uuid = mapper.tryGetUniqueId(sender).orElseThrow(() ->
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

					audience.sendMessage(output(Component.translatable("cc.command.crowdcontrol.reconnect.output")));
				}));
		// status command
		manager.command(ccCmd.literal("status")
				.meta(CommandMeta.DESCRIPTION, "Get the status of the Crowd Control service")
				.handler(commandContext -> mapper.asAudience(commandContext.getSender()).sendMessage(output(
						Component.translatable("cc.command.crowdcontrol.status." + (getCrowdControl() != null))))));
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
							ArgumentDescription.of("The username of the Twitch account to unlink")
					)
					.handler(commandContext -> {
						// TODO: allow targeting multiple players
						S sender = commandContext.getSender();
						Audience audience = mapper.asAudience(sender);
						P player = asPlayer(sender);
						if (player == null) {
							audience.sendMessage(output(Component.translatable("cc.command.cast-error", NamedTextColor.RED)));
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
		return isGlobal() || (!getHosts().isEmpty());// && getAllPlayers().stream().anyMatch(this::isHost)); TODO: I need to trigger the status update on /account if I want this
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
	 * Sends a packet with an embedded message for the C# client.
	 *
	 * @param service the service to send the packet to
	 * @param message the message to send
	 */
	default void sendEmbeddedMessagePacket(@Nullable SocketManager service, @NotNull String message) {
		if (service == null)
			service = getCrowdControl();
		if (service == null) {
			getSLF4JLogger().warn("Attempted to send embedded message packet but the service is unavailable");
			return;
		}
		final @NotNull SocketManager finalService = service;
		getScheduledExecutor().schedule(() -> {
			try {
				getSLF4JLogger().debug("sending packet {} to {}", message, finalService);
				Response response = finalService.buildResponse()
						.packetType(PacketType.EFFECT_STATUS)
						.type(ResultType.NOT_VISIBLE)
						.effect("embedded_message")
						.message(message)
						.build();
				getSLF4JLogger().debug("final packet: {}", response.toJSON());
				response.send();
			} catch (Exception e) {
				getSLF4JLogger().error("Failed to send embedded message packet", e);
			}
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
		String message = "_mc_cc_server_status_" + new Gson().toJson(commandRegister().getCommands().stream().map(Command::getEffectName).collect(Collectors.toList()));
		service.addConnectListener(connectingService -> {
			sendEmbeddedMessagePacket(connectingService, message);
			updateConditionalEffectVisibility(connectingService);
		});
	}

	/**
	 * Updates the status of an effect.
	 *
	 * @param respondable an object that can be responded to
	 * @param effect      the effect to update
	 * @param status      the new status
	 */
	default void updateEffectStatus(@Nullable Respondable respondable, @NotNull String effect, Response.@NotNull ResultType status) {
		if (!status.isStatus())
			throw new IllegalArgumentException("status must be a status type (not a result type)");
		if (respondable == null)
			return;
		getSLF4JLogger().debug("Updating status of effect {} to {}", effect, status);
		Response response = respondable.buildResponse()
				.packetType(PacketType.EFFECT_STATUS)
				.effect(effect.toLowerCase(Locale.ENGLISH))
				.type(status)
				.build();
		response.send();
	}

	/**
	 * Updates the status of an effect.
	 *
	 * @param respondable an object that can be responded to
	 * @param effect      the effect to update
	 * @param status      the new status
	 */
	default void updateEffectStatus(Respondable respondable, @NotNull Command<?> effect, Response.@NotNull ResultType status) {
		updateEffectStatus(respondable, effect.getEffectName(), status);
	}

	/**
	 * Updates the visibility of a collection of {@link Command effect} IDs.
	 *
	 * @param respondable an object that can be responded to
	 * @param effectIds   IDs of the effect to update
	 * @param visible     effects' new visibility
	 */
	default void updateEffectIdVisibility(Respondable respondable, @NotNull Collection<String> effectIds, boolean visible) {
		effectIds.forEach(effectId -> updateEffectStatus(respondable, effectId, visible ? Response.ResultType.VISIBLE : Response.ResultType.NOT_VISIBLE));
	}

	/**
	 * Updates the visibility of an {@link Command effect} ID.
	 *
	 * @param respondable an object that can be responded to
	 * @param effectId    ID of the effect to update
	 * @param visible     effect's new visibility
	 */
	default void updateEffectIdVisibility(Respondable respondable, @NotNull String effectId, boolean visible) {
		updateEffectIdVisibility(respondable, Collections.singletonList(effectId), visible);
	}

	/**
	 * Updates the visibility of a collection of {@link Command effects}.
	 *
	 * @param respondable an object that can be responded to
	 * @param effects     the effect to update
	 * @param visible     effects' new visibility
	 */
	default void updateEffectVisibility(Respondable respondable, @NotNull Collection<Command<?>> effects, boolean visible) {
		updateEffectIdVisibility(respondable, effects.stream().map(Command::getEffectName).collect(Collectors.toList()), visible);
	}

	/**
	 * Updates the visibility of an {@link Command effect}.
	 *
	 * @param respondable an object that can be responded to
	 * @param effect      the effect to update
	 * @param visible     effect's new visibility
	 */
	default void updateEffectVisibility(Respondable respondable, @NotNull Command<?> effect, boolean visible) {
		updateEffectVisibility(respondable, Collections.singletonList(effect), visible);
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
	 * @throws IllegalStateException    if the plugin is not running in {@link #isServer() server mode}
	 */
	void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException;

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
		updateEffectStatus(service, "swap", getAllPlayers().size() <= 1 ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE);
		for (Command<?> effect : commandRegister().getCommands()) {
			TriState visibility = effect.isVisible();
			if (effect.isClientOnly()) {
				if (!clientVisible)
					visibility = TriState.FALSE;
				else if (visibility == TriState.UNKNOWN)
					visibility = TriState.TRUE;
			}
			else if (effect.isGlobal()) {
				if (!globalVisible)
					visibility = TriState.FALSE;
				else if (visibility == TriState.UNKNOWN)
					visibility = TriState.TRUE;
			}
			if (visibility != TriState.UNKNOWN)
				updateEffectVisibility(service, effect, visibility.getPrimitiveBoolean());

			TriState selectable = effect.isSelectable();
			if (selectable != TriState.UNKNOWN && visibility != TriState.FALSE)
				updateEffectStatus(service, effect, selectable == TriState.TRUE ? ResultType.SELECTABLE : ResultType.NOT_SELECTABLE);
		}
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
		getScheduledExecutor().schedule(() -> {
			// ensure player is still online
			Optional<P> optPlayer = mapper.getPlayer(uuid);
			if (!optPlayer.isPresent())
				return;
			P player = optPlayer.get();
			// send messages
			Audience audience = mapper.asAudience(player);
			audience.sendMessage(JOIN_MESSAGE_1);
			//noinspection OptionalGetWithoutIsPresent
			if (!isGlobal() && isServer() && getPlayerManager().getLinkedAccounts(mapper.tryGetUniqueId(player).get()).size() == 0
					&& (!isAdminRequired() || playerMapper().isAdmin(player)))
				audience.sendMessage(JOIN_MESSAGE_2);
			if (!globalEffectsUsable())
				audience.sendMessage(NO_GLOBAL_EFFECTS_MESSAGE);
			CrowdControl cc = getCrowdControl();
			if (cc == null) {
				if (mapper.isAdmin(player)) {
					if (isServer() && getPasswordOrEmpty().equals(""))
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
		return Component.text(request.getViewer());
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
}
