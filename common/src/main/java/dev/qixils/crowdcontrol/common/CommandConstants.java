package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class CommandConstants {
	public static final int REMOVE_ENTITY_RADIUS = 35;
	public static final String DINNERBONE_NAME = "Dinnerbone";
	public static final int DINNERBONE_RADIUS = 15;
	public static final int FALLING_BLOCK_FALL_DISTANCE = 5;
	public static final long WEATHER_TICKS = 20 * 60 * 60;
	public static final long ZIP_TIME_TICKS = 10000; // roughly a day
	public static final Duration DISABLE_JUMPING_DURATION = Duration.ofSeconds(10);
	public static final long DISABLE_JUMPING_TICKS = DISABLE_JUMPING_DURATION.getSeconds() * 20;
	public static final int FLOWER_RADIUS = 10;
	public static final int FLOWER_MIN = 14;
	public static final int FLOWER_MAX = 28;
	public static final double DIG_RADIUS = .5d;
	public static final Component KEEP_INVENTORY_MESSAGE = Component.text(
			"Your inventory will be kept on death",
			NamedTextColor.GREEN
	);
	public static final Component LOSE_INVENTORY_MESSAGE = new TextBuilder(NamedTextColor.RED)
			.next("Your inventory will &lnot&r be kept on death").build();
	public static final Sound KEEP_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.activate"),
			Source.MASTER,
			1f,
			1f
	);
	public static final Sound LOSE_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.deactivate"),
			Source.MASTER,
			1f,
			1f
	);
	private static final int MIN_ITEM_DAMAGE = 15;
	public static final int MIN_MAX_HEALTH = 10;

	public static Sound spookySoundOf(Key key) {
		return Sound.sound(
				key,
				Source.MASTER,
				1.75f,
				1f
		);
	}

	public static boolean canApplyDurability(int oldDurability, int newDurability, int maxDurability) {
		LoggerFactory.getLogger(CommandConstants.class).warn(oldDurability + " " + newDurability + " " + maxDurability);
		if (oldDurability == newDurability)
			return false;
		int min = Math.min(maxDurability, Math.max(MIN_ITEM_DAMAGE, maxDurability / 100));
		return newDurability >= min;
	}

	public static boolean canApplyDamage(int oldDamage, int newDamage, int maxDurability) {
		return canApplyDurability(maxDurability - oldDamage, maxDurability - newDamage, maxDurability);
	}

	private CommandConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}
