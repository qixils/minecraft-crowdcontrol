package dev.qixils.crowdcontrol.plugin.fabric.packets.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered = false;

	public static void registerPackets() {
		if (registered) return;
		registered = true;

		PayloadTypeRegistry.playS2C().register(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(ExtraFeatureC2S.PACKET_ID, ExtraFeatureC2S.PACKET_CODEC);
	}

	public static void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		if (!ServerPlayNetworking.canSend(player, payload.type())) return;
		try {
			ServerPlayNetworking.send(player, payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}
