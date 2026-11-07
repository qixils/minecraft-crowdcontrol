package dev.qixils.crowdcontrol.plugin.fabric.packets;

import dev.qixils.crowdcontrol.common.packets.VersionRequestPacketS2C;
import net.minecraft.resources.ResourceLocation;

public class RequestVersionS2C extends VersionRequestPacketS2C {
	// boilerplate
	public static final RequestVersionS2C INSTANCE = new RequestVersionS2C();
	public static final ResourceLocation PACKET_ID = new ResourceLocation(METADATA.channel());
	private RequestVersionS2C() {}
}
