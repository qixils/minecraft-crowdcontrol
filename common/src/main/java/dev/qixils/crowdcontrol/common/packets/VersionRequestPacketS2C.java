package dev.qixils.crowdcontrol.common.packets;

import io.netty.buffer.ByteBuf;

import static dev.qixils.crowdcontrol.common.Plugin.VERSION_REQUEST_KEY;

public class VersionRequestPacketS2C implements PluginPacket {
	public static final VersionRequestPacketS2C INSTANCE = new VersionRequestPacketS2C();
	public static final Metadata<VersionRequestPacketS2C> METADATA = new Metadata<>(VERSION_REQUEST_KEY.asString(), $ -> INSTANCE);

	protected VersionRequestPacketS2C() {
	}

	@Override
	public void write(ByteBuf buf) {
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
