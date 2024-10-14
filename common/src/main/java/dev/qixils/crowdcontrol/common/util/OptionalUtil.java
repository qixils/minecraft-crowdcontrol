package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OptionalUtil {

	public static <T> Stream<T> stream(Optional<T> optional) {
		return optional.map(Stream::of).orElseGet(Stream::empty);
	}

	@SafeVarargs
	public static <T> Optional<T> or(@NotNull Optional<T>... optionals) {
		for (Optional<T> optional : optionals) {
			if (optional.isPresent())
				return optional;
		}
		return Optional.empty();
	}

	@SafeVarargs
	public static <T> Optional<T> or(@NotNull Supplier<Optional<T>>... optionals) {
		for (Supplier<Optional<T>> optional : optionals) {
			Optional<T> result = optional.get();
			if (result.isPresent())
				return result;
		}
		return Optional.empty();
	}
}
