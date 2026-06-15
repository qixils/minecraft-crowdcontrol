package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
	private final String effectName = "entity_charged_creeper";
	private final Component displayName = getDefaultDisplayName();

	public ChargedCreeperCommand(PaperCrowdControlPlugin plugin) {
		super(plugin, EntityType.CREEPER);
	}

	@Override
	protected Entity spawnEntity(@Nullable Component viewer, @NotNull Player player) {
		Creeper creeper = (Creeper) super.spawnEntity(viewer, player);
		creeper.setPowered(true);
		Location pos = creeper.getLocation();
		creeper.getWorld().playSound(Sounds.LIGHTNING_STRIKE.get(), pos.getX(), pos.getY(), pos.getZ());
		return creeper;
	}
}
