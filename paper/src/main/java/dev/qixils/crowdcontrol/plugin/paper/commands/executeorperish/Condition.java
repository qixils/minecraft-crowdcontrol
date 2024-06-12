package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Condition {
	// stand on...
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(1, "generic", Material.COBBLESTONE)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(1, "plank",
			Material.OAK_PLANKS,
			Material.BIRCH_PLANKS,
			Material.ACACIA_PLANKS,
			Material.CRIMSON_PLANKS,
			Material.JUNGLE_PLANKS,
			Material.WARPED_PLANKS,
			Material.DARK_OAK_PLANKS,
			Material.SPRUCE_PLANKS
	)),
	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(4, "stripped_log",
			Material.STRIPPED_OAK_LOG,
			Material.STRIPPED_BIRCH_LOG,
			Material.STRIPPED_ACACIA_LOG,
			Material.STRIPPED_JUNGLE_LOG,
			Material.STRIPPED_DARK_OAK_LOG,
			Material.STRIPPED_SPRUCE_LOG,
			Material.STRIPPED_CRIMSON_STEM,
			Material.STRIPPED_WARPED_STEM,
			Material.STRIPPED_OAK_WOOD,
			Material.STRIPPED_BIRCH_WOOD,
			Material.STRIPPED_ACACIA_WOOD,
			Material.STRIPPED_JUNGLE_WOOD,
			Material.STRIPPED_DARK_OAK_WOOD,
			Material.STRIPPED_SPRUCE_WOOD,
			Material.STRIPPED_CRIMSON_HYPHAE,
			Material.STRIPPED_WARPED_HYPHAE
	)),
	// obtain...
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", Material.STONE)),
	// craft...
	CRAFT_STONE_HOE(new CraftItemCondition(2, "generic", Material.STONE_HOE)),
	CRAFT_WOODEN_HOE(new CraftItemCondition(1, "generic", Material.WOODEN_HOE)),

	//// overworld-only ////
	// stand on...
	STAND_ON_DIRT(new StandOnBlockCondition(1, "generic", ConditionFlags.OVERWORLD, Material.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(1, "generic", ConditionFlags.OVERWORLD, Material.STONE)),
	STAND_ON_SAND(new StandOnBlockCondition(2, "generic", ConditionFlags.OVERWORLD, Material.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(3, "bed", ConditionFlags.OVERWORLD,
			Material.WHITE_BED,
			Material.ORANGE_BED,
			Material.MAGENTA_BED,
			Material.LIGHT_BLUE_BED,
			Material.YELLOW_BED,
			Material.LIME_BED,
			Material.PINK_BED,
			Material.GRAY_BED,
			Material.LIGHT_GRAY_BED,
			Material.CYAN_BED,
			Material.PURPLE_BED,
			Material.BLUE_BED,
			Material.BROWN_BED,
			Material.GREEN_BED,
			Material.RED_BED,
			Material.BLACK_BED
	)),
	// obtain...
	OBTAIN_WHEAT_SEEDS(new ObtainItemCondition(1, "generic_alt", ConditionFlags.OVERWORLD, Material.WHEAT_SEEDS)),
	// craft...
	CRAFT_SANDSTONE(new CraftItemCondition(1, "generic_block", Material.SANDSTONE, ConditionFlags.OVERWORLD)),

	//// nether-only ////
	// stand on...
	STAND_ON_FIRE(new StandOnBlockCondition(1, "generic", ConditionFlags.NETHER, Material.FIRE)),
	// obtain...
	OBTAIN_NETHER_BRICK(new ObtainItemCondition(3, "generic", ConditionFlags.NETHER, Material.NETHER_BRICK)),
	OBTAIN_OBSIDIAN(new ObtainItemCondition(3, "generic_alt", ConditionFlags.NETHER, Material.OBSIDIAN)),
	// craft...
	CRAFT_QUARTZ(new CraftItemCondition(1, "generic", Material.QUARTZ_BLOCK, ConditionFlags.NETHER)),
	CRAFT_GOLD_INGOT(new CraftItemCondition(1, "generic", Material.GOLD_INGOT, ConditionFlags.NETHER)),
	CRAFT_GLOWSTONE(new CraftItemCondition(1, "generic_block", Material.GLOWSTONE, ConditionFlags.NETHER)),

	//// miscellaneous ////
	JUMP(new JumpingJacksCondition(49)),
	;

	private static final @NotNull List<SuccessCondition> CONDITIONS;

	static {
		List<SuccessCondition> conditions = new ArrayList<>(values().length);
		for (Condition cond : values())
			conditions.add(cond.getCondition());
		CONDITIONS = Collections.unmodifiableList(conditions);
	}

	private final @NotNull SuccessCondition condition;

	public static @NotNull List<@NotNull SuccessCondition> items() {
		return CONDITIONS;
	}
}
