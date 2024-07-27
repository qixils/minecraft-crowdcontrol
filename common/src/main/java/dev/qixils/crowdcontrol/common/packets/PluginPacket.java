package dev.qixils.crowdcontrol.common.packets;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public interface PluginPacket {
	Logger LOGGER = LoggerFactory.getLogger("CrowdControl/PluginPacket");

	Metadata<?> metadata();
	void write(ByteBuf buf);

	@Data
	@Accessors(fluent = true)
	class Metadata<T extends PluginPacket> {
		private final String channel;
		private final Function<ByteBuf, T> factory;
	}
}
