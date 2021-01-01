package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
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
    public ClassCooldowns getClassCooldown() {
        return super.getClassCooldown();
    }

    @Override
    protected Entity spawnEntity(Player player, Location location) {
        Creeper creeper = (Creeper) super.spawnEntity(player, location);
        creeper.setPowered(true);
        player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.0f, 1.0f);
        return creeper;
    }

    @Override
    protected Location getSpawnLocation(Location location) {
        return RandomUtil.randomNearbyBlock(location, 5, SPAWN_RADIUS, true, BlockUtil.AIR_ARRAY);
    }
}
