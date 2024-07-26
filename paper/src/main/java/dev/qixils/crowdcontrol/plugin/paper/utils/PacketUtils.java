package dev.qixils.crowdcontrol.plugin.paper.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Utf8String;

// TODO: review this and common.packets(.util)

public class PacketUtils {
	public static String readUtf8(byte[] data, int maxLength) {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
		return Utf8String.read(byteBuf, maxLength);
	}
}
