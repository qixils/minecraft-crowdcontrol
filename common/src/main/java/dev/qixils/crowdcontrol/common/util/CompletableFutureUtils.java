package dev.qixils.crowdcontrol.common.util;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureUtils {
	private CompletableFutureUtils() {
		throw new IllegalStateException("Cannot instantiate utility class");
	}

	public static CompletableFuture<?>[] toArray(Collection<CompletableFuture<?>> futures) {
		CompletableFuture<?>[] array = new CompletableFuture[futures.size()];
		int i = 0;
		for (CompletableFuture<?> future : futures) {
			array[i++] = future;
		}
		return array;
	}

	public static CompletableFuture<?> allOf(Collection<CompletableFuture<?>> futures) {
		return CompletableFuture.allOf(toArray(futures));
	}
}
