package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.plugin.mojmap.AbstractCommandSourceStackMapper;
import net.kyori.adventure.audience.Audience;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

public class CommandSourceStackMapper extends AbstractCommandSourceStackMapper {
	public CommandSourceStackMapper(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	public FabricCrowdControlPlugin getPlugin() {
		return (FabricCrowdControlPlugin) super.getPlugin();
	}

	@Override
	public @NotNull Audience asAudience(@NotNull CommandSourceStack entity) {
		return getPlugin().adventure().provider().audience(entity);
	}
}
