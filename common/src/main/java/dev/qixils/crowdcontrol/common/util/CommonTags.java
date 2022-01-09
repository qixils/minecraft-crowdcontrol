package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

public class CommonTags {
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
			Key.key(MINECRAFT_NAMESPACE, "wither_rose")
	);

	public static final KeyedTag STONES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "granite"),
			Key.key(MINECRAFT_NAMESPACE, "diorite"),
			Key.key(MINECRAFT_NAMESPACE, "andesite"),
			Key.key(MINECRAFT_NAMESPACE, "deepslate"),
			Key.key(MINECRAFT_NAMESPACE, "stone"),
			Key.key(MINECRAFT_NAMESPACE, "dirt"),
			Key.key(MINECRAFT_NAMESPACE, "gravel"),
			Key.key(MINECRAFT_NAMESPACE, "netherrack"),
			Key.key(MINECRAFT_NAMESPACE, "grass_block"),
			Key.key(MINECRAFT_NAMESPACE, "sand"),
			Key.key(MINECRAFT_NAMESPACE, "red_sand"),
			Key.key(MINECRAFT_NAMESPACE, "sandstone"),
			Key.key(MINECRAFT_NAMESPACE, "red_sandstone"),
			Key.key(MINECRAFT_NAMESPACE, "terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "orange_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "black_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "blue_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "brown_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "cyan_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "gray_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "green_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "light_blue_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "light_gray_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "lime_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "magenta_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "pink_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "purple_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "red_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "white_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "yellow_terracotta")
	);

	public static final KeyedTag TORCHES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_wall_torch"),
			Key.key(MINECRAFT_NAMESPACE, "soul_wall_torch")
	);

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

	public static final KeyedTag SET_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "tnt"),
			Key.key(MINECRAFT_NAMESPACE, "fire"),
			Key.key(MINECRAFT_NAMESPACE, "cobweb"),
			Key.key(MINECRAFT_NAMESPACE, "redstone_torch"),
			Key.key(MINECRAFT_NAMESPACE, "wither_rose"),
			Key.key(MINECRAFT_NAMESPACE, "lightning_rod"),
			Key.key(MINECRAFT_NAMESPACE, "bedrock")
	);

	public static final KeyedTag SET_FALLING_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "anvil"),
			Key.key(MINECRAFT_NAMESPACE, "sand"),
			Key.key(MINECRAFT_NAMESPACE, "red_sand"),
			Key.key(MINECRAFT_NAMESPACE, "gravel")
	);

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
			Key.key(MINECRAFT_NAMESPACE, "elytra")
	);

	public static final KeyedTag REPLACEABLE_BLOCKS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "air"),
			Key.key(MINECRAFT_NAMESPACE, "cave_air"),
			Key.key(MINECRAFT_NAMESPACE, "void_air"),
			Key.key(MINECRAFT_NAMESPACE, "grass"),
			Key.key(MINECRAFT_NAMESPACE, "tall_grass"),
			Key.key(MINECRAFT_NAMESPACE, "water"),
			Key.key(MINECRAFT_NAMESPACE, "lava")
	);

	public static final KeyedTag SPOOKY_SOUNDS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "entity.creeper.primed"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.stare"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.scream"),
			Key.key(MINECRAFT_NAMESPACE, "entity.ender_dragon.growl"),
			Key.key(MINECRAFT_NAMESPACE, "entity.ghast.hurt"),
			Key.key(MINECRAFT_NAMESPACE, "entity.generic.explode"),
			Key.key(MINECRAFT_NAMESPACE, "ambient.cave")
	);

	public static Sound spookySoundOf(Key key) {
		return Sound.sound(
				key,
				Source.MASTER,
				1.75f,
				1f
		);
	}

	private CommonTags() {
		throw new IllegalStateException("Utility class");
	}
}
