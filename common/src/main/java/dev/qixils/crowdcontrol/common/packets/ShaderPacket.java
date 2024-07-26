package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.packets.util.Utf8String;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;

import java.time.Duration;

import static dev.qixils.crowdcontrol.common.Plugin.SHADER_SIZE;

@Data
public class ShaderPacket implements PluginPacket {
	private final String shader;
	private final Duration duration;

	@Override
	public String channel() {
		return Plugin.SHADER_KEY.asString();
	}

	@Override
	public ByteBuf message() {
		ByteBuf buf = Unpooled.buffer();
		Utf8String.write(buf, shader, SHADER_SIZE);
		buf.writeLong(duration.toMillis());
		return buf;
	}
}
