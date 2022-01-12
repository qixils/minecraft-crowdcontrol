package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
	private final String effectName = "entity_charged_creeper";
	private final String displayName = "Summon Charged Creeper";

	public ChargedCreeperCommand(PaperCrowdControlPlugin plugin) {
		super(plugin, EntityType.CREEPER);
	}

	@Override
	protected Entity spawnEntity(String viewer, Player player) {
		Creeper creeper = (Creeper) super.spawnEntity(viewer, player);
		creeper.setPowered(true);
		creeper.getWorld().playSound(creeper.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.0f, 1.0f);
		return creeper;
	}
}
