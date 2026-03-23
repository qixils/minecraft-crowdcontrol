package dev.qixils.crowdcontrol.plugin.fabric.packets.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered = false;

	public static void registerPackets() {
		if (registered) return;
		registered = true;

		PayloadTypeRegistry.clientboundPlay().register(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC);

		PayloadTypeRegistry.serverboundPlay().register(ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ResponseVersionC2S.PACKET_ID, (payload, context) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			FabricCrowdControlPlugin.getInstance().handleVersionResponse(payload, new ServerPacketContext(context.player()));
		});
		PayloadTypeRegistry.serverboundPlay().register(ExtraFeatureC2S.PACKET_ID, ExtraFeatureC2S.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ExtraFeatureC2S.PACKET_ID, (payload, context) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			FabricCrowdControlPlugin.getInstance().handleExtraFeatures(payload, new ServerPacketContext(context.player()));
		});
	}
}
