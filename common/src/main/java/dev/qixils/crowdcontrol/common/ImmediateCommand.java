package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ImmediateCommand<P> extends Command<P> {
	@Override
	default @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request) {
		return CompletableFuture.completedFuture(executeImmediately(players, request));
	}

	Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request);
}
