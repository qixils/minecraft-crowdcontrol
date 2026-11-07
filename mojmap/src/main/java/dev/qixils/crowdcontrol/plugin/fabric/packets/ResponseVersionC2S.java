package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.VersionResponsePacketC2S;
import dev.qixils.crowdcontrol.common.util.SemVer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ResponseVersionC2S extends VersionResponsePacketC2S {
	// boilerplate
	public static final ResourceLocation PACKET_ID = new ResourceLocation(METADATA.channel());
	public ResponseVersionC2S(@NotNull FriendlyByteBuf buf) { super(buf); }
	public ResponseVersionC2S(@NotNull SemVer version) { super(version); }
}
