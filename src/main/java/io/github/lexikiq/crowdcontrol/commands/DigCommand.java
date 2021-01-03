package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.STONES_SET;

public class DigCommand extends ChatCommand {
    private final static double RADIUS = .5D;
    public DigCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*2.5);
    }

    @Override
    public @NotNull String getCommand() {
        return "dig";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        Set<Block> blocks = new HashSet<>();
        int depth = -(2 + rand.nextInt(4));
        for (Player player : players) {
            for (double x = -RADIUS; x <= RADIUS; ++x) {
                for (int y = depth; y < 0; ++y) {
                    for (double z = -RADIUS; z <= RADIUS; ++z) {
                        Block block = player.getLocation().add(x, y, z).getBlock();
                        if (STONES_SET.contains(block.getType())) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        if (blocks.isEmpty()) {return false;}
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Block block : blocks) {
                    block.setType(Material.AIR);
                }
            }
        }.runTask(plugin);
        return true;
    }
}
