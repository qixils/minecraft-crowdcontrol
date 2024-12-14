package dev.qixils.crowdcontrol.plugin.fabric.client.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
@SuppressWarnings("unused")
public class ClientPacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/ClientPacketUtil");

	public static <T extends CustomPacketPayload> void sendToServer(@NotNull T payload) {
		if (!ClientPlayNetworking.canSend(payload.type())) return;
		try {
			ClientPlayNetworking.send(payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Server cannot receive packet {}", payload);
		}
	}
}
