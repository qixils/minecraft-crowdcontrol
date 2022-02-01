package dev.qixils.crowdcontrol.plugin.sponge8.utils;

// TODO: remove?
public class MinecraftMath {
	private static final double[] SIN = new double[65536];

	static {
		for (int i = 0; i < SIN.length; ++i) {
			SIN[i] = Math.sin((double) i * Math.PI * 2d / 65536d);
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
