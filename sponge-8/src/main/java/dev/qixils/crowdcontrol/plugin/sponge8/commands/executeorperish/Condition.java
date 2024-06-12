package dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish;

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
	STAND_ON_DIRT(new StandOnBlockCondition(1, "generic", BlockTypes.DIRT.get())),
	STAND_ON_STONE(new StandOnBlockCondition(1, "generic", BlockTypes.STONE.get())),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(1, "generic", BlockTypes.COBBLESTONE.get())),
	STAND_ON_SAND(new StandOnBlockCondition(2, "generic", BlockTypes.SAND.get())),
	STAND_ON_A_BED(new StandOnBlockCondition(3, "bed",
			BlockTypes.WHITE_BED.get(),
			BlockTypes.ORANGE_BED.get(),
			BlockTypes.MAGENTA_BED.get(),
			BlockTypes.LIGHT_BLUE_BED.get(),
			BlockTypes.YELLOW_BED.get(),
			BlockTypes.LIME_BED.get(),
			BlockTypes.PINK_BED.get(),
			BlockTypes.GRAY_BED.get(),
			BlockTypes.LIGHT_GRAY_BED.get(),
			BlockTypes.CYAN_BED.get(),
			BlockTypes.PURPLE_BED.get(),
			BlockTypes.BLUE_BED.get(),
			BlockTypes.BROWN_BED.get(),
			BlockTypes.GREEN_BED.get(),
			BlockTypes.RED_BED.get(),
			BlockTypes.BLACK_BED.get()
	)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(1, "plank",
			BlockTypes.OAK_PLANKS.get(),
			BlockTypes.BIRCH_PLANKS.get(),
			BlockTypes.ACACIA_PLANKS.get(),
			BlockTypes.CRIMSON_PLANKS.get(),
			BlockTypes.JUNGLE_PLANKS.get(),
			BlockTypes.WARPED_PLANKS.get(),
			BlockTypes.DARK_OAK_PLANKS.get(),
			BlockTypes.SPRUCE_PLANKS.get()
	)),
	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(2, "stripped_log",
			BlockTypes.STRIPPED_OAK_LOG.get(),
			BlockTypes.STRIPPED_BIRCH_LOG.get(),
			BlockTypes.STRIPPED_ACACIA_LOG.get(),
			BlockTypes.STRIPPED_JUNGLE_LOG.get(),
			BlockTypes.STRIPPED_DARK_OAK_LOG.get(),
			BlockTypes.STRIPPED_SPRUCE_LOG.get(),
			BlockTypes.STRIPPED_CRIMSON_STEM.get(),
			BlockTypes.STRIPPED_WARPED_STEM.get(),
			BlockTypes.STRIPPED_OAK_WOOD.get(),
			BlockTypes.STRIPPED_BIRCH_WOOD.get(),
			BlockTypes.STRIPPED_ACACIA_WOOD.get(),
			BlockTypes.STRIPPED_JUNGLE_WOOD.get(),
			BlockTypes.STRIPPED_DARK_OAK_WOOD.get(),
			BlockTypes.STRIPPED_SPRUCE_WOOD.get(),
			BlockTypes.STRIPPED_CRIMSON_HYPHAE.get(),
			BlockTypes.STRIPPED_WARPED_HYPHAE.get()
	)),
	OBTAIN_STONE_HOE(new ObtainItemCondition(2, "generic", ItemTypes.STONE_HOE.get())),
	OBTAIN_WOODEN_HOE(new ObtainItemCondition(1, "generic", ItemTypes.WOODEN_HOE.get())),
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", ItemTypes.STONE.get())),
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
