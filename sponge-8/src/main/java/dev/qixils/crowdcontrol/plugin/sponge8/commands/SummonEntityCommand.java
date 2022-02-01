package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.List;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	protected final EntityType<?> entityType;
	protected final boolean isMonster;
	private final String effectName;
	private final String displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType<?> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.isMonster = entityType.category().equals(EntityCategories.MONSTER.get());
		this.effectName = "entity_" + entityType.key(RegistryTypes.ENTITY_TYPE).value();
		this.displayName = "Summon " + plugin.getTextUtil().asPlain(entityType);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isMonster) {
			for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
				if (world.difficulty().equals(Difficulties.PEACEFUL.get())) {
					return request.buildResponse()
							.type(ResultType.FAILURE)
							.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}

		sync(() -> players.forEach(player -> spawnEntity(request.getViewer(), player)));
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Blocking
	protected Entity spawnEntity(String viewer, ServerPlayer player) {
		Entity entity = player.world().createEntity(entityType, player.position());
		entity.offer(Keys.DISPLAY_NAME, Component.text(viewer));
		entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.IS_TAMED, true);
		entity.offer(Keys.TAMER, player.uniqueId());
		entity.offer(SpongeCrowdControlPlugin.VIEWER_SPAWNED, true);
		// API8: loot table data

		try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.world().spawnEntity(entity);
		}
		return entity;
	}
}
