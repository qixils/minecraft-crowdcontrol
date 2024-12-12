package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPacketUtil {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/ClientPacketUtil");

	@ExpectPlatform
	public static void registerPackets() {
		throw new AssertionError();
	}

	@ExpectPlatform
	public static <T extends CustomPacketPayload> void sendToServer(@NotNull T payload) {
		throw new AssertionError();
	}
}
