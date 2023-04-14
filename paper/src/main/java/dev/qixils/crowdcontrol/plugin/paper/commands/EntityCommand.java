package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.jetbrains.annotations.NotNull;

public interface EntityCommand extends FeatureElementCommand {
	@NotNull EntityType getEntityType();

	@Override
	default @NotNull FeatureFlagSet requiredFeatures() {
		return CraftMagicNumbers.getEntityTypes(getEntityType()).requiredFeatures();
	}

	@Override
	default TriState isSelectable() {
		if (!Monster.class.isAssignableFrom(getEntityType().getEntityClass()))
			return TriState.TRUE;
		if (Bukkit.getWorlds().stream().anyMatch(world -> world.getDifficulty() != Difficulty.PEACEFUL))
			return TriState.TRUE;
		return TriState.FALSE;
	}
}
