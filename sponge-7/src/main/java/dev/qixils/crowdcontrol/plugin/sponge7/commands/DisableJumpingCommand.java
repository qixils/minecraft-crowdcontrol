package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@EventListener
public class DisableJumpingCommand extends TimedCommand {
	private final Map<UUID, Integer> jumpsBlockedAt = new HashMap<>(1);
	private final String effectName = "disable_jumping";
	private final String displayName = "Disable Jumping";

	public DisableJumpingCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder().request(request)
				.duration(CommandConstants.DISABLE_JUMPING_DURATION)
				.startCallback($ -> {
					List<Player> players = plugin.getPlayers(request);
					if (players.isEmpty())
						return request.buildResponse()
								.type(ResultType.FAILURE)
								.message("No players online");

					int tick = plugin.getGame().getServer().getRunningTimeTicks();
					for (Player player : players)
						jumpsBlockedAt.put(player.getUniqueId(), tick);
					playerAnnounce(players, request);

					return null; // success
				}).build().queue();
	}

	@SuppressWarnings("DuplicatedCode")
	@Listener
	public void onMoveEvent(MoveEntityEvent.Position event) {
		Entity entity = event.getTargetEntity();
		UUID uuid = entity.getUniqueId();
		if (!jumpsBlockedAt.containsKey(uuid))
			return;

		int blockedAt = jumpsBlockedAt.get(uuid);
		// if this effect has expired then remove its data from the map
		if ((blockedAt + CommandConstants.DISABLE_JUMPING_TICKS) < plugin.getGame().getServer().getRunningTimeTicks()) {
			jumpsBlockedAt.remove(uuid, blockedAt);
			return;
		}

		if (!entity.getType().equals(EntityTypes.PLAYER))
			return;
		if (event.getToTransform().getPosition().getY() <= event.getFromTransform().getPosition().getY())
			return;
		if (entity.isOnGround())
			return;
		if (entity.getVelocity().getY() <= 0)
			return;
		if (event.getCause().contains(plugin.getPluginContainer()))
			return;

		// ensure player hit-box is not inside a liquid (i.e. they are probably just swimming up
		// which is okay in Paper)
		Optional<AABB> optAABB = entity.getBoundingBox();
		if (optAABB.isPresent()) {
			World world = entity.getWorld();
			AABB bbox = optAABB.get();

			Set<Vector3i> checked = new HashSet<>(7);
			Vector3d min = bbox.getMin();
			if (canAscendAt(checked, world, min.toInt()))
				return;
			Vector3d max = bbox.getMax();
			if (canAscendAt(checked, world, max.toInt()))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.getX(), min.getY(), max.getZ())))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.getX(), max.getY(), max.getZ())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.getX(), min.getY(), max.getZ())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.getX(), min.getY(), min.getZ())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.getX(), max.getY(), min.getZ())))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.getX(), max.getY(), min.getZ())))
				return;
		}

		// now that we know this is a jump, we can cancel it lmao
		event.setCancelled(true);
	}

	private static boolean canAscendAt(Set<Vector3i> checked, World world, Vector3i pos) {
		if (checked.contains(pos))
			return false;
		checked.add(pos);
		BlockState block = world.getBlock(pos);
		return block.getType().equals(BlockTypes.LADDER) || SpongeCrowdControlPlugin.isLiquid(block);
	}
}
