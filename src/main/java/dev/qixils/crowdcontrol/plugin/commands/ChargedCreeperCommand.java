package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
    public ChargedCreeperCommand(CrowdControlPlugin plugin) {
        super(plugin, EntityType.CREEPER);
    }

    private final String effectName = "entity-charged-creeper";
    private final String displayName = "Summon Charged Creeper";

    @Override
    protected Entity spawnEntity(Player player, Location location) {
        Creeper creeper = (Creeper) super.spawnEntity(player, location);
        creeper.setPowered(true);
        player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.0f, 1.0f);
        return creeper;
    }

    @Override
    protected Location getSpawnLocation(Location location) {
        return BlockUtil.blockFinderBuilder()
                .origin(location)
                .locationValidator(BlockUtil.SPAWNING_SPACE)
                .minRadius(5)
                .maxRadius(SPAWN_RADIUS)
                .build().next();
    }
}
