package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

@Getter
public class ChargedCreeperCommand extends SummonEntityCommand {
    public ChargedCreeperCommand(BukkitCrowdControlPlugin plugin) {
        super(plugin, EntityType.CREEPER);
    }

    private final String effectName = "entity_charged_creeper";
    private final String displayName = "Summon Charged Creeper";

    @Override
    protected Entity spawnEntity(String viewer, Player player) {
        Creeper creeper = (Creeper) super.spawnEntity(viewer, player);
        creeper.setPowered(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.0f, 1.0f);
        return creeper;
    }
}
