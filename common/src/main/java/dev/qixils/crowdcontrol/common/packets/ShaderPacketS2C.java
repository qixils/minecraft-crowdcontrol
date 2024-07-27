package dev.qixils.crowdcontrol.common.packets;

import dev.qixils.crowdcontrol.common.packets.util.Utf8String;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Duration;

import static dev.qixils.crowdcontrol.common.Plugin.SHADER_KEY;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class ShaderPacketS2C implements PluginPacket {
	public static final Metadata<ShaderPacketS2C> METADATA = new Metadata<>(SHADER_KEY.asString(), ShaderPacketS2C::new);
	protected static final int SIZE = 64;

	private final String shader;
	private final Duration duration;

	public ShaderPacketS2C(ByteBuf buf) {
		shader = Utf8String.read(buf, SIZE);
		duration = Duration.ofMillis(buf.readLong());
	}

	@Override
	public void write(ByteBuf buf) {
		Utf8String.write(buf, shader, SIZE);
		buf.writeLong(duration.toMillis());
	}

	@Override
	public Metadata<?> metadata() {
		return METADATA;
	}
}
