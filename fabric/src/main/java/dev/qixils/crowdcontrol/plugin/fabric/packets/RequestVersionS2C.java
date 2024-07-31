package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.VersionRequestPacketS2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.resources.ResourceLocation.parse;

public class RequestVersionS2C extends VersionRequestPacketS2C implements CustomPacketPayload {
	// boilerplate
	public static final RequestVersionS2C INSTANCE = new RequestVersionS2C();
	public static final StreamCodec<RegistryFriendlyByteBuf, RequestVersionS2C> PACKET_CODEC = StreamCodec.unit(INSTANCE);
	public static final Type<RequestVersionS2C> PACKET_ID = new Type<>(parse(METADATA.channel()));
	public @Override @NotNull Type<RequestVersionS2C> type() { return PACKET_ID; }
	private RequestVersionS2C() {}
}
