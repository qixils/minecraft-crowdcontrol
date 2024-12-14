package dev.qixils.crowdcontrol.plugin.fabric.client.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.client.ClientPacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record ClientPacketContextImpl(@NotNull ClientPlayNetworking.Context context) implements ClientPacketContext {

	@Override
	public LocalPlayer player() {
		return context.player();
	}

	@Override
	public void send(@NotNull CustomPacketPayload payload) {
		context.responseSender().sendPacket(payload);
	}
}
