package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.util.FabricPermissionUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FabricCrowdControlPlugin extends ModdedCrowdControlPlugin {
	private final FabricServerCommandManager<CommandSourceStack> commandManager
		= FabricServerCommandManager.createNative(ExecutionCoordinator.asyncCoordinator());
	private final PermissionUtil permissionUtil = new FabricPermissionUtil();

	@Override
	public @Nullable Path getPath(@NotNull String asset) {
		return FabricLoader.getInstance()
			.getModContainer("crowdcontrol")
			.flatMap(container -> container.findPath(asset))
			.orElse(null);
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
			"Fabric",
			server().getServerModName(),
			FabricLoader.getInstance().getModContainer("fabricloader")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse(null)
		);
	}
}
