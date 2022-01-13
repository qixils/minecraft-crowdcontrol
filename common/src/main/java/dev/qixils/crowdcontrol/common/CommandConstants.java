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
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * Constant variables that are consistently used across command implementations.
 */
public class CommandConstants {

	/**
	 * The radius to search for an entity to remove during the execution of the Remove XYZ Entity
	 * command.
	 */
	public static final int REMOVE_ENTITY_RADIUS = 35;
	/**
	 * The name to apply to entities to turn them upside-down.
	 */
	public static final String DINNERBONE_NAME = "Dinnerbone";
	/**
	 * The name to apply to entities to turn them upside-down as a text component.
	 */
	public static final Component DINNERBONE_COMPONENT = Component.text(DINNERBONE_NAME);
	/**
	 * The radius to search for entities to flip upside-down.
	 */
	public static final int DINNERBONE_RADIUS = 15;
	/**
	 * The amount of blocks that a falling block should fall for the Place Falling Block command.
	 */
	public static final int FALLING_BLOCK_FALL_DISTANCE = 5;
	/**
	 * The minimum duration of weather effects in ticks.
	 */
	public static final long WEATHER_TICKS = 20 * 60 * 60;
	/**
	 * The amount of time in the day to skip from the Zip Time command.
	 */
	public static final long ZIP_TIME_TICKS = 10000; // roughly a day
	/**
	 * The amount of time to disable jumping for.
	 */
	public static final Duration DISABLE_JUMPING_DURATION = Duration.ofSeconds(10);
	/**
	 * The amount of time to disable jumping for in ticks.
	 */
	public static final long DISABLE_JUMPING_TICKS = DISABLE_JUMPING_DURATION.getSeconds() * 20;
	/**
	 * The radius to search for valid locations to place flowers for the Flower Command.
	 */
	public static final int FLOWER_RADIUS = 10;
	/**
	 * The minimum (inclusive) number of flowers to be placed by Flower Command.
	 */
	public static final int FLOWER_MIN = 14;
	/**
	 * The maximum (inclusive) number of flowers to be placed by Flower Command.
	 */
	public static final int FLOWER_MAX = 28;
	/**
	 * The radius for the X and Z axis to break blocks in the Dig Command.
	 */
	public static final double DIG_RADIUS = .5d; // TODO: should have depth as a variable too
	/**
	 * The message to display to players when Keep Inventory has been enabled for them.
	 */
	public static final Component KEEP_INVENTORY_MESSAGE = Component.text(
			"Your inventory will be kept on death",
			NamedTextColor.GREEN
	);
	/**
	 * The message to display to players when Keep Inventory has been disabled for them.
	 */
	public static final Component LOSE_INVENTORY_MESSAGE = new TextBuilder(NamedTextColor.RED)
			.next("Your inventory will &lnot&r be kept on death").build();
	/**
	 * The message to play to players when Keep Inventory has been enabled for them.
	 */
	public static final Sound KEEP_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.activate"),
			Source.MASTER,
			1f,
			1f
	);
	/**
	 * The message to play to players when Keep Inventory has been disabled for them.
	 */
	public static final Sound LOSE_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.deactivate"),
			Source.MASTER,
			1f,
			1f
	);
	/**
	 * The minimum amount of durability allowed to be set by the Damage Item command.
	 */
	private static final int MIN_ITEM_DAMAGE = 15;
	/**
	 * The minimum amount of health allowed to be set by the -1 Max Health command.
	 */
	public static final int MIN_MAX_HEALTH = 10;
	/**
	 * The amount of items that should be moved around by the Clutter Inventory command.
	 * This should be an even number.
	 */
	public static final int CLUTTER_ITEMS = 6;
	/**
	 * The radius in which blocks can be placed by the ore vein command.
	 */
	public static final int VEIN_RADIUS = 6;
	/**
	 * The amount of veins to spawn per usage of the ore vein command.
	 */
	public static final int VEIN_COUNT = 2;

	// do-or-die

	/**
	 * How long streamers should be given to complete a Do-or-Die task.
	 */
	public static final Duration DO_OR_DIE_DURATION = Duration.ofSeconds(31);
	/**
	 * How long the grace period should last for the Do-or-Die command. This prevents the command
	 * from being excessively spammed to prevent the streamer from progressing.
	 */
	public static final Duration DO_OR_DIE_COOLDOWN = DO_OR_DIE_DURATION.multipliedBy(3);
	/**
	 * The color used at the start of the countdown timer.
	 */
	private static final TextColor DO_OR_DIE_START_COLOR = TextColor.color(0xE4F73D);
	/**
	 * The color used at the end of the countdown timer.
	 */
	private static final TextColor DO_OR_DIE_END_COLOR = TextColor.color(0xF42929);
	/**
	 * How long Do-or-Die's on-screen Titles should last.
	 */
	public static final Title.Times DO_OR_DIE_TIMES = Title.Times.of(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1));
	/**
	 * Message to show to users when they complete a Do-or-Die task.
	 */
	public static final Title DO_OR_DIE_SUCCESS = Title.title(
			Component.empty(),
			Component.text("Task Completed!").color(NamedTextColor.GREEN),
			DO_OR_DIE_TIMES
	);
	/**
	 * Message to show to users when they fail a Do-or-Die task.
	 */
	public static final Title DO_OR_DIE_FAILURE = Title.title(
			Component.empty(),
			Component.text("Task Failed").color(NamedTextColor.RED),
			DO_OR_DIE_TIMES
	);

	/**
	 * Returns the text color used at the given point during the countdown.
	 *
	 * @param secondsLeft seconds until the task is failed
	 * @return text color to use for the countdown text
	 */
	public static TextColor doOrDieColor(int secondsLeft) {
		return TextColor.lerp((secondsLeft - 1f) / DO_OR_DIE_DURATION.getSeconds(), DO_OR_DIE_END_COLOR, DO_OR_DIE_START_COLOR);
	}

	// misc methods

	// TODO: make this an enum or collection of static fields; there's a lot of different types of sounds used
	// (probably two enums/collections-- one for basic sounds, and one for variable sounds like this one)

	/**
	 * Returns the sound corresponding with a spooky sound key.
	 *
	 * @param key key for a spooky sound
	 * @return {@link Sound} object
	 */
	@NotNull
	public static Sound spookySoundOf(@NotNull Key key) {
		return Sound.sound(
				key,
				Source.MASTER,
				1.75f,
				1f
		);
	}

	/**
	 * Determines whether a change to an item's durability can be applied by one of the durability
	 * commands.
	 *
	 * <p>The parameters of this method are effectively the inverse of
	 * {@link #canApplyDamage(int, int, int)}-- this method takes in remaining durability while
	 * the other takes in used up durability.</p>
	 *
	 * <p><i>The third parameter is the same for both methods.</i></p>
	 *
	 * @param oldDurability the item's original durability
	 * @param newDurability the new durability to set
	 * @param maxDurability the item's maximum durability
	 * @return {@code true} if the change can be applied
	 * @see #canApplyDurability(int, int, int)
	 */
	public static boolean canApplyDurability(int oldDurability, int newDurability, int maxDurability) {
		LoggerFactory.getLogger(CommandConstants.class).warn(oldDurability + " " + newDurability + " " + maxDurability);
		if (oldDurability == newDurability)
			return false;
		int min = Math.min(maxDurability, Math.max(MIN_ITEM_DAMAGE, maxDurability / 100));
		return newDurability >= min;
	}

	/**
	 * Determines whether a change to an item's damage can be applied by one of the durability
	 * commands.
	 *
	 * <p>The parameters of this method are effectively the inverse of
	 * {@link #canApplyDurability(int, int, int)}-- this method takes in used up durability while
	 * the other takes in remaining durability.</p>
	 *
	 * <p><i>The third parameter is the same for both methods.</i></p>
	 *
	 * @param oldDamage     the item's original amount of damage (used durability)
	 * @param newDamage     the item's new damage
	 * @param maxDurability the item's maximum durability
	 * @return {@code true} if the change can be applied
	 * @see #canApplyDurability(int, int, int)
	 */
	public static boolean canApplyDamage(int oldDamage, int newDamage, int maxDurability) {
		return canApplyDurability(maxDurability - oldDamage, maxDurability - newDamage, maxDurability);
	}

	/**
	 * Builds the title for a lootbox inventory.
	 *
	 * @param request request that caused the execution of the lootbox command
	 * @return text component for the inventory title
	 */
	public static Component buildLootboxTitle(Request request) {
		return new TextBuilder()
				.next(request.getViewer(), Plugin.USER_COLOR)
				.rawNext(" has gifted you...")
				.build();
	}

	/**
	 * Builds the lore for the item awarded by a lootbox.
	 *
	 * @param request request that caused the execution of the lootbox command
	 * @return text component for the item's lore
	 */
	public static Component buildLootboxLore(Request request) {
		return new TextBuilder("Donated by ")
				.next(request.getViewer(), Plugin.USER_COLOR, TextDecoration.ITALIC)
				.build();
	}

	/**
	 * Weights used for randomly determining how many random attributes should be applied to the
	 * random item generated by the Lootbox Command.
	 */
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

	/**
	 * Weights used for randomly determining how many random enchantments should be applied to the
	 * random item generated by the Lootbox Command.
	 */
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
