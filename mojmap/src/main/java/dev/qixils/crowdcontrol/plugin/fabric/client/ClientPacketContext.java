package dev.qixils.crowdcontrol.plugin.fabric.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public interface ClientPacketContext {
	LocalPlayer player();
	void send(@NotNull CustomPacketPayload payload);
}
