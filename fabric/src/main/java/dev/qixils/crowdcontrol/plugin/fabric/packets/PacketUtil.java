package dev.qixils.crowdcontrol.plugin.fabric.packets;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketUtil {
	private static boolean registered;
	public static void registerPackets() {
		if (registered) return;
		registered = true;

		PayloadTypeRegistry.playS2C().register(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
		PayloadTypeRegistry.playC2S().register(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC);
	}
}
