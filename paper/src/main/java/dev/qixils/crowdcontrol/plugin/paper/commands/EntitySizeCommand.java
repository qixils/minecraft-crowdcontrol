package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.Bukkit;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.plugin.paper.utils.AttributeUtil.addModifier;
import static java.lang.Math.pow;

@Getter
public class EntitySizeCommand extends PaperCommand {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;
	private static final double radius = pow(ENTITY_SEARCH_RADIUS, 2);

	public EntitySizeCommand(PaperCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<Player> players = playerSupplier.get();
			Set<Location> locations = players.stream().map(Player::getLocation).collect(Collectors.toSet());
			boolean success = false;
			for (World world : Bukkit.getServer().getWorlds()) {
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
						entity.getScheduler().run(plugin.getPaperPlugin(), $ -> addModifier(living, Attribute.SCALE, SCALE_MODIFIER_UUID, SCALE_MODIFIER_NAME, level, AttributeModifier.Operation.ADD_SCALAR, true), null);
						success = true;
						break;
					}
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find entities to resize");
		}, plugin.getSyncExecutor()));
	}

	public static EntitySizeCommand increase(PaperCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_double", 1);
	}

	public static EntitySizeCommand decrease(PaperCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_halve", -0.5);
	}
}
