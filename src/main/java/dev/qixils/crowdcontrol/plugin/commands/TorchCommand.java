package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class TorchCommand extends Command {
    protected final boolean placeTorches;
    protected final String effectName;
    protected final String displayName;
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
        this.effectName = placeTorches ? "Lit" : "Dim";
        this.displayName = (placeTorches ? "Place" : "Break") + " Torches";
    }

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        Predicate<Location> predicate = placeTorches ? loc -> loc.getBlock().isReplaceable() : BlockUtil.TORCHES::matches;
        List<Location> nearbyBlocks = new ArrayList<>();
        CrowdControlPlugin.getPlayers().forEach(player -> nearbyBlocks.addAll(BlockUtil.blockFinderBuilder()
                .origin(player.getLocation())
                .maxRadius(5)
                .locationValidator(predicate)
                .shuffleLocations(false)
                .build().getAll()));
        if (nearbyBlocks.isEmpty())
            return new Response.Result(Response.ResultType.FAILURE, "No available blocks to place/remove");

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Location location : nearbyBlocks) {
                Block block = location.getBlock();
                if (placeTorches)
                    placeTorch(location);
                else
                    block.setType(Material.AIR, false);
            }
        });
        return Response.Result.SUCCESS;
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
