package dev.qixils.crowdcontrol.common.packets;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.function.Function;

public interface PluginPacket {
	Metadata<?> metadata();
	void write(ByteBuf buf);

	@Data
	@Accessors(fluent = true)
	class Metadata<T extends PluginPacket> {
		private final String channel;
		private final Function<ByteBuf, T> factory;
	}
}
