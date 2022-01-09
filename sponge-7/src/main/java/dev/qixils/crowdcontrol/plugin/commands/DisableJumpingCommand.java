package dev.qixils.crowdcontrol.plugin.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.data.property.block.MatterProperty.Matter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class DisableJumpingCommand extends TimedCommand {
	private final Map<UUID, Integer> jumpsBlockedAt = new HashMap<>(1);
	private final String effectName = "disable_jumping";
	private final String displayName = "Disable Jumping";

	public DisableJumpingCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private static boolean isLiquid(BlockState block) {
		Optional<MatterProperty> matterProp = block.getProperty(MatterProperty.class);
		return matterProp.isPresent() && Matter.LIQUID.equals(matterProp.get().getValue());
	}

	@Override
	public @NotNull Duration getDuration() {
		return CommandConstants.DISABLE_JUMPING_DURATION;
	}

	@Override
	public boolean isEventListener() {
		return true;
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

		// validate that this is a jump (improved in API8 maybe?)
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
			Location<World> location = event.getFromTransform().getLocation();
			AABB bbox = optAABB.get();

			// TODO cache results per Vector3i?
			Vector3d min = bbox.getMin();
			if (isLiquid(world.getBlock(min.toInt())))
				return;
			Vector3d max = bbox.getMax();
			if (isLiquid(world.getBlock(max.toInt())))
				return;
			if (isLiquid(world.getBlock(new Vector3i(min.getX(), min.getY(), max.getZ()))))
				return;
			if (isLiquid(world.getBlock(new Vector3i(min.getX(), max.getY(), max.getZ()))))
				return;
			if (isLiquid(world.getBlock(new Vector3i(max.getX(), min.getY(), max.getZ()))))
				return;
			if (isLiquid(world.getBlock(new Vector3i(max.getX(), min.getY(), min.getZ()))))
				return;
			if (isLiquid(world.getBlock(new Vector3i(max.getX(), max.getY(), min.getZ()))))
				return;
			if (isLiquid(world.getBlock(new Vector3i(min.getX(), max.getY(), min.getZ()))))
				return;
		}

		// now that we know this is a jump, we can cancel it lmao
		event.setCancelled(true);
	}
}
