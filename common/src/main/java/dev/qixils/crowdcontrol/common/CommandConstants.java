package dev.qixils.crowdcontrol.common;

import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.TextBuilder;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

/**
 * Constant variables that are consistently used across command implementations.
 */
public class CommandConstants {

	/**
	 * The default validator which ensures that a given sound is available.
	 * This should be set when the plugin is enabled.
	 */
	public static Predicate<Key> SOUND_VALIDATOR = null;
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
	public static final double DIG_RADIUS = .5d;
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
	/**
	 * How long the freeze commands should last.
	 */
	public static final Duration FREEZE_DURATION = Duration.ofSeconds(10);

	// tags

	/**
	 * Collection of blocks to be used in the Place Block command.
	 */
	public static final KeyedTag SET_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "tnt"),
			Key.key(MINECRAFT_NAMESPACE, "fire"),
			Key.key(MINECRAFT_NAMESPACE, "cobweb"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wither_rose"),
			Key.key(MINECRAFT_NAMESPACE, "lightning_rod"),
			Key.key(MINECRAFT_NAMESPACE, "bedrock")
	);

	/**
	 * Collection of blocks to be used in the Place Falling Block command.
	 */
	public static final KeyedTag SET_FALLING_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "anvil"),
			Key.key(MINECRAFT_NAMESPACE, "sand"),
			Key.key(MINECRAFT_NAMESPACE, "red_sand"),
			Key.key(MINECRAFT_NAMESPACE, "gravel")
	);

	/**
	 * Collection of items to be used in the Give Item and Take Item commands.
	 */
	public static final KeyedTag GIVE_TAKE_ITEMS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "wooden_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "stone_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "golden_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "iron_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "diamond_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "netherite_pickaxe"),
			Key.key(MINECRAFT_NAMESPACE, "golden_apple"),
			Key.key(MINECRAFT_NAMESPACE, "enchanted_golden_apple"),
			Key.key(MINECRAFT_NAMESPACE, "ender_eye"),
			Key.key(MINECRAFT_NAMESPACE, "end_portal_frame"),
			Key.key(MINECRAFT_NAMESPACE, "elytra"),
			Key.key(MINECRAFT_NAMESPACE, "iron_ingot"),
			Key.key(MINECRAFT_NAMESPACE, "gold_ingot"),
			Key.key(MINECRAFT_NAMESPACE, "diamond"),
			Key.key(MINECRAFT_NAMESPACE, "netherite_ingot")
	);

	/**
	 * Collection of entities that are safe to summon into the world.
	 */
	public static final KeyedTag SAFE_ENTITIES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "armor_stand"),
			Key.key(MINECRAFT_NAMESPACE, "axolotl"),
			Key.key(MINECRAFT_NAMESPACE, "bat"),
			Key.key(MINECRAFT_NAMESPACE, "bee"),
			Key.key(MINECRAFT_NAMESPACE, "blaze"),
			Key.key(MINECRAFT_NAMESPACE, "boat"),
			Key.key(MINECRAFT_NAMESPACE, "cat"),
			Key.key(MINECRAFT_NAMESPACE, "cave_spider"),
			Key.key(MINECRAFT_NAMESPACE, "chicken"),
			Key.key(MINECRAFT_NAMESPACE, "cod"),
			Key.key(MINECRAFT_NAMESPACE, "cow"),
			Key.key(MINECRAFT_NAMESPACE, "creeper"),
			Key.key(MINECRAFT_NAMESPACE, "dolphin"),
			Key.key(MINECRAFT_NAMESPACE, "donkey"),
			Key.key(MINECRAFT_NAMESPACE, "drowned"),
			Key.key(MINECRAFT_NAMESPACE, "elder_guardian"),
			Key.key(MINECRAFT_NAMESPACE, "ender_dragon"),
			Key.key(MINECRAFT_NAMESPACE, "enderman"),
			Key.key(MINECRAFT_NAMESPACE, "endermite"),
			Key.key(MINECRAFT_NAMESPACE, "evoker"),
			Key.key(MINECRAFT_NAMESPACE, "fox"),
			Key.key(MINECRAFT_NAMESPACE, "ghast"),
			Key.key(MINECRAFT_NAMESPACE, "giant"),
			Key.key(MINECRAFT_NAMESPACE, "glow_squid"),
			Key.key(MINECRAFT_NAMESPACE, "goat"),
			Key.key(MINECRAFT_NAMESPACE, "guardian"),
			Key.key(MINECRAFT_NAMESPACE, "hoglin"),
			Key.key(MINECRAFT_NAMESPACE, "horse"),
			Key.key(MINECRAFT_NAMESPACE, "husk"),
			Key.key(MINECRAFT_NAMESPACE, "illusioner"),
			Key.key(MINECRAFT_NAMESPACE, "iron_golem"),
			Key.key(MINECRAFT_NAMESPACE, "lightning"),
			Key.key(MINECRAFT_NAMESPACE, "llama"),
			Key.key(MINECRAFT_NAMESPACE, "magma_cube"),
			Key.key(MINECRAFT_NAMESPACE, "minecart"),
			Key.key(MINECRAFT_NAMESPACE, "minecart_chest"),
			Key.key(MINECRAFT_NAMESPACE, "minecart_furnace"),
			Key.key(MINECRAFT_NAMESPACE, "minecart_hopper"),
			Key.key(MINECRAFT_NAMESPACE, "minecart_tnt"),
			Key.key(MINECRAFT_NAMESPACE, "mule"),
			Key.key(MINECRAFT_NAMESPACE, "mushroom_cow"),
			Key.key(MINECRAFT_NAMESPACE, "ocelot"),
			Key.key(MINECRAFT_NAMESPACE, "panda"),
			Key.key(MINECRAFT_NAMESPACE, "parrot"),
			Key.key(MINECRAFT_NAMESPACE, "phantom"),
			Key.key(MINECRAFT_NAMESPACE, "pig"),
			Key.key(MINECRAFT_NAMESPACE, "piglin"),
			Key.key(MINECRAFT_NAMESPACE, "piglin_brute"),
			Key.key(MINECRAFT_NAMESPACE, "pillager"),
			Key.key(MINECRAFT_NAMESPACE, "polar_bear"),
			Key.key(MINECRAFT_NAMESPACE, "primed_tnt"),
			Key.key(MINECRAFT_NAMESPACE, "pufferfish"),
			Key.key(MINECRAFT_NAMESPACE, "rabbit"),
			Key.key(MINECRAFT_NAMESPACE, "ravager"),
			Key.key(MINECRAFT_NAMESPACE, "salmon"),
			Key.key(MINECRAFT_NAMESPACE, "sheep"),
			Key.key(MINECRAFT_NAMESPACE, "shulker"),
			Key.key(MINECRAFT_NAMESPACE, "silverfish"),
			Key.key(MINECRAFT_NAMESPACE, "skeleton"),
			Key.key(MINECRAFT_NAMESPACE, "skeleton_horse"),
			Key.key(MINECRAFT_NAMESPACE, "slime"),
			Key.key(MINECRAFT_NAMESPACE, "snowman"),
			Key.key(MINECRAFT_NAMESPACE, "spider"),
			Key.key(MINECRAFT_NAMESPACE, "squid"),
			Key.key(MINECRAFT_NAMESPACE, "stray"),
			Key.key(MINECRAFT_NAMESPACE, "strider"),
			Key.key(MINECRAFT_NAMESPACE, "trader_llama"),
			Key.key(MINECRAFT_NAMESPACE, "tropical_fish"),
			Key.key(MINECRAFT_NAMESPACE, "turtle"),
			Key.key(MINECRAFT_NAMESPACE, "vex"),
			Key.key(MINECRAFT_NAMESPACE, "villager"),
			Key.key(MINECRAFT_NAMESPACE, "vindicator"),
			Key.key(MINECRAFT_NAMESPACE, "wandering_trader"),
			Key.key(MINECRAFT_NAMESPACE, "witch"),
			Key.key(MINECRAFT_NAMESPACE, "wither"),
			Key.key(MINECRAFT_NAMESPACE, "wither_skeleton"),
			Key.key(MINECRAFT_NAMESPACE, "wolf"),
			Key.key(MINECRAFT_NAMESPACE, "zoglin"),
			Key.key(MINECRAFT_NAMESPACE, "zombie"),
			Key.key(MINECRAFT_NAMESPACE, "zombie_horse"),
			Key.key(MINECRAFT_NAMESPACE, "zombie_villager"),
			Key.key(MINECRAFT_NAMESPACE, "zombified_piglin")
	);

	/**
	 * Collection of flower blocks.
	 */
	public static final KeyedTag FLOWERS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "poppy"),
			Key.key(MINECRAFT_NAMESPACE, "dandelion"),
			Key.key(MINECRAFT_NAMESPACE, "blue_orchid"),
			Key.key(MINECRAFT_NAMESPACE, "allium"),
			Key.key(MINECRAFT_NAMESPACE, "azure_bluet"),
			Key.key(MINECRAFT_NAMESPACE, "orange_tulip"),
			Key.key(MINECRAFT_NAMESPACE, "red_tulip"),
			Key.key(MINECRAFT_NAMESPACE, "pink_tulip"),
			Key.key(MINECRAFT_NAMESPACE, "white_tulip"),
			Key.key(MINECRAFT_NAMESPACE, "oxeye_daisy"),
			Key.key(MINECRAFT_NAMESPACE, "cornflower"),
			Key.key(MINECRAFT_NAMESPACE, "lily_of_the_valley"),
			Key.key(MINECRAFT_NAMESPACE, "wither_rose"),
			// legacy pre-1.12
			Key.key(MINECRAFT_NAMESPACE, "red_flower"),
			Key.key(MINECRAFT_NAMESPACE, "yellow_flower")
	);

	/**
	 * Collection of torch blocks.
	 */
	public static final KeyedTag TORCHES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_wall_torch")
	);

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
	 * Color used for the subtitle of the Do-or-Die success message.
	 */
	private static final TextColor SUCCESS_SUBTITLE_COLOR = TextColor.color(0x99ff99);
	/**
	 * Message to show to users when they complete a Do-or-Die task.
	 */
	@Deprecated
	@ScheduledForRemoval
	public static final Title DO_OR_DIE_SUCCESS = Title.title(
			Component.text("Task Completed!").color(NamedTextColor.GREEN),
			Component.empty(),
			DO_OR_DIE_TIMES
	);
	/**
	 * Message to show to users when they fail a Do-or-Die task.
	 */
	public static final Title DO_OR_DIE_FAILURE = Title.title(
			Component.text("Task Failed").color(NamedTextColor.RED),
			Component.empty(),
			DO_OR_DIE_TIMES
	);

	/**
	 * Creates the title to display to a user upon the completion of a Do-or-Die task.
	 * Includes the name of the random item they won for completing the task.
	 *
	 * @param rewardItem item that the user is being rewarded with
	 * @return title to display
	 */
	@NotNull
	public static Title doOrDieSuccess(Translatable rewardItem) {
		return doOrDieSuccess(Component.translatable(rewardItem));
	}

	/**
	 * Creates the title to display to a user upon the completion of a Do-or-Die task.
	 * Includes the name of the random item they won for completing the task.
	 *
	 * @param rewardItem item that the user is being rewarded with
	 * @return title to display
	 */
	@NotNull
	public static Title doOrDieSuccess(@NotNull Component rewardItem) {
		return Title.title(
				Component.text("Task Completed!").color(NamedTextColor.GREEN),
				Component.text("You have been rewarded with ", SUCCESS_SUBTITLE_COLOR)
						.append(rewardItem),
				DO_OR_DIE_TIMES
		);
	}

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
	 * Gets the number of blocks that should be dug down from using the Dig Command as a negative
	 * integer.
	 *
	 * @return depth to dig as a random negative integer
	 */
	public static int getDigDepth() {
		return -(3 + RandomUtil.RNG.nextInt(5));
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
