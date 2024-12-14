package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class ClientPacketUtil {

	@ExpectPlatform
	public static <T extends CustomPacketPayload> void sendToServer(@NotNull T payload) {
		throw new AssertionError();
	}
}
