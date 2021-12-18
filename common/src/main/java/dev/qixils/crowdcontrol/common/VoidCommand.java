package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface VoidCommand<P> extends Command<P> {
	@Override
	default @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull P> players, @NotNull Request request) {
		voidExecute(players, request);
		return CompletableFuture.completedFuture(null);
	}

	void voidExecute(@NotNull List<@NotNull P> players, @NotNull Request request);
}
