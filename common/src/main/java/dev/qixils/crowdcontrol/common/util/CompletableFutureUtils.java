package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Utilities for {@link CompletableFuture}s.
 */
public class CompletableFutureUtils {

	private CompletableFutureUtils() {
		throw new IllegalStateException("Cannot instantiate utility class");
	}

	/**
	 * Converts a collection of {@link CompletableFuture}s to an array.
	 *
	 * @param futures collection of {@link CompletableFuture}s
	 * @return array of {@link CompletableFuture}s
	 */
	public static CompletableFuture<?> @NotNull [] toArray(Collection<? extends CompletableFuture<?>> futures) {
		CompletableFuture<?>[] array = new CompletableFuture[futures.size()];
		int i = 0;
		for (CompletableFuture<?> future : futures) {
			array[i++] = future;
		}
		return array;
	}

	/**
	 * Returns a new CompletableFuture that is completed when all of
	 * the given CompletableFutures complete.  If any of the given
	 * CompletableFutures complete exceptionally, then the returned
	 * CompletableFuture also does so, with a CompletionException
	 * holding this exception as its cause.  Otherwise, the results,
	 * if any, of the given CompletableFutures are not reflected in
	 * the returned CompletableFuture, but may be obtained by
	 * inspecting them individually. If no CompletableFutures are
	 * provided, returns a CompletableFuture completed with the value
	 * {@code null}.
	 *
	 * <p>Among the applications of this method is to await completion
	 * of a set of independent CompletableFutures before continuing a
	 * program.</p>
	 *
	 * @param futures the CompletableFutures
	 * @return a new CompletableFuture that is completed when all of the
	 * given CompletableFutures complete
	 * @throws NullPointerException if the collection or any of its elements are
	 *                              {@code null}
	 */
	@SuppressWarnings("GrazieInspection")
	@NotNull
	public static CompletableFuture<?> allOf(@NotNull Collection<? extends @NotNull CompletableFuture<?>> futures) {
		return CompletableFuture.allOf(toArray(futures));
	}
}
