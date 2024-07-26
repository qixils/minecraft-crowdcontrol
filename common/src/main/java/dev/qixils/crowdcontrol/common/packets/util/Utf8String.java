package dev.qixils.crowdcontrol.common.packets.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;

public class Utf8String {
	public static String read(ByteBuf buf, int maxLength) {
		int i = ByteBufUtil.utf8MaxBytes(maxLength);
		int j = VarInt.read(buf);
		if (j > i) {
			throw new RuntimeException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i + ")");
		} else if (j < 0) {
			throw new RuntimeException("The received encoded string buffer length is less than zero! Weird string!");
		} else {
			int k = buf.readableBytes();
			if (j > k) {
				throw new RuntimeException("Not enough bytes in buffer, expected " + j + ", but got " + k);
			} else {
				String string = buf.toString(buf.readerIndex(), j, StandardCharsets.UTF_8);
				buf.readerIndex(buf.readerIndex() + j);
				if (string.length() > maxLength) {
					throw new RuntimeException("The received string length is longer than maximum allowed (" + string.length() + " > " + maxLength + ")");
				} else {
					return string;
				}
			}
		}
	}

	public static void write(ByteBuf buf, CharSequence string, int maxLength) {
		if (string.length() > maxLength) {
			throw new RuntimeException("String too big (was " + string.length() + " characters, max " + maxLength + ")");
		} else {
			int i = ByteBufUtil.utf8MaxBytes(string);
			ByteBuf byteBuf = buf.alloc().buffer(i);

			try {
				int j = ByteBufUtil.writeUtf8(byteBuf, string);
				int k = ByteBufUtil.utf8MaxBytes(maxLength);
				if (j > k) {
					throw new RuntimeException("String too big (was " + j + " bytes encoded, max " + k + ")");
				}

				VarInt.write(buf, j);
				buf.writeBytes(byteBuf);
			} finally {
				byteBuf.release();
			}
		}
	}
}
