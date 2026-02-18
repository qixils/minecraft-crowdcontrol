package dev.qixils.crowdcontrol.plugin.neoforge;

import dev.qixils.crowdcontrol.common.VersionMetadata;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.LuckPermsPermissionUtil;
import dev.qixils.crowdcontrol.plugin.neoforge.util.NeoForgePermissionUtil;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

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

		modBus.addListener(this::register);

		onInitialize();
	}

	public void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playToServer(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC, this::onResponsePacket);
		registrar.playToServer(ExtraFeatureC2S.PACKET_ID, ExtraFeatureC2S.PACKET_CODEC, this::onFeaturePacket);

		registrar.playToClient(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
		registrar.playToClient(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
		registrar.playToClient(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC);
		registrar.playToClient(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC);
	}

	private void onResponsePacket(ResponseVersionC2S payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
		handleVersionResponse(payload, new ServerPacketContext(serverPlayer));
	}

	private void onFeaturePacket(ExtraFeatureC2S payload, IPayloadContext context) {
		if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
		handleExtraFeatures(payload, new ServerPacketContext(serverPlayer));
	}

	@Override
	public @Nullable Path getPath(@NotNull String asset) {
		try {
			return NeoForgeInitializer.container.getModInfo().getOwningFile().getFile().getContents().findFile(asset).map(Path::of).orElse(null);
		} catch (Exception ignored) {
			return null;
		}
	}

	@Override
	public @NotNull Stream<String> getPathNames(@NotNull String directory) {
		try {
			if (!directory.endsWith("/")) directory = directory + '/';
			int directories = directory.replaceAll("[^/]", "").length();

			Stream.Builder<String> paths = Stream.builder();
			var contents = NeoForgeInitializer.container.getModInfo().getOwningFile().getFile().getContents();
			contents.visitContent(directory, (path, resource) -> {
				if (path.replaceAll("[^/]", "").length() != directories) return;
				paths.accept(path);
			});
			return paths.build();
		} catch (Exception ignored) {
			getSLF4JLogger().error("kyoritranslator2 failed loadsss", ignored);
			return Stream.empty();
		}
	}

	@Override
	public InputStream getInputStream(@NotNull String asset) {
		try {
			return NeoForgeInitializer.container.getModInfo().getOwningFile().getFile().getContents().openFile(asset);
		} catch (Exception e) {
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
			Optional.ofNullable(FMLLoader.getCurrentOrNull()).map(fml -> fml.getVersionInfo().neoForgeVersion()).orElse(null)
		);
	}

	public void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		// TODO: check can send?
		try {
			PacketDistributor.sendToPlayer(player, payload);
		} catch (UnsupportedOperationException e) {
			getSLF4JLogger().debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}

