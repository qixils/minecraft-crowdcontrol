package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

@Getter
public class CommandSourceStackMapper implements EntityMapper<CommandSourceStack> {
	protected final FabricCrowdControlPlugin plugin;

	protected CommandSourceStackMapper(FabricCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull Audience asAudience(@NotNull CommandSourceStack entity) {
		return plugin.translator().wrap(entity);
	}

	@Override
	public final @NotNull Optional<UUID> tryGetUniqueId(@NotNull CommandSourceStack entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUUID).or(() -> entity.get(Identity.UUID));
	}

	@Override
	public final boolean isAdmin(@NotNull CommandSourceStack entity) {
		if (entity.hasPermission(3)) return true;
		return EntityMapper.super.isAdmin(entity);
	}
}
