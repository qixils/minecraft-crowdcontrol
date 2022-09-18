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
	STAND_ON_DIRT(new StandOnBlockCondition(0, "generic", BlockTypes.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(0, "generic", BlockTypes.STONE)),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(0, "generic", BlockTypes.COBBLESTONE)),
	STAND_ON_SAND(new StandOnBlockCondition(1, "generic", BlockTypes.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(2, "bed", BlockTypes.BED)),
	STAND_ON_A_PLANK(new StandOnBlockCondition(0, "plank", BlockTypes.PLANKS)),
	OBTAIN_STONE_HOE(new ObtainItemCondition(1, "generic", ItemTypes.STONE_HOE)),
	OBTAIN_WOODEN_HOE(new ObtainItemCondition(0, "generic", ItemTypes.WOODEN_HOE)),
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", ItemTypes.STONE)),
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
