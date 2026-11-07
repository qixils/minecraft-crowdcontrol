package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

//@Environment(EnvType.CLIENT)
public interface ClientPacketContext {
	LocalPlayer player();
	void send(@NotNull PluginPacket payload);
}
