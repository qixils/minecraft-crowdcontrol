package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_SEARCH_RADIUS;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.SCALE_MODIFIER_UUID;
import static dev.qixils.crowdcontrol.plugin.fabric.utils.AttributeUtil.addModifier;
import static java.lang.Math.pow;

@Getter
public class EntitySizeCommand extends ModdedCommand {
	private final Duration defaultDuration = Duration.ofSeconds(30);
	private final String effectName;

	private final double level;
	private static final double radius = pow(ENTITY_SEARCH_RADIUS, 2);

	public EntitySizeCommand(ModdedCrowdControlPlugin plugin, String effectName, double level) {
		super(plugin);
		this.effectName = effectName;
		this.level = level;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			MinecraftServer server = plugin.getServer();
			if (server == null)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Server not active");
			Set<Location> locations = playerSupplier.get().stream().map(Location::new).collect(Collectors.toSet());
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
						addModifier(living, Attributes.SCALE, SCALE_MODIFIER_UUID, level, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true);
						success = true;
						break;
					}
				}
			}
			if (!success)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find entities to resize");
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}, plugin.getSyncExecutor()));
	}

	public static EntitySizeCommand increase(ModdedCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_double", 1);
	}

	public static EntitySizeCommand decrease(ModdedCrowdControlPlugin plugin) {
		return new EntitySizeCommand(plugin, "entity_size_halve", -0.5);
	}
}
