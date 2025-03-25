package dev.qixils.crowdcontrol.plugin.fabric.client.neoforge;

import dev.qixils.crowdcontrol.plugin.fabric.client.ClientPacketContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public record ClientPacketContextImpl(@NotNull IPayloadContext context, @NotNull LocalPlayer player) implements ClientPacketContext {

	@Override
	public LocalPlayer player() {
		return player;
	}

	@Override
	public void send(@NotNull CustomPacketPayload payload) {
		context.handle(payload);
	}
}
