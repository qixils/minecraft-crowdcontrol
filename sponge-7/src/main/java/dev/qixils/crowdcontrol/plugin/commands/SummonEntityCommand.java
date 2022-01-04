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
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.entity.CustomNameVisibleData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
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
	// TODO: mob key

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
					return request.buildResponse().type(ResultType.FAILURE).message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}

		plugin.getSyncExecutor().execute(() -> players.forEach(player -> spawnEntity(request.getViewer(), player)));
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Blocking
	protected Entity spawnEntity(String viewer, Player player) {
		Entity entity = player.getLocation().createEntity(entityType);
		entity.get(DisplayNameData.class).ifPresent(data -> data.displayName().set(Text.of(viewer)));
		entity.get(CustomNameVisibleData.class).ifPresent(data -> data.customNameVisible().set(true));
		entity.get(TameableData.class).ifPresent(data -> data.owner().set(Optional.of(player.getUniqueId())));
		// loot table data not supported in v7
		// TODO mob key

		try (StackFrame frame = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.getWorld().spawnEntity(entity);
		}
		return entity;
	}
}
