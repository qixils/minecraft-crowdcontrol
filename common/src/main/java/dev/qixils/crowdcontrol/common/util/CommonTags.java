package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

/**
 * Collection of {@link CommonTags tags} that are used in various commands.
 */
public class CommonTags {

	/**
	 * Collection of blocks generally found underground in the overworld.
	 */
	public static final KeyedTag STONES = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "granite"),
			Key.key(MINECRAFT_NAMESPACE, "diorite"),
			Key.key(MINECRAFT_NAMESPACE, "andesite"),
			Key.key(MINECRAFT_NAMESPACE, "deepslate"),
			Key.key(MINECRAFT_NAMESPACE, "stone"),
			Key.key(MINECRAFT_NAMESPACE, "dirt"),
			Key.key(MINECRAFT_NAMESPACE, "gravel"),
			Key.key(MINECRAFT_NAMESPACE, "netherrack"),
			Key.key(MINECRAFT_NAMESPACE, "nether_bricks"),
			Key.key(MINECRAFT_NAMESPACE, "nether_wart_block"),
			Key.key(MINECRAFT_NAMESPACE, "warped_wart_block"),
			Key.key(MINECRAFT_NAMESPACE, "warped_nylium"),
			Key.key(MINECRAFT_NAMESPACE, "crimson_nylium"),
			Key.key(MINECRAFT_NAMESPACE, "grass_block"),
			Key.key(MINECRAFT_NAMESPACE, "grass"), // legacy (1.12.2)
			Key.key(MINECRAFT_NAMESPACE, "sand"),
			Key.key(MINECRAFT_NAMESPACE, "red_sand"),
			Key.key(MINECRAFT_NAMESPACE, "soul_sand"),
			Key.key(MINECRAFT_NAMESPACE, "soul_soil"),
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
			Key.key(MINECRAFT_NAMESPACE, "yellow_terracotta"),
			Key.key(MINECRAFT_NAMESPACE, "end_stone")
	);

	/**
	 * Collection of blocks generally found underground in the overworld except for gravel.
	 */
	public static final KeyedTag STONES_EXCEPT_GRAVEL = STONES.except(
			Key.key(MINECRAFT_NAMESPACE, "gravel")
	);

	/**
	 * Air blocks.
	 */
	public static final KeyedTag AIR = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "air"),
			Key.key(MINECRAFT_NAMESPACE, "cave_air"),
			Key.key(MINECRAFT_NAMESPACE, "void_air")
	);

	/**
	 * Liquid blocks.
	 */
	public static final KeyedTag LIQUIDS = new KeyedTag(
			Key.key(MINECRAFT_NAMESPACE, "water"),
			Key.key(MINECRAFT_NAMESPACE, "lava")
	);

	private CommonTags() {
		throw new IllegalStateException("Utility class");
	}
}
