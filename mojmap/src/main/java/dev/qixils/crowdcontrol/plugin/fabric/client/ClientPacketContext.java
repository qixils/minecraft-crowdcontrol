package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public interface ClientPacketContext {
	LocalPlayer player();
	void send(@NotNull ResourceLocation payload, @NotNull FriendlyByteBuf buf);
	default void send(@NotNull PluginPacket payload) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		payload.write(buf);
		send(new ResourceLocation(payload.metadata().channel()), new FriendlyByteBuf(buf.copy()));
	}
}
