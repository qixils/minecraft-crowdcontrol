package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link Command} that does not return a
 * {@link dev.qixils.crowdcontrol.socket.Response.Builder result}. It is assumed that the command
 * will manually call {@link dev.qixils.crowdcontrol.socket.Response#send()}.
 *
 * @param <P> class used to represent online players
 */
public interface VoidCommand<P> extends Command<P> {

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 *
	 * @param players players to apply the effect to
	 * @param request request that prompted the execution of this command
	 * @return an empty {@link CompletableFuture}
	 */
	@Override
	default @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request) {
		voidExecute(players, request);
		return CompletableFuture.completedFuture(null);
	}

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 *
	 * @param players players to apply the effect to
	 * @param request request that prompted the execution of this command
	 */
	void voidExecute(@NotNull List<@NotNull P> players, @NotNull Request request);
}
