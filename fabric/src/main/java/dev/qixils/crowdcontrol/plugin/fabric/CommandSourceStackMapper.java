package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.plugin.mojmap.AbstractCommandSourceStackMapper;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class CommandSourceStackMapper extends AbstractCommandSourceStackMapper {
	private final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull Audience asAudience(@NotNull CommandSourceStack entity) {
		return plugin.adventure().provider().audience(entity);
	}
}
