package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CollectionUtil {
	private static final Logger log = LoggerFactory.getLogger(CollectionUtil.class);

	@Nullable
	public static <T> T init(@NotNull Supplier<T> supplier, @Nullable Consumer<Exception> onError) {
		try {
			return supplier.get();
		} catch (Exception e) {
			if (onError != null) {
				try {
					onError.accept(e);
				} catch (Exception e2) {
					log.error("Failed to initialize {}...", supplier.getClass().getName(), e);
					log.error("...and failed to call onError", e2);
				}
			} else {
				log.error("Failed to initialize {}", supplier.getClass().getName(), e);
			}
			return null;
		}
	}

	@Nullable
	public static <T> T init(@NotNull Supplier<T> supplier) {
		return init(supplier, null);
	}

	public static <T> void initTo(@NotNull Collection<T> collection, @NotNull Supplier<? extends T> supplier, @Nullable Consumer<Exception> onError) {
		T t = init(supplier, onError);
		if (t != null)
			collection.add(t);
	}

	public static <T> void initTo(@NotNull Collection<T> collection, @NotNull Supplier<? extends T> supplier) {
		initTo(collection, supplier, null);
	}
}
