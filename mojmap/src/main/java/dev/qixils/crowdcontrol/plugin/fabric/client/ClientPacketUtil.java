package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPacketUtil {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/ClientPacketUtil");

	public static <T extends CustomPacketPayload> void sendToServer(@NotNull T payload) {
		try {
			NetworkManager.sendToServer(payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Server cannot receive packet {}", payload);
		}
	}
}
