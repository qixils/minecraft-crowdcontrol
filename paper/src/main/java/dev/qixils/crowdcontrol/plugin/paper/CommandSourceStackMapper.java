package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
final class CommandSourceStackMapper implements EntityMapper<CommandSourceStack> {
	private final CommandSenderMapper<CommandSender> source;

	@Override
	public Plugin<?, ?> getPlugin() {
		return source.getPlugin();
	}

	private CommandSender getSender(@NotNull CommandSourceStack entity) {
		return Objects.requireNonNullElseGet(entity.getExecutor(), entity::getSender);
	}

	@Override
	public @NotNull Audience asAudience(@NotNull CommandSourceStack entity) {
		return source.asAudience(getSender(entity)); // todo: maybe forwarding to executor & sender? might cause issues with Pointered shenanigans
	}

	@Override
	public @NotNull Optional<UUID> tryGetUniqueId(@NotNull CommandSourceStack entity) {
		return source.tryGetUniqueId(getSender(entity));
	}

	@Override
	public boolean hasPermission(@NotNull CommandSourceStack entity, @NotNull PermissionWrapper perm) {
		return source.hasPermission(entity.getSender(), perm);
	}
}
