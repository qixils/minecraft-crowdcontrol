package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private final String displayName = "Entity Chaos";
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return globalCommandUnusable(request);

		List<Entity> entities = new ArrayList<>(200);
		for (World world : plugin.getGame().getServer().getWorlds()) {
			entities.addAll(world.getEntities(entity -> !entity.getType().equals(EntityTypes.PLAYER)));
		}

		sync(() -> {
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				entity.clearPassengers();
				entity.setLocation(players.get(i % players.size()).getLocation());
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
