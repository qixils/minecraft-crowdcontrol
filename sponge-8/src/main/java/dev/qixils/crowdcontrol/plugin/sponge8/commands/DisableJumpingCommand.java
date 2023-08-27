package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.BlockFinder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.util.*;

@Getter
@EventListener
public class DisableJumpingCommand extends TimedVoidCommand {
	private final Map<UUID, Long> jumpsBlockedAt = new HashMap<>(1);
	private final String effectName = "disable_jumping";

	public DisableJumpingCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDefaultDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder().request(request)
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					if (players.isEmpty())
						return request.buildResponse()
								.type(ResultType.FAILURE)
								.message("No players online");

					long tick = plugin.getGame().server().runningTimeTicks().ticks();
					for (Player player : players)
						jumpsBlockedAt.put(player.uniqueId(), tick);
					playerAnnounce(players, request);

					return null; // success
				}).build().queue();
	}

	@SuppressWarnings("DuplicatedCode")
	@Listener
	public void onMoveEvent(MoveEntityEvent event) {
		Entity entity = event.entity();
		UUID uuid = entity.uniqueId();
		if (!jumpsBlockedAt.containsKey(uuid))
			return;

		long blockedAt = jumpsBlockedAt.get(uuid);
		// if this effect has expired then remove its data from the map
		if ((blockedAt + CommandConstants.DISABLE_JUMPING_TICKS) < plugin.getGame().server().runningTimeTicks().ticks()) {
			jumpsBlockedAt.remove(uuid, blockedAt);
			return;
		}

		if (!entity.type().equals(EntityTypes.PLAYER.get()))
			return;
		if (event.destinationPosition().y() <= event.originalPosition().y())
			return;
		if (entity.onGround().get())
			return;
		if (entity.velocity().get().y() <= 0)
			return;
		if (event.cause().contains(plugin.getPluginContainer()))
			return;

		// ensure player hit-box is not inside a liquid (i.e. they are probably just swimming up
		// which is okay in Paper)
		Optional<AABB> optAABB = entity.boundingBox();
		if (optAABB.isPresent()) {
			World<?, ?> world = entity.world();
			AABB bbox = optAABB.get();

			Set<Vector3i> checked = new HashSet<>(7);
			Vector3d min = bbox.min();
			if (canAscendAt(checked, world, min.toInt()))
				return;
			Vector3d max = bbox.max();
			if (canAscendAt(checked, world, max.toInt()))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.x(), min.y(), max.z())))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.x(), max.y(), max.z())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.x(), min.y(), max.z())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.x(), min.y(), min.z())))
				return;
			if (canAscendAt(checked, world, new Vector3i(max.x(), max.y(), min.z())))
				return;
			if (canAscendAt(checked, world, new Vector3i(min.x(), max.y(), min.z())))
				return;
		}

		// now that we know this is a jump, we can cancel it lmao
		event.setCancelled(true);
	}

	private static boolean canAscendAt(Set<Vector3i> checked, World<?, ?> world, Vector3i pos) {
		if (checked.contains(pos))
			return false;
		checked.add(pos);
		BlockState block = world.block(pos);
		return block.type().equals(BlockTypes.LADDER.get()) || BlockFinder.isLiquid(block);
	}
}
