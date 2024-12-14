package dev.qixils.crowdcontrol.plugin.fabric.packets.neoforge;

import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import dev.qixils.crowdcontrol.plugin.neoforge.NeoForgeCrowdControlPlugin;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered;

	public static void registerPackets(final RegisterPayloadHandlersEvent event) {
		if (registered) return;
		registered = true;

		// Sets the current network version
		final PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playToServer(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC, (payload, context) -> {
			if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
			if (!NeoForgeCrowdControlPlugin.isInstanceAvailable()) return;
			NeoForgeCrowdControlPlugin.getInstance().handleVersionResponse(payload, new ServerPacketContext(serverPlayer));
		});
		registrar.playToServer(ExtraFeatureC2S.PACKET_ID, ExtraFeatureC2S.PACKET_CODEC, (payload, context) -> {
			if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
			if (!NeoForgeCrowdControlPlugin.isInstanceAvailable()) return;
			NeoForgeCrowdControlPlugin.getInstance().handleExtraFeatures(payload, new ServerPacketContext(serverPlayer));
		});

		if (FMLEnvironment.dist != Dist.CLIENT) {
			registrar.playToClient(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC, (payload, context) -> {});
			registrar.playToClient(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC, (payload, context) -> {});
			registrar.playToClient(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC, (payload, context) -> {});
			registrar.playToClient(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC, (payload, context) -> {});
		}
	}

	public static void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		// TODO: check can send?
		try {
			PacketDistributor.sendToPlayer(player, payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}
