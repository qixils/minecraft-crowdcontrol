package dev.qixils.crowdcontrol.plugin.neoforge.client.neoforge;

import dev.qixils.crowdcontrol.plugin.fabric.client.ClientPacketContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClientPacketContextImpl(@NotNull IPayloadContext context) implements ClientPacketContext {

	@Override
	public LocalPlayer player() {
		return (LocalPlayer) context.player();
	}

	@Override
	public void send(@NotNull CustomPacketPayload payload) {
		context.handle(payload);
	}
}
