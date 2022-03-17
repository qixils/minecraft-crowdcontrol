package dev.qixils.crowdcontrol.mojmap;

import dev.qixils.crowdcontrol.common.EntityMapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class AbstractCommandSourceStackMapper implements EntityMapper<CommandSourceStack> {
	@Override
	public final @NotNull Optional<UUID> getUniqueId(@NotNull CommandSourceStack entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUUID);
	}

	@Override
	public final boolean isAdmin(@NotNull CommandSourceStack entity) {
		return entity.hasPermission(3);
	}
}
