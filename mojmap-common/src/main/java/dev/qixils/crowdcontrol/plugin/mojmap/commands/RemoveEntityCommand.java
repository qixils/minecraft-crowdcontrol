package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.StreamSupport;

import static dev.qixils.crowdcontrol.common.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public class RemoveEntityCommand extends ImmediateCommand {
	protected final EntityType<?> entityType;
	private final String effectName;
	private final String displayName;

	public RemoveEntityCommand(MojmapPlugin plugin, EntityType<?> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + Registry.ENTITY_TYPE.getKey(entityType).getPath();
		this.displayName = "Remove " + plugin.getTextUtil().asPlain(entityType.getDescription());
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder result = request.buildResponse().type(ResultType.FAILURE)
				.message("No " + plugin.getTextUtil().asPlain(entityType.getDescription()) + "s found nearby to remove");

		for (ServerPlayer player : players) {
			Vec3 playerPosition = player.position();
			List<Entity> entities = StreamSupport.stream(player.getLevel().getAllEntities().spliterator(), false)
					.filter(entity -> entity.getType() == entityType && entity.distanceToSqr(playerPosition) <= REMOVE_ENTITY_RADIUS * REMOVE_ENTITY_RADIUS)
					// TODO: ensure this sort is in the correct order
					.sorted((entity1, entity2) -> (int) (entity1.distanceToSqr(playerPosition) - entity2.distanceToSqr(playerPosition))).toList();
			if (entities.isEmpty())
				continue;
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			entities.get(0).remove(RemovalReason.KILLED);
		}
		return result;
	}
}
