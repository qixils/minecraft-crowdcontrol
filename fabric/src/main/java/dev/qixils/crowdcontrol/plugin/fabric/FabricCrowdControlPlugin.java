package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.packets.fabric.PacketUtilImpl;
import dev.qixils.crowdcontrol.plugin.fabric.util.FabricPermissionUtil;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FabricCrowdControlPlugin extends ModdedCrowdControlPlugin implements ModInitializer {
	private final FabricServerCommandManager<CommandSourceStack> commandManager
		= FabricServerCommandManager.createNative(ExecutionCoordinator.asyncCoordinator());
	private final PermissionUtil permissionUtil = new FabricPermissionUtil();

	@Override
	public void onInitialize() {
		super.onInitialize();

		PacketUtilImpl.registerPackets();
	}

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

	public void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		if (!ServerPlayNetworking.canSend(player, payload.type())) return;
		try {
			ServerPlayNetworking.send(player, payload);
		} catch (UnsupportedOperationException e) {
			getSLF4JLogger().debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}
