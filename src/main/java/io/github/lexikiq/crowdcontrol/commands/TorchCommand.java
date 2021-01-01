package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.github.lexikiq.crowdcontrol.utils.BlockUtil.*;

public class TorchCommand extends ChatCommand {
    protected final boolean placeTorches;
    protected final String name;
    protected static final BlockFace[] BLOCK_FACES = new BlockFace[]{
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.WEST,
            BlockFace.SOUTH,
            BlockFace.NORTH
    };

    public TorchCommand(CrowdControl plugin, boolean placeTorches) {
        super(plugin);
        this.placeTorches = placeTorches;
        this.name = placeTorches ? "lit" : "dim";
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.TORCH;
    }

    @Override
    public @NotNull String getCommand() {
        return name;
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        Material[] materials = placeTorches ? AIR_ARRAY : TORCH_ARRAY;
        List<Location> nearbyBlocks = new ArrayList<>();
        for (Player player : players) {
            nearbyBlocks.addAll(RandomUtil.randomNearbyBlocks(player.getLocation(), 5, false, materials));
        }
        if (nearbyBlocks.isEmpty()) {return false;}
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Location location : nearbyBlocks) {
                    Block block = location.getBlock();
                    if (placeTorches) {
                        placeTorch(location);
                    } else {
                        block.setType(Material.AIR, false);
                    }
                }
            }
        }.runTask(plugin);
        return true;
    }

    protected boolean placeTorch(Location location) {
        Block block = location.getBlock();
        BlockFace placeFace = null;
        for (BlockFace blockFace : BLOCK_FACES) {
            boolean facingDown = blockFace == BlockFace.DOWN;
            Vector value = facingDown ? blockFace.getDirection() : blockFace.getOppositeFace().getDirection();
            if (!facingDown && placeFace != null) {continue;} // down takes priority
            Material type = location.clone().add(value).getBlock().getType();
            if (!AIR_BLOCKS.contains(type) && !TORCH_SET.contains(type)) {
                placeFace = blockFace;
                if (facingDown) { // down takes priority
                    break;
                }
            }
        }
        if (placeFace == null) {return false;}
        boolean facingDown = placeFace == BlockFace.DOWN;
        Material placeBlock = facingDown ? Material.TORCH : Material.WALL_TORCH;
        block.setType(placeBlock);
        if (!facingDown) {
            Directional data = (Directional) block.getBlockData();
            data.setFacing(placeFace);
            block.setBlockData(data, false);
        }
        return true;
    }
}
