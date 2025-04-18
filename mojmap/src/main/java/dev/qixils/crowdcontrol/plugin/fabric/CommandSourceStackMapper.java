package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
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
	protected final ModdedCrowdControlPlugin plugin;

	protected CommandSourceStackMapper(ModdedCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull Audience asAudience(@NotNull CommandSourceStack entity) {
		return plugin.adventure().audience(entity);
	}

	@Override
	public final @NotNull Optional<UUID> tryGetUniqueId(@NotNull CommandSourceStack entity) {
		return Optional.ofNullable(entity.getEntity()).map(Entity::getUUID).or(() -> entity.get(Identity.UUID));
	}

	@Override
	public boolean hasPermission(@NotNull CommandSourceStack entity, @NotNull PermissionWrapper perm) {
		return plugin.getPermissionUtil().check(entity, perm);
	}
}
