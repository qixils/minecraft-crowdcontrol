package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

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
	public static final int CLUTTER_ITEMS = 3;
	public static final int VEIN_RADIUS = 12;
	public static final int VEIN_COUNT = 2;
	// do-or-die
	public static final Duration DO_OR_DIE_DURATION = Duration.ofSeconds(31);
	public static final Duration DO_OR_DIE_COOLDOWN = DO_OR_DIE_DURATION.multipliedBy(3);
	private static final TextColor DO_OR_DIE_START_COLOR = TextColor.color(0xE4F73D);
	private static final TextColor DO_OR_DIE_END_COLOR = TextColor.color(0xF42929);
	public static final Title.Times DO_OR_DIE_TIMES = Title.Times.of(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1));
	public static final Title DO_OR_DIE_SUCCESS = Title.title(
			Component.empty(),
			Component.text("Task Completed!").color(NamedTextColor.GREEN),
			DO_OR_DIE_TIMES
	);
	public static final Title DO_OR_DIE_FAILURE = Title.title(
			Component.empty(),
			Component.text("Task Failed").color(NamedTextColor.RED),
			DO_OR_DIE_TIMES
	);

	public static TextColor doOrDieColor(int secondsLeft) {
		return TextColor.lerp((float) secondsLeft / DO_OR_DIE_DURATION.getSeconds(), DO_OR_DIE_END_COLOR, DO_OR_DIE_START_COLOR);
	}

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

	public static Component buildLootboxTitle(Request request) {
		return new TextBuilder()
				.next(request.getViewer(), Plugin.USER_COLOR)
				.rawNext(" has gifted you...")
				.build();
	}

	public static Component buildLootboxLore(Request request) {
		return new TextBuilder("Donated by ")
				.next(request.getViewer(), Plugin.USER_COLOR, TextDecoration.ITALIC)
				.build();
	}

	@Getter
	public enum AttributeWeights implements Weighted {
		NONE(0, 167),
		ONE(1, 20),
		TWO(2, 10),
		THREE(3, 2),
		FOUR(4, 1);

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(AttributeWeights::getWeight).sum();
		private final int level;
		private final int weight;

		AttributeWeights(int level, int weight) {
			this.level = level;
			this.weight = weight;
		}
	}

	@Getter
	public enum EnchantmentWeights implements Weighted {
		ONE(1, 40),
		TWO(2, 15),
		THREE(3, 3),
		FOUR(4, 2),
		FIVE(5, 1);

		public static final int TOTAL_WEIGHTS = Arrays.stream(values()).mapToInt(EnchantmentWeights::getWeight).sum();
		private final int level;
		private final int weight;

		EnchantmentWeights(int level, int weight) {
			this.level = level;
			this.weight = weight;
		}
	}

	private CommandConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}
}
