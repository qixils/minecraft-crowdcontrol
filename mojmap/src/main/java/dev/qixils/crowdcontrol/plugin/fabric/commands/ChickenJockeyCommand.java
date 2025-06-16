package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ChickenJockeyCommand extends SummonEntityCommand<Chicken> {
	public ChickenJockeyCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin, "entity_chicken_jockey", null, EntityType.CHICKEN);
	}

	@Override
	@Blocking
	protected Chicken spawnEntity(@Nullable Component viewer, @NotNull ServerPlayer player) {
		Chicken chicken = super.spawnEntity(viewer, player);
		if (chicken == null) return null;
		chicken.setChickenJockey(true);

		Zombie zombie = spawnEntity(viewer, player, EntityType.ZOMBIE, plugin);
		if (zombie == null) return chicken;
		zombie.setBaby(true);

		zombie.startRiding(chicken, true);

		return chicken;
	}
}
