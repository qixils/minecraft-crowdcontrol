package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import lombok.Getter;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.api.entity.living.player.Player;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
	private final String effectName = "entity_charged_creeper";
	private final String displayName = "Summon Charged Creeper";

	public ChargedCreeperCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin, EntityTypes.CREEPER);
	}

	@Override
	protected Entity spawnEntity(String viewer, Player player) {
		Creeper creeper = (Creeper) super.spawnEntity(viewer, player);
		creeper.charged().set(true);
		player.getWorld().playSound(SoundTypes.ENTITY_LIGHTNING_THUNDER, SoundCategories.HOSTILE, creeper.getLocation().getPosition(), 1f, 1f);
		return creeper;
	}
}
