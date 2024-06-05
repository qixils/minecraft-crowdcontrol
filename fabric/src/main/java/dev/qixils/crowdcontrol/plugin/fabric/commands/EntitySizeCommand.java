package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static java.lang.Math.pow;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class EntitySizeCommand extends ImmediateCommand {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;
	private static final double radius = pow(ENTITY_SEARCH_RADIUS, 2);

	public EntitySizeCommand(FabricCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		MinecraftServer server = plugin.getServer();
		if (server == null)
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Server not active");
		Set<Location> locations = players.stream().map(Location::new).collect(Collectors.toSet());
		boolean success = false;
		for (ServerLevel world : plugin.getServer().getAllLevels()) {
			for (Entity entity : world.getAllEntities()) {
				if (!(entity instanceof LivingEntity living)) continue;
				if (entity instanceof Player) continue; // skip players
				Location entityLoc = new Location(entity);
				for (Location loc : locations) {
					if (!Objects.equals(loc.level(), entityLoc.level()))
						continue;
                    if (loc.squareDistanceTo(entityLoc) > radius)
                        continue;
					if (AttributeUtil.getModifier(living, Attributes.SCALE, SCALE_MODIFIER_UUID).map(AttributeModifier::amount).orElse(0d) == level)
						continue;
                    addModifier(living, Attributes.SCALE, SCALE_MODIFIER_UUID, SCALE_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
                    success = true;
                    break;
                }
			}
		}
		if (!success)
			return request.buildResponse().type(Response.ResultType.FAILURE).message("Could not find entities to resize");
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	public static EntitySizeCommand increase(FabricCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_double", 1);
	}

	public static EntitySizeCommand decrease(FabricCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_halve", -0.5);
	}
}
