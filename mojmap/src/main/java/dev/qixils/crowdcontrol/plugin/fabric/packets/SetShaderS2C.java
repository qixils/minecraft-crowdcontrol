package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.ShaderPacketS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.time.Duration;

public class SetShaderS2C extends ShaderPacketS2C {
	// boilerplate
	public static final ResourceLocation PACKET_ID = new ResourceLocation(METADATA.channel());
	public SetShaderS2C(FriendlyByteBuf buf) { super(buf); }
	public SetShaderS2C(String shader, Duration duration) { super(shader, duration); }
}
