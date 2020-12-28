package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChargedCreeperCommand extends SummonEntityCommand {
    public ChargedCreeperCommand(CrowdControl plugin) {
        super(plugin, EntityType.CREEPER);
    }

    @Override
    public @NotNull String getCommand() {
        return "charged";
    }

    @Override
    public int getCooldownSeconds() {
        return 60*30;
    }

    @Override
    public Entity spawnEntity(Player player, Location location) {
        Creeper creeper = (Creeper) super.spawnEntity(player, location);
        creeper.setPowered(true);
        player.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.0f, 1.0f);
        return creeper;
    }
}
