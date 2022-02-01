package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
	private final String displayName = "Entity Chaos";
	private final String effectName = "entity_chaos";

	public EntityChaosCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (!isGlobalCommandUsable(players, request))
			return request.buildResponse().type(ResultType.UNAVAILABLE).message("Global command cannot be used on this streamer");

		List<Entity> entities = new ArrayList<>(200);
		for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
			entities.addAll(world.entities().stream().filter(entity -> !(entity instanceof Player)).collect(Collectors.toList()));
		}

		sync(() -> {
			for (int i = 0; i < entities.size(); i++) {
				Entity entity = entities.get(i);
				// TODO clear passengers
				entity.setLocation(players.get(i % players.size()).serverLocation());
			}
		});

		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
