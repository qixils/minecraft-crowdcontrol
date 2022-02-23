package dev.qixils.crowdcontrol.plugin.sponge8.utils;

public class MinecraftMath {
	private static final double[] SIN = new double[65536];

	static {
		for (int i = 0; i < SIN.length; ++i) {
			SIN[i] = (float) Math.sin((double) i * 3.141592653589793D * 2.0D / 65536.0D);
		}
	}

	private MinecraftMath() {
		throw new IllegalStateException("Utility class cannot be instantiated");
	}

	public static double sin(double value) {
		return SIN[(int) (value * 10430.378F) & '\uffff'];
	}

	public static double cos(double value) {
		return SIN[(int) (value * 10430.378F + 16384.0F) & '\uffff'];
	}
}
