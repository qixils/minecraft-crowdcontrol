package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3d;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
	private final String effectName = "entity_charged_creeper";
	private final Component displayName = getDefaultDisplayName();

	public ChargedCreeperCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, EntityTypes.CREEPER.get());
	}

	@Override
	protected Entity spawnEntity(@NotNull Component viewer, @NotNull ServerPlayer player) {
		Entity creeper = super.spawnEntity(viewer, player);
		creeper.offer(Keys.IS_CHARGED, true);
		Vector3d pos = player.position();
		player.world().playSound(Sounds.LIGHTNING_STRIKE.get(), pos.x(), pos.y(), pos.z());
		return creeper;
	}
}
