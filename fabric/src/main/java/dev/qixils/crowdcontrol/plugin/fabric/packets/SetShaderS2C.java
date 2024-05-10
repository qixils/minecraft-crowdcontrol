package dev.qixils.crowdcontrol.plugin.fabric.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin.SHADER_ID;

public record SetShaderS2C(@NotNull String shader, @NotNull Duration duration) implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, SetShaderS2C> PACKET_CODEC = CustomPacketPayload.codec(SetShaderS2C::write, SetShaderS2C::new);
	public static final Type<SetShaderS2C> PACKET_ID = new Type<>(SHADER_ID);
	public @Override @NotNull Type<SetShaderS2C> type() { return PACKET_ID; }
	public SetShaderS2C(FriendlyByteBuf buf) { this(buf.readUtf(64), Duration.ofMillis(buf.readLong())); }
	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(shader, 64);
		buf.writeLong(duration.toMillis());
	}
}
