package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SummonEntityCommand extends ChatCommand {
    protected final EntityType entityType;
    protected static final int SPAWN_RADIUS = 7;

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
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        for (Player player : players) {
            Location loc = getSpawnLocation(player.getLocation());
            if (loc != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        spawnEntity(player, loc);
                    }
                }.runTask(plugin);
            }
        }
        return true;
    }

    protected Entity spawnEntity(Player player, Location location) {
        return player.getWorld().spawnEntity(location, entityType);
    }

    protected Location getSpawnLocation(Location location) {
        return RandomUtil.randomNearbyBlock(location, 3, SPAWN_RADIUS, true, BlockUtil.AIR_ARRAY);
    }
}
