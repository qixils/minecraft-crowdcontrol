package dev.qixils.crowdcontrol.plugin.utils;

import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public record PlayerListWrapper(
		Request request,
		Consumer<List<Player>> consumer
) implements BiConsumer<List<Player>, Throwable> {

	private static final Logger logger = Logger.getLogger("MC-CC-PlayerListWrapper");

	private static boolean isCause(@NotNull Class<? extends Throwable> potentialCause, @Nullable Throwable exception) {
		if (exception == null) return false;
		if (potentialCause.isInstance(exception)) return true;
		return isCause(potentialCause, exception.getCause());
	}

	@Override
	public void accept(@Nullable List<@NotNull Player> players, @Nullable Throwable exception) {
		if (exception != null) {
			if (!isCause(NoApplicableTarget.class, exception))
				logger.log(Level.WARNING, "Caught unknown error", exception);
			sendErrorResponse();
		}
		// players cannot be null here. this is just shutting up IntelliJ
		else if (players == null || players.isEmpty()) {
			sendErrorResponse();
		} else {
			consumer.accept(players);
		}
	}

	private void sendErrorResponse() {
		request.buildResponse().type(ResultType.RETRY).message("Effect targets unavailable").build().send();
	}
}
