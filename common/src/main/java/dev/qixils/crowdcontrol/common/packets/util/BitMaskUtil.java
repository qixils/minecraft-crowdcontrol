package dev.qixils.crowdcontrol.common.packets.util;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class BitMaskUtil {
	public static <T extends Enum<T>> Set<T> fromBitMask(Class<T> enumClass, long mask) {
		Set<T> set = EnumSet.noneOf(enumClass);
		for (T feature : enumClass.getEnumConstants()) {
			if ((mask & (1L << feature.ordinal())) > 0L)
				set.add(feature);
		}
		return set;
	}

	public static long toBitMask(Iterable<? extends Enum<?>> set) {
		long mask = 0L;
		for (Enum<?> feature : set) {
			mask |= 1L << feature.ordinal();
		}
		return mask;
	}

	public static long toBitMask(Enum<?>... values) {
		return toBitMask(Arrays.asList(values));
	}
}
