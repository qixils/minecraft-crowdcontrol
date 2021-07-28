package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.ClassCooldowns;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
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

    public TorchCommand(CrowdControlPlugin plugin, boolean placeTorches) {
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
    public boolean execute(String authorName, List<Player> players, String... args) {
        Material[] materials = placeTorches ? BlockUtil.AIR_ARRAY : BlockUtil.TORCH_ARRAY;
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

    protected void placeTorch(Location location) {
        Block block = location.getBlock();
        BlockFace placeFace = null;
        for (BlockFace blockFace : BLOCK_FACES) {
            boolean facingDown = blockFace == BlockFace.DOWN;
            Vector value = facingDown ? blockFace.getDirection() : blockFace.getOppositeFace().getDirection();
            if (!facingDown && placeFace != null) {continue;} // down takes priority
            Material type = location.clone().add(value).getBlock().getType();
            if (type.isSolid()) {
                placeFace = blockFace;
                if (facingDown) { // down takes priority
                    break;
                }
            }
        }
        if (placeFace == null) {return;}
        boolean facingDown = placeFace == BlockFace.DOWN;
        Material placeBlock = facingDown ? Material.TORCH : Material.WALL_TORCH;
        block.setType(placeBlock);
        if (!facingDown) {
            Directional data = (Directional) block.getBlockData();
            data.setFacing(placeFace);
            block.setBlockData(data, false);
        }
    }
}
