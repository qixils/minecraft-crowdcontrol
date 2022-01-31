package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.EntityMapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

class PlayerMapper<E extends Player> extends CommandSenderMapper<E> {
	@Override
	public @NotNull Optional<UUID> getUniqueId(@NotNull Player entity) {
		return Optional.of(entity.getUniqueId());
	}
}
