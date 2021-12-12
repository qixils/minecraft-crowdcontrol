package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public interface Command<P extends Audience> {
	@NotNull
	@CheckReturnValue
	Plugin<P, ? super P> getPlugin();

	@NotNull
	@CheckReturnValue
	CompletableFuture<Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request);

	@NotNull
	@CheckReturnValue
	String getEffectName();

	@NotNull
	@CheckReturnValue
	String getDisplayName();

	default void executeAndNotify(@NotNull Request request) {
		List<P> players = getPlugin().getPlayers(request);

		// ensure targets are online / available
		if (players.isEmpty())
			throw new NoApplicableTarget();

		execute(new ArrayList<>(players), request).thenAccept(builder -> {
			if (builder == null) return;

			Response response = builder.build();
			response.send();

			if (response.getResultType() == Response.ResultType.SUCCESS)
				announce(players, request);
		});
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	default boolean isGlobalCommandUsable(@Nullable List<P> players, @NotNull Request request) {
		Plugin<P, ? super P> plugin = getPlugin();
		if (plugin.isGlobal(request))
			return true;

		Collection<String> hosts = plugin.getHosts();
		if (hosts.isEmpty())
			return false;

		for (Target target : request.getTargets()) {
			if (hosts.contains(String.valueOf(target.getId())))
				return true;
			if (hosts.contains(target.getName().toLowerCase(Locale.ENGLISH)))
				return true;
		}

		if (players == null)
			players = plugin.getPlayers(request);

		for (P player : players) {
			String uuidStr = plugin.getUUID(player).toString().toLowerCase(Locale.ENGLISH);
			if (hosts.contains(uuidStr) || hosts.contains(uuidStr.replace("-", "")))
				return true;
			if (hosts.contains(plugin.getUsername(player).toLowerCase(Locale.ENGLISH)))
				return true;
		}

		return false;
	}

	@Deprecated
	default void announce(final Request request) {
		Plugin<?, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		announce(plugin.getPlayers(request), request);
	}

	default void announce(final Collection<? extends Audience> audiences, final Request request) {
		Plugin<?, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		announce(Audience.audience(audiences), request);
	}

	default void announce(final Audience audience, final Request request) {
		Plugin<?, ?> plugin = getPlugin();
		if (!plugin.announceEffects()) return;
		audience.sendMessage(new TextBuilder()
				.next(request.getViewer(), Plugin.USER_COLOR)
				.next(" used command ")
				.next(getProcessedDisplayName(), Plugin.CMD_COLOR)
		);
	}

	@NotNull
	@CheckReturnValue
	default String getProcessedDisplayName() {
		return getDisplayName();
	}
}
