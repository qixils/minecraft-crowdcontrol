package dev.qixils.crowdcontrol.plugin.fabric.client.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ClientPacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/ClientPacketUtil");

	public static <T extends CustomPacketPayload> void sendToServer(@NotNull T payload) {
		// TODO: check can send?
		try {
			PacketDistributor.sendToServer(payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Server cannot receive packet {}", payload);
		}
	}
}
