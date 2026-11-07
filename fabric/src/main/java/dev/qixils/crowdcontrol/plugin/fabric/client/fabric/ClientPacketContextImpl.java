package dev.qixils.crowdcontrol.plugin.fabric.client.fabric;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import dev.qixils.crowdcontrol.plugin.fabric.client.ClientPacketContext;
import dev.qixils.crowdcontrol.plugin.fabric.packets.fabric.PacketUtilImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record ClientPacketContextImpl(@NotNull Minecraft client, @NotNull PacketSender responseSender) implements ClientPacketContext {

	@Override
	public LocalPlayer player() {
		return client.player;
	}

	@Override
	public void send(@NotNull PluginPacket payload) {
		responseSender.sendPacket(PacketUtilImpl.channel(payload), PacketUtilImpl.encode(payload));
	}
}
