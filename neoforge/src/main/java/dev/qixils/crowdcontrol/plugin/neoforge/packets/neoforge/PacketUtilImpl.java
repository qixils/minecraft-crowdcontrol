package dev.qixils.crowdcontrol.plugin.neoforge.packets.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");

	public static void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		try {
			PacketDistributor.sendToPlayer(player, payload);
		} catch (UnsupportedOperationException e) {
			log.debug("Player {} cannot receive packet {}", player, payload);
		}
	}
}
