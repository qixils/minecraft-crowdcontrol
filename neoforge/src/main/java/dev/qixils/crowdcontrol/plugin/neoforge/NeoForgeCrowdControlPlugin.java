package dev.qixils.crowdcontrol.plugin.neoforge;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.NeoForgePermissionUtil;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.common.Mod;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

// TODO: access transformers
// TODO: `crowdcontrol-client` "mod" ?

@Getter
@Mod("crowdcontrol")
public class NeoForgeCrowdControlPlugin extends ModdedCrowdControlPlugin {
	private final NeoForgeServerCommandManager<CommandSourceStack> commandManager
		= NeoForgeServerCommandManager.createNative(ExecutionCoordinator.asyncCoordinator());
	private final PermissionUtil permissionUtil = new NeoForgePermissionUtil();

	static {
		LoggerFactory.getLogger("CrowdControl").info("Static loaded");
	}

	public NeoForgeCrowdControlPlugin() {
		super();
		getSLF4JLogger().info("Loaded");
		onInitialize();
	}

	// TODO: getInputStream?

	@Override
	public @NotNull VersionMetadata getVersionMetadata() {
		return new VersionMetadata(
			server().getServerVersion(),
			"NeoForge",
			server().getServerModName(),
			null // TODO: FML version
		);
	}

	// TODO: expose config
}

