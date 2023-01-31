package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.common.ClientOnly;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A command which handles incoming effects requested by Crowd Control server.
 *
 * @param <P> class used to represent online players
 */
public interface Command<P> {

	/**
	 * Gets the plugin that registered this command.
	 *
	 * @return owning plugin
	 */
	@NotNull
	@CheckReturnValue
	Plugin<P, ?> getPlugin();

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 * The resulting status of executing the command (or null) is returned.
	 *
	 * @param players players to apply the effect to
	 * @param request request that prompted the execution of this command
	 * @return {@link CompletableFuture} containing either the resulting status of executing the
	 * command or null
	 */
	@NotNull
	@CheckReturnValue
	CompletableFuture<@Nullable Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request);

	/**
	 * Determines if this command object listens for events dispatched by the Minecraft server API.
	 *
	 * @return if events are being listened to
	 */
	default boolean isEventListener() {
		return getClass().isAnnotationPresent(EventListener.class);
	}

	/**
	 * Gets the internal code name for an effect.
	 * It should match the name of an effect from the project's .cs file.
	 *
	 * @return internal code name
	 */
	@NotNull
	@CheckReturnValue
	String getEffectName();

	/**
	 * Gets the default display name for this command.
	 *
	 * @return default display name
	 */
	@NotNull
	@CheckReturnValue
	default TranslatableComponent getDefaultDisplayName() {
		return Component.translatable("cc.effect." + getEffectName() + ".name");
	}

	/**
	 * Gets the effect's raw display name. This is used when sending a chat message to streamers
	 * informing them of the activation of an effect.
	 *
	 * <p>Further processing may take place in the {@link #getProcessedDisplayName(Request)} method.</p>
	 *
	 * @return display name
	 */
	@NotNull
	@CheckReturnValue
	default Component getDisplayName() {
		return getDefaultDisplayName();
	}

	/**
	 * Gets the effect's processed display name. This contains the contents of
	 * {@link #getDisplayName()} and may optionally include additional information such as how long
	 * the command's effects will last.
	 *
	 * @return processed display name
	 */
	@NotNull
	@CheckReturnValue
	default Component getProcessedDisplayName(@NotNull Request request) {
		return getDisplayName();
	}

	/**
	 * {@link #execute Executes} this command and notifies its targets (if
	 * {@link Plugin#announceEffects() enabled}).
	 *
	 * @param request request that prompted the execution of this command
	 */
	default void executeAndNotify(@NotNull Request request) {
		Plugin<P, ?> plugin = getPlugin();
		plugin.getSLF4JLogger().debug("Executing " + getDisplayName());
		List<P> players = plugin.getPlayers(request);

		// ensure targets are online / available
		if (players.isEmpty())
			throw new NoApplicableTarget();

		// disallow execution of global commands
		if (getClass().isAnnotationPresent(Global.class)) {
			if (!plugin.globalEffectsUsable()) {
				request.buildResponse()
						.type(ResultType.UNAVAILABLE)
						.message("Global effects are disabled")
						.send();
				return;
			} else if (!isGlobalCommandUsable(players, request)) {
				request.buildResponse()
						.type(ResultType.UNAVAILABLE)
						.message("Global effects cannot be used on the targeted streamer")
						.send();
				return;
			}

		}

		// disallow execution of client commands
		if (isClientOnly()) {
			if (!getPlugin().supportsClientOnly()) {
				request.buildResponse()
						.type(ResultType.UNAVAILABLE)
						.message("Client-side effects are not supported by this streamer's setup")
						.send();
				return;
			} else if (!isClientAvailable(players, request)) {
				request.buildResponse()
						.type(ResultType.FAILURE)
						.message("Client-side effects are currently unavailable or cannot be used on this streamer")
						.send();
				return;
			}
		}

		// create shuffled copy of players so that the recipients of limited effects are random
		List<P> shuffledPlayers = new ArrayList<>(players);
		Collections.shuffle(shuffledPlayers);

		execute(shuffledPlayers, request).thenAccept(builder -> {
			if (builder == null) return;

			Response response = builder.build();
			response.send();

			if (response.getResultType() == Response.ResultType.SUCCESS)
				announce(plugin.playerMapper().asAudience(players), request);
		});
	}

	/**
	 * Whether a {@link ClientOnly} effect with the provided arguments would be able to successfully
	 * execute. For example, this will return false if {@link Plugin#supportsClientOnly()} returns
	 * {@code false} or if no connected players have the client mod installed.
	 *
	 * @param players list of players targeted by the effect
	 * @param request request that triggered this effect
	 * @return whether a client-only effect could execute
	 */
	default boolean isClientAvailable(@Nullable List<P> players, @NotNull Request request) {
		if (getPlugin().supportsClientOnly())
			throw new UnsupportedOperationException(
					"Plugin reports that it supports client-only effects but has no implementation for #isClientAvailable"
			);
		return false;
	}

	/**
	 * Determines if this global command is usable.
	 *
	 * @param players players being targeted by this command
	 * @param request request that prompted the execution of this command
	 * @return whether this global command is usable
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	default boolean isGlobalCommandUsable(@Nullable List<P> players, @NotNull Request request) {
		Plugin<P, ?> plugin = getPlugin();
		if (plugin.isGlobal(request))
			return true;

		Collection<String> hosts = plugin.getHosts();
		if (hosts.isEmpty())
			return false;

		for (Target target : request.getTargets()) {
			if (target.getId() != null && hosts.contains(target.getId()))
				return true;
			if (target.getName() != null && hosts.contains(target.getName().toLowerCase(Locale.ENGLISH)))
				return true;
		}

		if (players == null)
			players = plugin.getPlayers(request);

		for (P player : players) {
			if (isHost(player))
				return true;
		}

		return false;
	}

	/**
	 * Whether the specified player is known to be a server host.
	 *
	 * @param player player to check
	 * @return whether the player is a server host
	 */
	default boolean isHost(@NotNull P player) {
		Plugin<P, ?> plugin = getPlugin();
		Collection<String> hosts = plugin.getHosts();
		if (hosts.isEmpty())
			return false;

		String uuidStr = plugin.playerMapper().getUniqueId(player).toString().toLowerCase(Locale.ENGLISH);
		if (hosts.contains(uuidStr) || hosts.contains(uuidStr.replace("-", "")))
			return true;

		return hosts.contains(plugin.getUsername(player).toLowerCase(Locale.ENGLISH));
	}

	/**
	 * Announces the {@link #execute(List, Request) execution} of this command.
	 *
	 * @param request request that prompted the execution of this command
	 * @see #playerAnnounce(Collection, Request)
	 * @see #announce(Collection, Request)
	 * @see #announce(Audience, Request)
	 * @deprecated usage of {@link #playerAnnounce(Collection, Request)} is preferred
	 */
	@Deprecated
	default void announce(final @NotNull Request request) {
		Plugin<P, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		announce(plugin.playerMapper().asAudience(plugin.getPlayers(request)), request);
	}

	/**
	 * Announces the {@link #execute(List, Request) execution} of this command.
	 *
	 * @param audiences collection of audiences to render the effect announcement to
	 * @param request   request that prompted the execution of this command
	 */
	default void announce(final Collection<? extends Audience> audiences, final Request request) {
		Plugin<?, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		announce(Audience.audience(audiences), request);
	}

	/**
	 * Announces the {@link #execute(List, Request) execution} of this command.
	 *
	 * @param players collection of players to render the effect announcement to
	 * @param request request that prompted the execution of this command
	 */
	default void playerAnnounce(final Collection<P> players, final Request request) {
		Plugin<P, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		announce(plugin.playerMapper().asAudience(players), request);
	}

	/**
	 * Announces the {@link #execute(List, Request) execution} of this command.
	 *
	 * @param audience audience to render the effect announcement to
	 * @param request  request that prompted the execution of this command
	 */
	default void announce(final Audience audience, final Request request) {
		Plugin<P, ?> plugin = getPlugin();

		List<Audience> audiences = new ArrayList<>(3);
		audiences.add(plugin.getConsole());
		if (plugin.announceEffects()) {
			audiences.add(plugin.playerMapper().asAudience(plugin.getPlayerManager().getSpectators()));
			audiences.add(audience);
		}

		try {
			Audience.audience(audiences).sendMessage(Component.translatable(
					"cc.effect.used",
					plugin.getViewerComponent(request, true).color(Plugin.USER_COLOR),
					getProcessedDisplayName(request).colorIfAbsent(Plugin.CMD_COLOR)
			));
		} catch (Exception e) {
			LoggerFactory.getLogger(Command.class).warn("Failed to announce effect", e);
		}
	}

	/**
	 * Helper method which executes some code synchronously (i.e. on the server's main thread).
	 *
	 * @param runnable command to execute synchronously
	 */
	default void sync(@NotNull Runnable runnable) {
		getPlugin().getSyncExecutor().execute(runnable);
	}


	/**
	 * Helper method which executes some code asynchronously (i.e. off the server's main thread).
	 *
	 * @param runnable command to execute asynchronously
	 */
	default void async(@NotNull Runnable runnable) {
		getPlugin().getAsyncExecutor().execute(runnable);
	}

	/**
	 * Whether this command can only be applied to players with the mod installed locally.
	 *
	 * @return whether this effect is client-side
	 */
	default boolean isClientOnly() {
		return getClass().isAnnotationPresent(ClientOnly.class);
	}
}
