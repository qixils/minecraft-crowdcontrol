package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Condition {
	// stand on...
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(0, "generic", Blocks.COBBLESTONE)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(0, "plank",
			Blocks.OAK_PLANKS,
			Blocks.BIRCH_PLANKS,
			Blocks.ACACIA_PLANKS,
			Blocks.CRIMSON_PLANKS,
			Blocks.JUNGLE_PLANKS,
			Blocks.WARPED_PLANKS,
			Blocks.DARK_OAK_PLANKS,
			Blocks.SPRUCE_PLANKS
	)),
	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(4, "stripped_log",
			Blocks.STRIPPED_OAK_LOG,
			Blocks.STRIPPED_BIRCH_LOG,
			Blocks.STRIPPED_ACACIA_LOG,
			Blocks.STRIPPED_JUNGLE_LOG,
			Blocks.STRIPPED_DARK_OAK_LOG,
			Blocks.STRIPPED_SPRUCE_LOG,
			Blocks.STRIPPED_CRIMSON_STEM,
			Blocks.STRIPPED_WARPED_STEM,
			Blocks.STRIPPED_OAK_WOOD,
			Blocks.STRIPPED_BIRCH_WOOD,
			Blocks.STRIPPED_ACACIA_WOOD,
			Blocks.STRIPPED_JUNGLE_WOOD,
			Blocks.STRIPPED_DARK_OAK_WOOD,
			Blocks.STRIPPED_SPRUCE_WOOD,
			Blocks.STRIPPED_CRIMSON_HYPHAE,
			Blocks.STRIPPED_WARPED_HYPHAE
	)),
	// obtain...
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", Items.STONE)),
	// craft...
	CRAFT_STONE_HOE(new CraftItemCondition(1, "generic", Items.STONE_HOE)),
	CRAFT_WOODEN_HOE(new CraftItemCondition(0, "generic", Items.WOODEN_HOE)),

	//// overworld-only ////
	// stand on...
	STAND_ON_DIRT(new StandOnBlockCondition(0, "generic", ConditionFlags.OVERWORLD, Blocks.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(0, "generic", ConditionFlags.OVERWORLD, Blocks.STONE)),
	STAND_ON_SAND(new StandOnBlockCondition(1, "generic", ConditionFlags.OVERWORLD, Blocks.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(3, "bed", ConditionFlags.OVERWORLD,
			Blocks.WHITE_BED,
			Blocks.ORANGE_BED,
			Blocks.MAGENTA_BED,
			Blocks.LIGHT_BLUE_BED,
			Blocks.YELLOW_BED,
			Blocks.LIME_BED,
			Blocks.PINK_BED,
			Blocks.GRAY_BED,
			Blocks.LIGHT_GRAY_BED,
			Blocks.CYAN_BED,
			Blocks.PURPLE_BED,
			Blocks.BLUE_BED,
			Blocks.BROWN_BED,
			Blocks.GREEN_BED,
			Blocks.RED_BED,
			Blocks.BLACK_BED
	)),
	// obtain...
	OBTAIN_WHEAT_SEEDS(new ObtainItemCondition(0, "generic_alt", ConditionFlags.OVERWORLD, Items.WHEAT_SEEDS)),
	// craft...
	CRAFT_SANDSTONE(new CraftItemCondition(0, "generic_block", Items.SANDSTONE, ConditionFlags.OVERWORLD)),

	//// nether-only ////
	// stand on...
	STAND_ON_FIRE(new StandOnBlockCondition(0, "generic", ConditionFlags.NETHER, Blocks.FIRE)),
	// obtain...
	OBTAIN_NETHER_BRICK(new ObtainItemCondition(3, "generic", ConditionFlags.NETHER, Items.NETHER_BRICK)),
	OBTAIN_OBSIDIAN(new ObtainItemCondition(2, "generic_alt", ConditionFlags.NETHER, Items.OBSIDIAN)),
	// craft...
	CRAFT_QUARTZ(new CraftItemCondition(0, "generic", Items.QUARTZ_BLOCK, ConditionFlags.NETHER)),
	CRAFT_GOLD_INGOT(new CraftItemCondition(0, "generic", Items.GOLD_INGOT, ConditionFlags.NETHER)),
	CRAFT_GLOWSTONE(new CraftItemCondition(0, "generic_block", Items.GLOWSTONE, ConditionFlags.NETHER)),

	//// miscellaneous ////
	JUMP(new JumpingJacksCondition(49)),
	;

	private static final List<SuccessCondition> CONDITIONS;

	static {
		List<SuccessCondition> conditions = new ArrayList<>(values().length);
		for (Condition cond : values())
			conditions.add(cond.getCondition());
		CONDITIONS = Collections.unmodifiableList(conditions);
	}

	private final SuccessCondition condition;

	public static List<SuccessCondition> items() {
		return CONDITIONS;
	}
}
