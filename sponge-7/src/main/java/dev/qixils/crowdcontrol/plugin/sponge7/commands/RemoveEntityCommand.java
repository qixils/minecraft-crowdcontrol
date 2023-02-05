package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.REMOVE_ENTITY_RADIUS;

@Getter
public class RemoveEntityCommand extends ImmediateCommand {
	protected final EntityType entityType;
	private final String effectName;
	private final Component displayName;

	public RemoveEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "remove_entity_" + SpongeTextUtil.csIdOf(entityType);
		this.displayName = Component.translatable("cc.effect.remove_entity.name", SpongeTextUtil.getFixedName(entityType));
	}

	private boolean removeEntityFrom(Player player) {
		Vector3d playerPosition = player.getPosition();
		List<Entity> entities = new ArrayList<>(player.getWorld().getNearbyEntities(player.getPosition(), REMOVE_ENTITY_RADIUS));
		entities.removeIf(entity -> !entity.getType().equals(entityType));

		if (entities.isEmpty())
			return false;

		if (entities.size() > 1) {
			entities.sort((o1, o2) ->
					(int) (o1.getLocation().getPosition().distanceSquared(playerPosition)
							- o2.getLocation().getPosition().distanceSquared(playerPosition)));
		}

		entities.get(0).remove();
		return true;
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Builder result = request.buildResponse().type(ResultType.RETRY)
				.message("No " + entityType.getTranslation().get() + "s found nearby to remove");

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(SpongeTextUtil.csIdOf(entityType));
		int victims = 0;

		// first pass (hosts)
		for (Player player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (removeEntityFrom(player))
				victims++;
		}

		// second pass (guests)
		for (Player player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (removeEntityFrom(player))
				victims++;
		}

		if (victims > 0)
			result.type(ResultType.SUCCESS).message("SUCCESS");

		return result;
	}
}
