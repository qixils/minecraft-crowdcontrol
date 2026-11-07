package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.ExtraFeaturePacketC2S;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ExtraFeatureC2S extends ExtraFeaturePacketC2S {
	// boilerplate
	public static final ResourceLocation PACKET_ID = new ResourceLocation(METADATA.channel());
	public ExtraFeatureC2S(@NotNull FriendlyByteBuf buf) { super(buf); }
	public ExtraFeatureC2S(@NotNull Set<ExtraFeature> version) { super(version); }
}
