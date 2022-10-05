package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Condition {
	STAND_ON_DIRT(new StandOnBlockCondition(0, "generic", Blocks.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(0, "generic", Blocks.STONE)),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(0, "generic", Blocks.COBBLESTONE)),
	STAND_ON_SAND(new StandOnBlockCondition(1, "generic", Blocks.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(2, "bed",
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
	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(2, "stripped_log",
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
	OBTAIN_STONE_HOE(new ObtainItemCondition(1, "generic", Items.STONE_HOE)),
	OBTAIN_WOODEN_HOE(new ObtainItemCondition(0, "generic", Items.WOODEN_HOE)),
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", Items.STONE)),
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
