package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import io.github.lexikiq.crowdcontrol.utils.ParticleUtil;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class ParticleCommand extends ChatCommand {
    public ParticleCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public @NotNull String getCommand() {
        return "particle";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        if (args.length == 0) {
            return false;
        }
        Particle particle = getParticleByArgs(args, false);
        if (particle == null) {
            return false;
        }
        for (Player player : players) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    ParticleUtil.spawnPlayerParticles(player, particle, 35);
                }
            }.runTask(plugin);
        }
        return true;
    }

    public static @Nullable Particle getParticleByArgs(String[] args, boolean allowDataClass) {
        String text = String.join("_", args).toUpperCase(Locale.ENGLISH);
        if (text.isEmpty()) {
            return null;
        }
        Particle particle;
        try {
            particle = Particle.valueOf(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (particle.getDataType() != Void.class && !allowDataClass) {
            return null;
        }
        return particle;
    }
}
