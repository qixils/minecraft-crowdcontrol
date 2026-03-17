package dev.qixils.crowdcontrol.plugin.fabric.client.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.client.ClientPacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record ClientPacketContextImpl(@NotNull Minecraft minecraft, @NotNull PacketSender sender) implements ClientPacketContext {

	@Override
	public LocalPlayer player() {
		return minecraft.player;
	}

	@Override
	public void send(@NotNull ResourceLocation location, @NotNull FriendlyByteBuf buf) {
		sender.sendPacket(location, buf);
	}
}
