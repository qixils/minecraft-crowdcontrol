package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;
import static java.lang.Math.pow;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class EntitySizeCommand extends ImmediateCommand {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;
	private static final double radius = pow(ENTITY_SEARCH_RADIUS, 2);

	public EntitySizeCommand(PaperCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Set<Location> locations = players.stream().map(Player::getLocation).collect(Collectors.toSet());
		boolean success = false;
		for (World world : plugin.getServer().getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (!(entity instanceof LivingEntity living)) continue;
				if (entity instanceof Player) continue; // skip players
				Location entityLoc = entity.getLocation();
				for (Location loc : locations) {
					if (!Objects.equals(loc.getWorld(), entityLoc.getWorld()))
						continue;
					if (loc.distanceSquared(entityLoc) > radius)
						continue;
					if (AttributeUtil.getModifier(living, Attribute.SCALE, SCALE_MODIFIER_UUID).map(AttributeModifier::getAmount).orElse(0d) == level)
						continue;
					entity.getScheduler().run(plugin, $ -> addModifier(living, Attribute.SCALE, SCALE_MODIFIER_UUID, SCALE_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, true), null);
					success = true;
					break;
				}
			}
		}
		if (!success)
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Could not find entities to resize");
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	public static EntitySizeCommand increase(PaperCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_double", 1);
	}

	public static EntitySizeCommand decrease(PaperCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_halve", -0.5);
	}
}
