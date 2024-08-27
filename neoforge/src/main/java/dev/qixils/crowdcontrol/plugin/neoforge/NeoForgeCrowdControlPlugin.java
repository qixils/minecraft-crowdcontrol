package dev.qixils.crowdcontrol.plugin.neoforge;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.NeoForgePermissionUtil;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: improve pause checks

@Getter
@Mod("crowdcontrol")
public class NeoForgeCrowdControlPlugin extends ModdedCrowdControlPlugin {
	private final NeoForgeServerCommandManager<CommandSourceStack> commandManager
		= NeoForgeServerCommandManager.createNative(ExecutionCoordinator.asyncCoordinator());
	private final PermissionUtil permissionUtil = new NeoForgePermissionUtil();
	private final ModContainer container;

	public NeoForgeCrowdControlPlugin(ModContainer container) {
		super();
		this.container = container;
		onInitialize();
	}

	@Override
	public @Nullable Path getPath(@NotNull String asset) {
		return container.getModInfo().getOwningFile().getFile().findResource(asset);
	}

	@Override
	public InputStream getInputStream(@NotNull String asset) {
		Path path = getPath(asset);

		if (path == null)
			return null;

		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			getSLF4JLogger().warn("Encountered exception while retrieving asset {}", asset, e);
			return null;
		}
	}

	@Override
	public @NotNull VersionMetadata getVersionMetadata() {
		return new VersionMetadata(
			server().getServerVersion(),
			"NeoForge",
			server().getServerModName(),
			FMLLoader.versionInfo().neoForgeVersion()
		);
	}
}

