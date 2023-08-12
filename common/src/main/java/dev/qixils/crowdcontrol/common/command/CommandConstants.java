package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.KeyedTag;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.Weighted;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static dev.qixils.crowdcontrol.common.util.RandomUtil.RNG;
import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

/**
 * Constant variables that are consistently used across command implementations.
 */
public class CommandConstants {

	/**
	 * The default validator which ensures that a given sound is available.
	 * This should be set when the plugin is enabled.
	 */
	public static @Nullable Predicate<Key> SOUND_VALIDATOR = null;
	/**
	 * The radius to search for an entity to remove during the execution of the Remove XYZ Entity
	 * command.
	 */
	public static final int REMOVE_ENTITY_RADIUS = 35;
	/**
	 * The name to apply to entities to turn them upside-down.
	 */
	public static final @NotNull String DINNERBONE_NAME = "Dinnerbone";
	/**
	 * The name to apply to entities to turn them upside-down as a text component.
	 */
	public static final @NotNull Component DINNERBONE_COMPONENT = Component.text(DINNERBONE_NAME);
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
	 * The amount of time to disable jumping for.
	 */
	public static final @NotNull Duration DISABLE_JUMPING_DURATION = Duration.ofSeconds(10);
	/**
	 * The amount of time to disable jumping for in ticks.
	 */
	public static final long DISABLE_JUMPING_TICKS = DISABLE_JUMPING_DURATION.getSeconds() * 20;
	/**
	 * The amount of time to invert controls for.
	 */
	public static final @NotNull Duration INVERT_CONTROLS_DURATION = Duration.ofSeconds(15);
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
	public static final @NotNull Component KEEP_INVENTORY_MESSAGE = Component.translatable(
			"cc.effect.keep_inventory_on.output",
			NamedTextColor.GREEN
	);
	/**
	 * The message to display to players when Keep Inventory has been disabled for them.
	 */
	public static final @NotNull Component LOSE_INVENTORY_MESSAGE = Component.translatable(
			"cc.effect.keep_inventory_off.output",
			NamedTextColor.RED
	);
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
	public static final @NotNull Duration FREEZE_DURATION = Duration.ofSeconds(10);
	/**
	 * Collection of blocks to be used in the Place Block command.
	 */
	public static final @NotNull KeyedTag SET_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "tnt"),
			Key.key(MINECRAFT_NAMESPACE, "fire"),
			Key.key(MINECRAFT_NAMESPACE, "cobweb"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wither_rose"),
			Key.key(MINECRAFT_NAMESPACE, "lightning_rod"),
			Key.key(MINECRAFT_NAMESPACE, "bedrock"),
			Key.key(MINECRAFT_NAMESPACE, "water"),
			Key.key(MINECRAFT_NAMESPACE, "sculk_catalyst")
	);
	/**
	 * Collection of blocks to be used in the Place Falling Block command.
	 */
	public static final @NotNull KeyedTag SET_FALLING_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "anvil"),
			Key.key(MINECRAFT_NAMESPACE, "sand"),
			Key.key(MINECRAFT_NAMESPACE, "red_sand"),
			Key.key(MINECRAFT_NAMESPACE, "gravel")
	);
	/**
	 * The starting denominator for odds when randomly giving armor to summoned entities. A value
	 * of 4 means that the odds will begin as a 1 in 4 chance of applying a given armor piece.
	 *
	 * @see #ENTITY_ARMOR_INC
	 */
	public static final int ENTITY_ARMOR_START = 4;
	/**
	 * The amount to increment the denominator for odds when randomly giving armor to summoned
	 * entities.
	 *
	 * @see #ENTITY_ARMOR_START
	 */
	public static final int ENTITY_ARMOR_INC = 2;
	/**
	 * How long potion effects given by the potion command should last in seconds.
	 */
	public static final int POTION_SECONDS = 20;
	/**
	 * How long potion effects given by the potion command should last as a {@link Duration}.
	 */
	public static final @NotNull Duration POTION_DURATION = Duration.ofSeconds(POTION_SECONDS);
	/**
	 * The time in ticks that represents the start of the day.
	 */
	public static final long DAY = 1000;
	/**
	 * The time in ticks that represents the start of the night.
	 */
	public static final long NIGHT = 13000;
	/**
	 * The minimum amount of durability allowed to be set by the Damage Item command.
	 */
	private static final int MIN_ITEM_DAMAGE_VALUE = 15;
	/**
	 * The percentage of durability to take from an item when using the Damage Item command as a denominator
	 * (i.e. {@code durability - (maxDurability / DAMAGE_ITEM_PERCENTAGE)}).
	 */
	public static final int ITEM_DAMAGE_PERCENTAGE = 4;
	/**
	 * The minimum percentage of durability allowed to be set by the Damage Item command as a denominator.
	 */
	public static final int MIN_ITEM_DAMAGE_PERCENTAGE = 5;
	/**
	 * Radius to search for entities when executing Entity Chaos with global effects disabled.
	 */
	public static final int CHAOS_LOCAL_RADIUS = 20;
	/**
	 * The minimum amount of blocks to teleport a player up by when executing the Bucket Clutch command.
	 */
	public static final int BUCKET_CLUTCH_MIN = 30;
	/**
	 * The maximum amount of blocks to teleport a player up by when executing the Bucket Clutch command.
	 */
	public static final int BUCKET_CLUTCH_MAX = 100;
	/**
	 * The title to use in the annoying pop-up menu.
	 */
	public static final Component POPUP_TITLE = Component.translatable("cc.effect.toast.title");

	// tags | TODO: probably deprecate these to allow backport mods?

	/**
	 * Collection of items to be used in the Give Item and Take Item commands.
	 */
	public static final @NotNull KeyedTag GIVE_TAKE_ITEMS = new KeyedTag(
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
			Key.key(MINECRAFT_NAMESPACE, "trident"),
			Key.key(MINECRAFT_NAMESPACE, "coal"),
			Key.key(MINECRAFT_NAMESPACE, "iron_ingot"),
			Key.key(MINECRAFT_NAMESPACE, "gold_ingot"),
			Key.key(MINECRAFT_NAMESPACE, "diamond"),
			Key.key(MINECRAFT_NAMESPACE, "netherite_ingot"),
			Key.key(MINECRAFT_NAMESPACE, "wooden_sword"),
			Key.key(MINECRAFT_NAMESPACE, "stone_sword"),
			Key.key(MINECRAFT_NAMESPACE, "golden_sword"),
			Key.key(MINECRAFT_NAMESPACE, "iron_sword"),
			Key.key(MINECRAFT_NAMESPACE, "diamond_sword"),
			Key.key(MINECRAFT_NAMESPACE, "netherite_sword"),
			Key.key(MINECRAFT_NAMESPACE, "cooked_porkchop"),
			Key.key(MINECRAFT_NAMESPACE, "recovery_compass")
	);
	/**
	 * Collection of flower blocks.
	 */
	public static final @NotNull KeyedTag FLOWERS = new KeyedTag(
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
			Key.key(MINECRAFT_NAMESPACE, "brown_mushroom"),
			Key.key(MINECRAFT_NAMESPACE, "red_mushroom"),
			Key.key(MINECRAFT_NAMESPACE, "crimson_fungus"),
			Key.key(MINECRAFT_NAMESPACE, "warped_fungus"),
			// legacy pre-1.12
			Key.key(MINECRAFT_NAMESPACE, "red_flower"),
			Key.key(MINECRAFT_NAMESPACE, "yellow_flower")
	);
	/**
	 * Collection of torch blocks.
	 */
	public static final @NotNull KeyedTag TORCHES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_wall_torch")
	);

	/**
	 * Collection of whitelisted spawnable entities.
	 * These names generally match those of the {@code EntityType} enum from Bukkit.
	 */
	public static final @NotNull Set<String> ENTITIES = new HashSet<>(Arrays.asList(
		"allay",
		"armor_stand",
		"axolotl",
		"bat",
		"bee",
		"blaze",
		"boat",
		"chest_boat",
		"camel",
		"cat",
		"cave_spider",
		"charged_creeper",
		"chicken",
		"cod",
		"cow",
		"creeper",
		"dolphin",
		"donkey",
		"drowned",
		"elder_guardian",
		"ender_dragon",
		"enderman",
		"endermite",
		"evoker",
		"fox",
		"frog",
		"ghast",
		"giant",
		"glow_squid",
		"goat",
		"guardian",
		"hoglin",
		"horse",
		"husk",
		"illusioner",
		"iron_golem",
		"lightning",
		"llama",
		"magma_cube",
		"minecart",
		"minecart_chest",
		"mushroom_cow",
		"mule",
		"ocelot",
		"panda",
		"parrot",
		"phantom",
		"pig",
		"piglin",
		"piglin_brute",
		"pillager",
		"polar_bear",
		"primed_tnt",
		"pufferfish",
		"rabbit",
		"ravager",
		"salmon",
		"sheep",
		"shulker",
		"silverfish",
		"skeleton",
		"skeleton_horse",
		"slime",
		"sniffer",
		"snowman",
		"spider",
		"squid",
		"stray",
		"strider",
		"tadpole",
		"trader_llama",
		"tropical_fish",
		"turtle",
		"vex",
		"villager",
		"vindicator",
		"wandering_trader",
		"warden",
		"witch",
		"wither",
		"wither_skeleton",
		"wolf",
		"zoglin",
		"zombie",
		"zombie_horse",
		"zombie_villager",
		"zombified_piglin"
	));

	// do-or-die
	/**
	 * How long streamers should be given to complete a Do-or-Die task.
	 */
	public static final @NotNull Duration DO_OR_DIE_DURATION = Duration.ofSeconds(31);
	/**
	 * How long the grace period should last for the Do-or-Die command. This prevents the command
	 * from being excessively spammed to prevent the streamer from progressing.
	 */
	public static final @NotNull Duration DO_OR_DIE_COOLDOWN = DO_OR_DIE_DURATION.plusSeconds(5);
	/**
	 * How long Do-or-Die's on-screen Titles should last.
	 */
	public static final Title.@NotNull Times DO_OR_DIE_TIMES = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1));
	/**
	 * Message to show to users when they fail a Do-or-Die task.
	 */
	public static final @NotNull Title DO_OR_DIE_FAILURE = Title.title(
			Component.translatable("cc.effect.do_or_die.failure", NamedTextColor.RED),
			Component.empty(),
			DO_OR_DIE_TIMES
	);
	/**
	 * The color used at the start of the countdown timer.
	 */
	private static final @NotNull TextColor DO_OR_DIE_START_COLOR = TextColor.color(0xE4F73D);
	/**
	 * The color used at the end of the countdown timer.
	 */
	private static final @NotNull TextColor DO_OR_DIE_END_COLOR = TextColor.color(0xF42929);
	/**
	 * Color used for the subtitle of the Do-or-Die success message.
	 */
	private static final @NotNull TextColor SUCCESS_SUBTITLE_COLOR = TextColor.color(0x99ff99);
	/**
	 * The minimum health that a player must have to be able to apply the Half Health effect.
	 */
	public static final double HALVE_HEALTH_MIN_HEALTH = 6; // 3 hearts
	/**
	 * The maximum search radius for locating biomes.
	 */
	public static final int BIOME_SEARCH_RADIUS = 512;
	/**
	 * The step size for locating biomes.
	 * A value of 1 will search every block, while a value of 32 will search every 32 blocks.
	 */
	public static final int BIOME_SEARCH_STEP = 32;
	/**
	 * The maximum search radius in chunks for locating structures.
	 */
	public static final int STRUCTURE_SEARCH_RADIUS = 64;
	/**
	 * Whether to search only for unexplored structures.
	 */
	public static final boolean STRUCTURE_SEARCH_UNEXPLORED = false;
	/**
	 * The chance of a summoned mushroom cow being brown as a percentage from 0 to 1.
	 */
	public static final double MUSHROOM_COW_BROWN_CHANCE = 0.01;

	private CommandConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	/**
	 * Creates the title to display to a user upon the completion of a Do-or-Die task.
	 * Includes the name of the random item they won for completing the task.
	 *
	 * @param rewardItem item that the user is being rewarded with
	 * @return title to display
	 */
	@NotNull
	public static Title doOrDieSuccess(@NotNull Translatable rewardItem) {
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
	public static Title doOrDieSuccess(@NotNull ComponentLike rewardItem) {
		return Title.title(
				Component.translatable("cc.effect.do_or_die.title").color(NamedTextColor.GREEN),
				Component.translatable("cc.effect.do_or_die.subtitle", SUCCESS_SUBTITLE_COLOR, rewardItem),
				DO_OR_DIE_TIMES
		);
	}

	// misc methods

	/**
	 * Returns the text color used at the given point during the countdown.
	 *
	 * @param secondsLeft seconds until the task is failed
	 * @return text color to use for the countdown text
	 */
	public static TextColor doOrDieColor(int secondsLeft) {
		return TextColor.lerp((secondsLeft - 1f) / DO_OR_DIE_DURATION.getSeconds(), DO_OR_DIE_END_COLOR, DO_OR_DIE_START_COLOR);
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
		if (oldDurability == newDurability)
			return false;
		if (maxDurability == newDurability)
			return true;
		int min = Math.max(MIN_ITEM_DAMAGE_VALUE, maxDurability / MIN_ITEM_DAMAGE_PERCENTAGE);
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
	 * @param plugin  the plugin instance
	 * @param request request that caused the execution of the lootbox command
	 * @return text component for the inventory title
	 */
	public static Component buildLootboxTitle(@NotNull Plugin<?, ?> plugin, @NotNull Request request) {
		return Component.translatable(
				"cc.effect.lootbox.title",
				plugin.getViewerComponent(request, false).color(Plugin.USER_COLOR)
		);
	}

	/**
	 * Builds the lore for the item awarded by a lootbox.
	 *
	 * @param plugin  the plugin instance
	 * @param request request that caused the execution of the lootbox command
	 * @return text component for the item's lore
	 */
	public static Component buildLootboxLore(@NotNull Plugin<?, ?> plugin, @NotNull Request request) {
		return Component.translatable(
				"cc.effect.lootbox.lore",
				plugin.getViewerComponent(request, false).color(Plugin.USER_COLOR).decorate(TextDecoration.ITALIC)
		);
	}

	/**
	 * Gets the number of blocks that should be dug down from using the Dig Command as a negative
	 * integer.
	 *
	 * @return depth to dig as a random negative integer
	 */
	public static int getDigDepth() {
		return -(3 + RNG.nextInt(5));
	}

	/**
	 * The power of the explosion that should be used in the "Explode" command.
	 * This returns a random positive double that tends towards 4 (TNT strength) but can be any
	 * value between 2.5 (a little less than a creeper; inclusive)
	 * and 7 (wither skull spawn; exclusive).
	 *
	 * @return random value between [2.5,7) that tends towards 4
	 */
	public static double explosionPower() {
		return Math.max(
				Math.round(RNG.doubles(4, 1, 7)
						.average()
						.orElse(5)), // failsafe? shouldn't be necessary though lol
				2.5
		);
	}

	/**
	 * Whether explosions created from the "Explode" command should place fire blocks.
	 * Currently, this has a 5% chance.
	 *
	 * @return whether to place fire blocks
	 */
	public static boolean shouldSpawnFire() {
		return RNG.nextDouble() >= 0.95;
	}

	/**
	 * Fetches a collection of inventory item slots in which a random item should be placed.
	 *
	 * @param luck player's luck value
	 * @return collection of ints
	 */
	@NotNull
	public static Collection<@NotNull Integer> lootboxItemSlots(int luck) {
		if (luck >= 10)
			return Arrays.asList(11, 13, 15);
		else if (luck >= 5)
			return Arrays.asList(12, 14);
		else
			return Collections.singletonList(13);
	}

	/**
	 * Creates a random X or Z velocity for the fling command.
	 *
	 * @return random double corresponding to X or Z velocity
	 */
	private static double randomFlingHoriz() {
		return (RNG.nextBoolean() ? -1 : 1) * (RandomUtil.nextDouble(1.2, 3));
	}

	/**
	 * Gets a random X,Y,Z vector for the fling command.
	 *
	 * @return array with 3 values corresponding to a vector
	 */
	public static double @NotNull [] randomFlingVector() {
		return new double[]{
				randomFlingHoriz(),
				RandomUtil.nextDouble(.4, 1.5),
				randomFlingHoriz()
		};
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
		THREE(3, 4),
		FOUR(4, 2),
		FIVE(5, 1);

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
}
