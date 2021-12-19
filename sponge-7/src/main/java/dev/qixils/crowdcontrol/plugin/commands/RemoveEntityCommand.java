package dev.qixils.crowdcontrol.plugin.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public class RemoveEntityCommand extends ImmediateCommand {
	protected final EntityType entityType;
	private final String effectName;
	private final String displayName;
	// TODO: mob key

	public RemoveEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + plugin.getTextUtil().valueOf(entityType);
		this.displayName = "Remove " + entityType.getTranslation().get();
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Builder result = request.buildResponse().type(ResultType.FAILURE)
				.message("No " + entityType.getTranslation().get() + "s found nearby to remove");

		for (Player player : players) {
			Vector3d playerPosition = player.getPosition();
			List<Entity> entities = new ArrayList<>(player.getWorld().getNearbyEntities(player.getPosition(), REMOVE_ENTITY_RADIUS));
			entities.removeIf(entity -> !entity.getType().equals(entityType));

			if (entities.isEmpty())
				continue;

			result.type(Response.ResultType.SUCCESS).message("SUCCESS");

			if (entities.size() > 1) {
				entities.sort((o1, o2) ->
						(int) (o1.getLocation().getPosition().distanceSquared(playerPosition)
								- o2.getLocation().getPosition().distanceSquared(playerPosition)));
			}

			entities.get(0).remove();
		}
		return result;
	}
}
