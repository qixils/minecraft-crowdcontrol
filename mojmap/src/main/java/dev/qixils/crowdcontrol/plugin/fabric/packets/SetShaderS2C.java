package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.ShaderPacketS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static net.minecraft.resources.ResourceLocation.parse;

public class SetShaderS2C extends ShaderPacketS2C implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, SetShaderS2C> PACKET_CODEC = CustomPacketPayload.codec(SetShaderS2C::write, SetShaderS2C::new);
	public static final Type<SetShaderS2C> PACKET_ID = new Type<>(parse(METADATA.channel()));
	public @Override @NotNull Type<SetShaderS2C> type() { return PACKET_ID; }
	public SetShaderS2C(FriendlyByteBuf buf) { super(buf); }
	public SetShaderS2C(String shader, Duration duration) { super(shader, duration); }
}
