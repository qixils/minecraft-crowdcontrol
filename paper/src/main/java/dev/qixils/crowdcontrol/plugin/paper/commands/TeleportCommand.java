package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.List;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class TeleportCommand extends ImmediateCommand {
	private final String effectName = "chorus_fruit";

	public TeleportCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
			.type(Response.ResultType.RETRY)
			.message("No teleportation destinations were available");
		for (Player player : players) {
			// TODO: passengers
			Location loc = player.getLocation();
			World level = loc.getWorld();
			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();
			for (int i = 0; i < 16; ++i) {
				double destX = x + (random.nextDouble() - 0.5) * 16.0;
				double destY = Math.clamp(y + (double)(random.nextInt(16) - 8), level.getMinHeight(), level.getMinHeight() + level.getLogicalHeight() - 1);
				double destZ = z + (random.nextDouble() - 0.5) * 16.0;
				if (!randomTeleport(player, destX, destY, destZ)) continue;
				// play sound
				level.playSound(loc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
				player.playSound(player, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				break;
			}
		}
		return result;
	}

	public boolean randomTeleport(Player player, double destX, double destY, double destZ) {
		Location loc = player.getLocation();
		Location dest = loc.clone().set(destX, destY, destZ);
		int chunkX = (int) Math.floor(destX) >> 4;
		int chunkZ = (int) Math.floor(destZ) >> 4;
		World world = player.getWorld();
        if (!world.isChunkLoaded(chunkX, chunkZ))
            return false;
        while (dest.getY() > world.getMinHeight()) {
            Block block = world.getBlockAt(dest.clone().subtract(0, 1, 0));
            if (block.isCollidable()) {
                player.teleport(dest);
                BoundingBox bb = player.getBoundingBox();
                if (!world.hasCollisionsIn(bb) && !containsAnyLiquid(world, bb)) {
					player.playEffect(EntityEffect.TELEPORT_ENDER);
                    return true;
                }
                player.teleport(loc);
            }
            dest.subtract(0, 1, 0);
        }
        return false;
	}

	public boolean containsAnyLiquid(World world, BoundingBox box) {
		int minX = (int)Math.floor(box.getMinX());
		int maxX = (int)Math.ceil(box.getMaxX());
		int minY = (int)Math.floor(box.getMinY());
		int maxY = (int)Math.ceil(box.getMaxY());
		int minZ = (int)Math.floor(box.getMinZ());
		int maxZ = (int)Math.ceil(box.getMaxZ());
		Location location = new Location(world, 0, 0, 0);

		for(int o = minX; o < maxX; ++o) {
			for(int p = minY; p < maxY; ++p) {
				for(int q = minZ; q < maxZ; ++q) {
					Block block = world.getBlockAt(location.set(o, p, q));
					if (block.isLiquid()) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
