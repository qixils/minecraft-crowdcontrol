package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ClassCooldowns;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChargedCreeperCommand extends SummonEntityCommand {
    public ChargedCreeperCommand(CrowdControlPlugin plugin) {
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
