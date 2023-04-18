package dev.qixils.crowdcontrol.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Utils {

	public static <T> void executeOn(@Nullable T object, @NotNull Consumer<@NotNull T> consumer) {
		if (object != null)
			consumer.accept(object);
	}
}
