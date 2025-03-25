package dev.qixils.crowdcontrol.common.util;

import java.util.Arrays;

public class ArrayUtil {
	public static <T> T[] concat(T[] first, T[]... others) {
		if (others.length == 0) return first;
		int length = first.length + Arrays.stream(others).mapToInt(arr -> arr.length).sum();
		T[] newArr = Arrays.copyOfRange(first, 0, length);
		int i = first.length;
		for (T[] other : others) {
			for (T obj : other) {
				newArr[i++] = obj;
			}
		}
		return newArr;
	}
}
