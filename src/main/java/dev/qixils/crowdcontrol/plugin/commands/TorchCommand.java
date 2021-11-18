package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
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
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class TorchCommand extends ImmediateCommand {
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
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Predicate<Location> predicate = placeTorches ? loc -> loc.getBlock().isReplaceable() : BlockUtil.TORCHES::matches;
        List<Location> nearbyBlocks = new ArrayList<>();
        players.forEach(player -> nearbyBlocks.addAll(BlockUtil.blockFinderBuilder()
                .origin(player.getLocation())
                .maxRadius(5)
                .locationValidator(predicate)
                .shuffleLocations(false)
                .build().getAll()));
        if (nearbyBlocks.isEmpty())
            return request.buildResponse().type(Response.ResultType.FAILURE).message("No available blocks to place/remove");

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Location location : nearbyBlocks) {
                Block block = location.getBlock();
                if (placeTorches)
                    placeTorch(location);
                else
                    block.setType(Material.AIR, false);
            }
        });
        return request.buildResponse().type(Response.ResultType.SUCCESS);
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
