package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.mixin.CreeperAccessor;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Blocking;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand<Creeper> {
	private final String effectName = "entity_charged_creeper";
	private final String displayName = "Summon Charged Creeper";

	public ChargedCreeperCommand(MojmapPlugin plugin) {
		super(plugin, EntityType.CREEPER);
	}

	@Override
	@Blocking
	protected Creeper spawnEntity(String viewer, ServerPlayer player) {
		Creeper creeper = super.spawnEntity(viewer, player);
		creeper.getEntityData().set(CreeperAccessor.getIsPoweredAccessor(), true);
		Vec3 pos = creeper.position();
		plugin.adventure().world(creeper.getLevel()).playSound(
				Sounds.LIGHTNING_STRIKE.get(),
				pos.x(),
				pos.y(),
				pos.z()
		);
		return creeper;
	}
}
