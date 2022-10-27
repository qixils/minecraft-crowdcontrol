package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum Condition {
	// stand on...
	STAND_ON_DIRT(new StandOnBlockCondition(0, "generic", Material.DIRT)),
	STAND_ON_STONE(new StandOnBlockCondition(0, "generic", Material.STONE)),
	STAND_ON_COBBLESTONE(new StandOnBlockCondition(0, "generic", Material.COBBLESTONE)),
	STAND_ON_SAND(new StandOnBlockCondition(1, "generic", Material.SAND)),
	STAND_ON_A_BED(new StandOnBlockCondition(2, "bed",
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
	STAND_ON_A_PLANK(new StandOnBlockCondition(0, "plank",
			Material.OAK_PLANKS,
			Material.BIRCH_PLANKS,
			Material.ACACIA_PLANKS,
			Material.CRIMSON_PLANKS,
			Material.JUNGLE_PLANKS,
			Material.WARPED_PLANKS,
			Material.DARK_OAK_PLANKS,
			Material.SPRUCE_PLANKS
	)),
	STAND_ON_A_STRIPPED_LOG(new StandOnBlockCondition(2, "stripped_log",
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
	OBTAIN_WHEAT_SEEDS(new ObtainItemCondition(0, "generic", Material.WHEAT_SEEDS)),
	OBTAIN_STONE(new ObtainItemCondition(3, "generic_block", Material.STONE)),
	// craft...
	CRAFT_STONE_HOE(new CraftItemCondition(1, "generic", Material.STONE_HOE)),
	CRAFT_WOODEN_HOE(new CraftItemCondition(0, "generic", Material.WOODEN_HOE)),
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
