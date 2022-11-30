package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
	private final String effectName = "entity_charged_creeper";

	public ChargedCreeperCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, EntityTypes.CREEPER);
	}

	@Override
	protected Entity spawnEntity(@NotNull Component viewer, @NotNull Player player) {
		Entity creeper = super.spawnEntity(viewer, player);
		creeper.offer(Keys.CREEPER_CHARGED, true);
		Vector3d pos = player.getTransform().getPosition();
		plugin.asAudience(player.getWorld())
				.playSound(Sounds.LIGHTNING_STRIKE.get(), pos.getX(), pos.getY(), pos.getZ());
		return creeper;
	}
}
