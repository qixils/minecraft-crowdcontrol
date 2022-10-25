package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FallingBlockCommand extends ChatCommand {
    protected final Material blockMaterial;
    public FallingBlockCommand(CrowdControl plugin, Material blockMaterial) {
        super(plugin);
        this.blockMaterial = blockMaterial;
    }

    @Override
    public int getCooldownSeconds() {
        return 5;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.FALLING_BLOCK;
    }

    @Override
    public @NotNull String getCommand() {
        return blockMaterial.name();
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        // input parsing
        int y;
        if (args.length < 1) {
            y = 5;
        } else {
            try {
                y = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (y < 1) {
                return false;
            }
        }

        // spawn the damn anvil
        for (Player player : players) {
            Location destination = player.getEyeLocation();
            destination.setY(Math.min(destination.getY()+y, player.getWorld().getMaxHeight()-1));
            Block block = destination.getBlock();
            if (BlockUtil.AIR_BLOCKS.contains(block.getType())) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        block.setType(blockMaterial, true);
                    }
                }.runTask(plugin);
            }
        }
        return true;
    }
}
