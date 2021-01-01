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
    public DigCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public int getCooldownSeconds() {
        return 60*3;
    }

    @Override
    public @NotNull String getCommand() {
        return "dig";
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        Set<Block> blocks = new HashSet<>();
        for (Player player : players) {
            for (int y = -2; y < 0; ++y){
                Block block = player.getLocation().add(0, y, 0).getBlock();
                if (STONES_SET.contains(block.getType())) {
                    blocks.add(block);
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
