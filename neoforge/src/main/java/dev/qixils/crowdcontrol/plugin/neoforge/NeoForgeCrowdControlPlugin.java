package dev.qixils.crowdcontrol.plugin.neoforge;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.neoforge.PacketUtilImpl;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.LuckPermsPermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.NeoForgePermissionUtil;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
public class NeoForgeCrowdControlPlugin extends ModdedCrowdControlPlugin {
	private final NeoForgeServerCommandManager<CommandSourceStack> commandManager
		= NeoForgeServerCommandManager.createNative(ExecutionCoordinator.asyncCoordinator());
	private final PermissionUtil permissionUtil;
	private final ModContainer container;

	public NeoForgeCrowdControlPlugin(ModContainer container, IEventBus modBus) {
		super();
		this.container = container;

		if (ModList.get().isLoaded("luckperms")) {
			permissionUtil = new LuckPermsPermissionUtil();
		} else {
			permissionUtil = new NeoForgePermissionUtil();
		}

		onInitialize();

		modBus.addListener(this::register);
	}

	public void register(final RegisterPayloadHandlersEvent event) {
		PacketUtilImpl.registerPackets(event);
	}

	@Override
	public @Nullable Path getPath(@NotNull String asset) {
		return NeoForgeInitializer.container.getModInfo().getOwningFile().getFile().findResource(asset);
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

