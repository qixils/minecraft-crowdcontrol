package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PacketUtil {

	@ExpectPlatform
	public static void sendToPlayer(@NotNull ServerPlayer player, @NotNull CustomPacketPayload payload) {
		throw new AssertionError();
	}
}
