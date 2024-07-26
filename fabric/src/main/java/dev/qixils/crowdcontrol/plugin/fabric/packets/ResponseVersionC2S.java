package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.VersionResponsePacketC2S;
import dev.qixils.crowdcontrol.common.util.SemVer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin.VERSION_RESPONSE_ID;

public class ResponseVersionC2S extends VersionResponsePacketC2S implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, ResponseVersionC2S> PACKET_CODEC = CustomPacketPayload.codec(ResponseVersionC2S::write, ResponseVersionC2S::new);
	public static final Type<ResponseVersionC2S> PACKET_ID = new Type<>(VERSION_RESPONSE_ID);
	public @Override @NotNull Type<ResponseVersionC2S> type() { return PACKET_ID; }
	public ResponseVersionC2S(@NotNull FriendlyByteBuf buf) { super(buf); }
	public ResponseVersionC2S(@NotNull SemVer version) { super(version); }
}
