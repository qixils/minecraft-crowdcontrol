package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand<Creeper> {
	private final String effectName = "entity_charged_creeper";
	private final Component displayName = getDefaultDisplayName();

	public ChargedCreeperCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin, EntityType.CREEPER);
	}

	@Override
	@Blocking
	protected Creeper spawnEntity(@NotNull Component viewer, @NotNull ServerPlayer player) {
		Creeper creeper = super.spawnEntity(viewer, player);
		creeper.getEntityData().set(Creeper.DATA_IS_POWERED, true);
		Vec3 pos = creeper.position();
		plugin.adventure().world(creeper.level().dimension().location()).playSound(
				Sounds.LIGHTNING_STRIKE.get(),
				pos.x(),
				pos.y(),
				pos.z()
		);
		return creeper;
	}
}
