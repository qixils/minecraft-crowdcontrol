package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_SEARCH_RADIUS;

@Getter
public class AgeCommand extends ModdedCommand {
	private static final double SEARCH = ENTITY_SEARCH_RADIUS * ENTITY_SEARCH_RADIUS;
	private final String effectName;
	private final boolean baby;

	public AgeCommand(ModdedCrowdControlPlugin plugin, boolean baby) {
		super(plugin);
		this.baby = baby;
		this.effectName = "age_" + (baby ? "baby" : "adult");
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			Set<AgeableMob> entities = new HashSet<>();
			for (ServerPlayer player : playerSupplier.get()) {
				entities.addAll(
					player.level().getEntities(
						EntityTypeTest.forClass(AgeableMob.class),
						AABB.ofSize(player.position(), SEARCH, SEARCH, SEARCH),
						ent -> !ent.is(EntityTypeTags.CANNOT_BE_AGE_LOCKED) && ent.isAgeLocked() != baby
					)
				);
			}
			if (entities.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No nearby entities");
			for (AgeableMob entity : entities) {
				entity.setAgeLocked(baby);
				entity.setBaby(baby);
			}
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}, plugin.getSyncExecutor()));
	}
}
