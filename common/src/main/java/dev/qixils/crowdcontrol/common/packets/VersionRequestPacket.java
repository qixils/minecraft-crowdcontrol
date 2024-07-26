package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.Plugin;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class VersionRequestPacket implements PluginPacket {
	private VersionRequestPacket() {
	}

	public static final VersionRequestPacket INSTANCE = new VersionRequestPacket();

	@Override
	public String channel() {
		return Plugin.VERSION_REQUEST_KEY.asString();
	}

	@Override
	public ByteBuf message() {
		return Unpooled.wrappedBuffer(new byte[0]);
	}
}
