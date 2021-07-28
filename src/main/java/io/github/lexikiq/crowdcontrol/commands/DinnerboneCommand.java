package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DinnerboneCommand extends ChatCommand {
    private static final String NAME = "Dinnerbone";
    private static final int RADIUS = 15;

    public DinnerboneCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public @NotNull String getCommand() {
        return "dinnerbone";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        Set<LivingEntity> entities = new HashSet<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    entities.addAll(player.getLocation().getNearbyLivingEntities(RADIUS, x -> x.getType() != EntityType.PLAYER && (x.getCustomName() == null || x.getCustomName().isEmpty() || x.getCustomName().equals(NAME))));
                }
                entities.forEach(x -> x.setCustomName(Objects.equals(x.getCustomName(), NAME) ? null : NAME));
            }
        }.runTask(plugin);
        return true;
    }
}
