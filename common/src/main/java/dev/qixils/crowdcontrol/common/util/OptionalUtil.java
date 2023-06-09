package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OptionalUtil {

	public static <T> Optional<T> or(@NotNull Optional<T> optional, @NotNull Optional<T> other) {
		if (optional.isPresent())
			return optional;
		return other;
	}

	public static <T> Optional<T> or(@NotNull Optional<T> optional, @NotNull Supplier<Optional<T>> other) {
		if (optional.isPresent())
			return optional;
		return other.get();
	}
}
