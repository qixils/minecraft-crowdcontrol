package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.mixin.CreeperAccessor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand<CreeperEntity> {
	private final String effectName = "entity_charged_creeper";
	private final Component displayName = getDefaultDisplayName();

	public ChargedCreeperCommand(FabricCrowdControlPlugin plugin) {
		super(plugin, EntityType.CREEPER);
	}

	@Override
	@Blocking
	protected CreeperEntity spawnEntity(@NotNull Component viewer, @NotNull ServerPlayerEntity player) {
		CreeperEntity creeper = super.spawnEntity(viewer, player);
		creeper.getDataTracker().set(CreeperAccessor.getIsPoweredAccessor(), true);
		Vec3d pos = creeper.getPos();
		plugin.adventure().world(creeper.world.getRegistryKey().getValue()).playSound(
				Sounds.LIGHTNING_STRIKE.get(),
				pos.getX(),
				pos.getY(),
				pos.getZ()
		);
		return creeper;
	}
}
