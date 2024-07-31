package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.ExtraFeaturePacketC2S;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ExtraFeatureC2S extends ExtraFeaturePacketC2S implements CustomPacketPayload {
	// boilerplate
	public static final StreamCodec<RegistryFriendlyByteBuf, ExtraFeatureC2S> PACKET_CODEC = CustomPacketPayload.codec(ExtraFeatureC2S::write, ExtraFeatureC2S::new);
	public static final Type<ExtraFeatureC2S> PACKET_ID = new Type<>(ResourceLocation.parse(METADATA.channel()));
	public @Override @NotNull Type<ExtraFeatureC2S> type() { return PACKET_ID; }
	public ExtraFeatureC2S(@NotNull FriendlyByteBuf buf) { super(buf); }
	public ExtraFeatureC2S(@NotNull Set<ExtraFeature> version) { super(version); }
}
