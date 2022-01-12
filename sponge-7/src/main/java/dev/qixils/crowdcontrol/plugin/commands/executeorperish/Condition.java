package dev.qixils.crowdcontrol.plugin.commands.executeorperish;

import dev.qixils.crowdcontrol.common.util.TextBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.item.ItemTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Condition {
	STAND_ON_DIRT(new StandOnBlockCondition("a %s block", BlockTypes.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition("a %s block", BlockTypes.STONE)),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition("a %s block", BlockTypes.COBBLESTONE)),
	STAND_ON_SAND(new StandOnBlockCondition("a %s block", BlockTypes.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(new TextBuilder("a &abed").build(),
			BlockTypes.BED
//            BlockTypes.WHITE_BED,
//            BlockTypes.ORANGE_BED,
//            BlockTypes.MAGENTA_BED,
//            BlockTypes.LIGHT_BLUE_BED,
//            BlockTypes.YELLOW_BED,
//            BlockTypes.LIME_BED,
//            BlockTypes.PINK_BED,
//            BlockTypes.GRAY_BED,
//            BlockTypes.LIGHT_GRAY_BED,
//            BlockTypes.CYAN_BED,
//            BlockTypes.PURPLE_BED,
//            BlockTypes.BLUE_BED,
//            BlockTypes.BROWN_BED,
//            BlockTypes.GREEN_BED,
//            BlockTypes.RED_BED,
//            BlockTypes.BLACK_BED
	)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(new TextBuilder("a &awooden plank").build(),
			BlockTypes.PLANKS
//			BlockTypes.OAK_PLANKS,
//			BlockTypes.BIRCH_PLANKS,
//			BlockTypes.ACACIA_PLANKS,
//			BlockTypes.CRIMSON_PLANKS,
//			BlockTypes.JUNGLE_PLANKS,
//			BlockTypes.WARPED_PLANKS,
//			BlockTypes.DARK_OAK_PLANKS,
//			BlockTypes.SPRUCE_PLANKS
	)),
	//	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(new TextBuilder("a &astripped log").build(),
//			BlockTypes.STRIPPED_OAK_LOG,
//			BlockTypes.STRIPPED_BIRCH_LOG,
//			BlockTypes.STRIPPED_ACACIA_LOG,
//			BlockTypes.STRIPPED_JUNGLE_LOG,
//			BlockTypes.STRIPPED_DARK_OAK_LOG,
//			BlockTypes.STRIPPED_SPRUCE_LOG,
//			BlockTypes.STRIPPED_CRIMSON_STEM,
//			BlockTypes.STRIPPED_WARPED_STEM,
//			BlockTypes.STRIPPED_OAK_WOOD,
//			BlockTypes.STRIPPED_BIRCH_WOOD,
//			BlockTypes.STRIPPED_ACACIA_WOOD,
//			BlockTypes.STRIPPED_JUNGLE_WOOD,
//			BlockTypes.STRIPPED_DARK_OAK_WOOD,
//			BlockTypes.STRIPPED_SPRUCE_WOOD,
//			BlockTypes.STRIPPED_CRIMSON_HYPHAE,
//			BlockTypes.STRIPPED_WARPED_HYPHAE
//	)),
	OBTAIN_STONE_HOE(new ObtainItemCondition("a %s", ItemTypes.STONE_HOE)),
	OBTAIN_WOODEN_HOE(new ObtainItemCondition("a %s", ItemTypes.WOODEN_HOE)),
	OBTAIN_STONE(new ObtainItemCondition("a %s block", ItemTypes.STONE)),
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
