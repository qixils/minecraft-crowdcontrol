package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.ClassCooldowns;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlockCommand extends ChatCommand {
    protected final Material material;
    public BlockCommand(CrowdControlPlugin plugin, Material block) {
        super(plugin);
        this.material = block;
    }

    @Override
    public int getCooldownSeconds() {
        return 60*5;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.BLOCK;
    }

    @Override
    public @NotNull String getCommand() {
        return material.name();
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        boolean didSomething = false;
        for (Player player : players) {
            Block block = player.getLocation().getBlock();
            if (BlockUtil.AIR_BLOCKS.contains(block.getType())) {
                didSomething = true;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        block.setType(material);
                    }
                }.runTask(plugin);
            }
        }
        return didSomething;
    }
}
