package dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish;

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
	STAND_ON_DIRT(new StandOnBlockCondition(2, "generic", BlockTypes.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(2, "generic", BlockTypes.STONE)),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(2, "generic", BlockTypes.COBBLESTONE)),
	STAND_ON_SAND(new StandOnBlockCondition(3, "generic", BlockTypes.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(4, "bed", BlockTypes.BED)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(2, "plank", BlockTypes.PLANKS)),
	OBTAIN_STONE_HOE(new ObtainItemCondition(3, "generic", ItemTypes.STONE_HOE)),
	OBTAIN_WOODEN_HOE(new ObtainItemCondition(2, "generic", ItemTypes.WOODEN_HOE)),
	OBTAIN_STONE(new ObtainItemCondition(4, "generic_block", ItemTypes.STONE, BlockTypes.STONE)),
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
