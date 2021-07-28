package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FreezeCommand extends ChatCommand {
    protected static final Material SET_MATERIAL = Material.GLASS;
    protected static final double RADIUS = 1.5;

    public FreezeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60;
    }

    @Override
    public @NotNull String getCommand() {
        return "freeze";
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (Player player : players) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location base = player.getLocation();
                    for (double x = -RADIUS; x <= RADIUS; ++x) {
                        for (int y = -1; y <= 2; ++y) {
                            for (double z = -RADIUS; z <= RADIUS; ++z) {
                                Location location = base.clone().add(x, y, z);
                                Block block = location.getBlock();
                                if (BlockUtil.AIR_PLACE_SET.contains(block.getType())) {
                                    location.getBlock().setType(SET_MATERIAL);
                                }
                            }
                        }
                    }
                }
            }.runTask(plugin);
        }
        return true;
    }
}
