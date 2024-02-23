package dev.qixils.crowdcontrol.plugin.fabric.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketUtil {
	public static void registerPackets() {
		PayloadTypeRegistry.playS2C().register(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC);
	}
}
