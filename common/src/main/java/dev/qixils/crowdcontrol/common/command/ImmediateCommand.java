package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link Command} that executes immediately on the same thread.
 *
 * @param <P> class used to represent online players
 */
public interface ImmediateCommand<P> extends Command<P> {

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 * The resulting status of executing the command is returned.
	 *
	 * @param players players to apply the effect to
	 * @param request request that prompted the execution of this command
	 * @return {@link CompletableFuture} containing the resulting status of executing the command
	 */
	@Override
	default @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request) {
		return CompletableFuture.completedFuture(executeImmediately(players, request));
	}

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 * The resulting status of executing the command is returned.
	 *
	 * @param players players to apply the effect to
	 * @param request request that prompted the execution of this command
	 * @return resulting status of executing the command
	 */
	Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request);
}
