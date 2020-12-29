package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SummonEntityCommand extends ChatCommand {
    private final EntityType entityType;
    private static final int SPAWN_RADIUS = 7;

    public SummonEntityCommand(CrowdControl plugin, EntityType entityType) {
        super(plugin);
        this.entityType = entityType;
    }

    @Override
    public @NotNull String getCommand() {
        return entityType.name();
    }

    @Override
    public int getCooldownSeconds() {
        return 60*15;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.ENTITY;
    }

    @Override
    public boolean execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        Player player = (Player) RandomUtil.randomElementFrom(players); // get random pokemon
        assert player != null;  // make IDE stop yelling at me
        Location loc = getSpawnLocation(player.getLocation());
        if (loc != null) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    spawnEntity(player, loc);
                }
            }.runTask(plugin);
            return true;
        }
        return false;
    }

    public Entity spawnEntity(Player player, Location location) {
        return player.getWorld().spawnEntity(location, entityType);
    }

    private static Location getSpawnLocation(Location location) {
        return RandomUtil.randomNearbyBlock(location, SPAWN_RADIUS, true, Material.AIR);
    }
}
