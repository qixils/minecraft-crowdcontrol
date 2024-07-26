package dev.qixils.crowdcontrol.common.packets;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface PluginPacket {
	Logger LOGGER = LoggerFactory.getLogger("CrowdControl/PluginPacket");
	Map<String, Function<ByteBuf, PluginPacket>> REGISTRY = new HashMap<>();

	String channel();
	ByteBuf message();
}
