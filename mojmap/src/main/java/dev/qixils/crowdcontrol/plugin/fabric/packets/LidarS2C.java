package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.LidarPacketS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static net.minecraft.resources.Identifier.parse;

public class LidarS2C extends LidarPacketS2C implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, LidarS2C> PACKET_CODEC = CustomPacketPayload.codec(LidarS2C::write, LidarS2C::new);
	public static final Type<LidarS2C> PACKET_ID = new Type<>(parse(METADATA.channel()));
	public @Override @NotNull Type<LidarS2C> type() { return PACKET_ID; }
	public LidarS2C(FriendlyByteBuf buf) { super(buf); }
	public LidarS2C(Duration duration) { super(duration); }
}
