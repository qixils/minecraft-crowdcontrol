package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import io.github.lexikiq.crowdcontrol.utils.ParticleUtil;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeleportCommand extends ChatCommand {
    public TeleportCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*7.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "tp";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (Player player : players) {
            Location destination = RandomUtil.randomNearbyBlock(player.getLocation(), 3, 15, true, BlockUtil.AIR_ARRAY);
            if (destination == null) {
                continue;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(destination);
                    ParticleUtil.spawnPlayerParticles(player, Particle.PORTAL, 100);
                    player.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1.0f, 1.0f);
                }
            }.runTask(plugin);
        }
        return true;
    }
}
