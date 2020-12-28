package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SummonEntityCommand extends ChatCommand {
    private final EntityType entityType;
    private static final int SPAWN_RADIUS = 7;
    private static LocalDateTime globalUsage = null;

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
    public boolean canUse() {
        return super.canUse() && (globalUsage == null || globalUsage.plusMinutes(1).isBefore(LocalDateTime.now()));
    }

    @Override
    public void execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        super.execute(event, players);
        globalUsage = LocalDateTime.now();

        Player player = (Player) RandomUtil.randomElementFrom(players);
        assert player != null;
        Location loc = getSpawnLocation(player.getLocation());
        if (loc != null) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.getWorld().spawnEntity(loc, entityType);
                }
            }.runTask(plugin);
        }
    }

    private static Location getSpawnLocation(Location location) {
        List<Location> locations = new ArrayList<>();
        for (int x = -SPAWN_RADIUS; x <= SPAWN_RADIUS; x++) {
            for (int y = -1; y <= SPAWN_RADIUS; y++) {
                for (int z = -SPAWN_RADIUS; z <= SPAWN_RADIUS; z++) {
                    Location base = location.clone().add(x, y, z);
                    Material above = base.clone().add(0, 1, 0).getBlock().getType();
                    Material below = base.clone().add(0, -1, 0).getBlock().getType();
                    if (base.getBlock().getType() == Material.AIR && above == Material.AIR && below != Material.AIR) {
                        locations.add(base);
                    }
                }
            }
        }
        if (locations.size() > 0) {
            return (Location) RandomUtil.randomElementFrom(locations);
        }
        return null;
    }
}
