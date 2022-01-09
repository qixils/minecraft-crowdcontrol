package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

public class CommandConstants {
	public static final int REMOVE_ENTITY_RADIUS = 35;
	public static final String DINNERBONE_NAME = "Dinnerbone";
	public static final int DINNERBONE_RADIUS = 15;
	public static final int FALLING_BLOCK_FALL_DISTANCE = 5;
	public static final long WEATHER_TICKS = 20 * 60 * 60;
	public static final long ZIP_TIME_TICKS = 10000; // roughly a day

	public static Sound spookySoundOf(Key key) {
		return Sound.sound(
				key,
				Source.MASTER,
				1.75f,
				1f
		);
	}

	private CommandConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}
