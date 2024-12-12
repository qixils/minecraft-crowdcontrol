package dev.qixils.crowdcontrol.plugin.fabric.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public interface ClientPacketContext {
	LocalPlayer player();
	void send(@NotNull CustomPacketPayload payload);
}
