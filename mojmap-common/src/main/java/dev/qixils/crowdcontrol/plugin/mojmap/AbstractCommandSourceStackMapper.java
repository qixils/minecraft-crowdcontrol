package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
public abstract class AbstractCommandSourceStackMapper implements EntityMapper<CommandSourceStack> {
	private final MojmapPlugin<?> plugin;

	protected AbstractCommandSourceStackMapper(MojmapPlugin<?> plugin) {
		this.plugin = plugin;
	}

	@Override
	public final @NotNull Optional<UUID> getUniqueId(@NotNull CommandSourceStack entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUUID);
	}

	@Override
	public final boolean isAdmin(@NotNull CommandSourceStack entity) {
		if (entity.hasPermission(3)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
