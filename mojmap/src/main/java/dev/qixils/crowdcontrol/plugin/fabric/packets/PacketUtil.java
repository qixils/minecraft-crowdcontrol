package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtil {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered;
	public static void registerPackets() {
		if (registered) return;
		registered = true;

		NetworkManager.registerReceiver(NetworkManager.c2s(), ResponseVersionC2S.PACKET_ID, ResponseVersionC2S.PACKET_CODEC, (payload, context) -> {
			if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
			ModdedCrowdControlPlugin.getInstance().handleVersionResponse(payload, context);
		});
		NetworkManager.registerReceiver(NetworkManager.c2s(), ExtraFeatureC2S.PACKET_ID, ExtraFeatureC2S.PACKET_CODEC, (payload, context) -> {
			if (!ModdedCrowdControlPlugin.isInstanceAvailable()) return;
			ModdedCrowdControlPlugin.getInstance().handleExtraFeatures(payload, context);
		});
		if (Platform.getEnvironment() != Env.CLIENT) {
			NetworkManager.registerS2CPayloadType(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC);
			NetworkManager.registerS2CPayloadType(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC);
		}
	}

	public static <T extends CustomPacketPayload> void sendToPlayer(@NotNull ServerPlayer player, @NotNull T payload) {
		try {
			NetworkManager.sendToPlayer(player, payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}
