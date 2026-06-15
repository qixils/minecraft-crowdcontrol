package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ChickenJockeyCommand extends SummonEntityCommand {
	private final String effectName = "entity_chicken_jockey";
	private final Component displayName = getDefaultDisplayName();

	public ChickenJockeyCommand(PaperCrowdControlPlugin plugin) {
		super(plugin, EntityType.CHICKEN);
	}

	@Override
	protected Entity spawnEntity(@Nullable Component viewer, @NotNull Player player) {
		Chicken chicken = (Chicken) super.spawnEntity(viewer, player);
		if (chicken == null) return null;
		chicken.setIsChickenJockey(true);

		Zombie zombie = (Zombie) spawnEntity(viewer, player, EntityType.ZOMBIE, getMobKey());
		if (zombie == null) return chicken;
		zombie.setBaby();

		chicken.addPassenger(zombie);
		return chicken;
	}
}
