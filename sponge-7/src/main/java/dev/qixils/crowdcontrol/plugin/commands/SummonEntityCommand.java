package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulties;

import java.util.List;
import java.util.Optional;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	protected final EntityType entityType;
	private final String effectName;
	private final String displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + Sponge7TextUtil.valueOf(entityType);
		this.displayName = "Summon " + entityType.getTranslation().get();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Class<? extends Entity> entityClass = entityType.getEntityClass();
		if (Monster.class.isAssignableFrom(entityClass)) {
			for (World world : plugin.getGame().getServer().getWorlds()) {
				if (world.getDifficulty().equals(Difficulties.PEACEFUL)) {
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
	protected Entity spawnEntity(String viewer, Player player) {
		Entity entity = player.getLocation().createEntity(entityType);
		entity.offer(Keys.DISPLAY_NAME, Text.of(viewer));
		entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.TAMED_OWNER, Optional.of(player.getUniqueId()));
		entity.offer(SpongeCrowdControlPlugin.VIEWER_SPAWNED, true);
		// loot table data not supported in v7

		try (StackFrame frame = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.getWorld().spawnEntity(entity);
		}
		return entity;
	}
}
