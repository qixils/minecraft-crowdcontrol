package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.event.EventRegister;
import net.minecraft.server.MinecraftServer;

public class MinecraftEvents {
	public static EventRegister<MinecraftServer> SERVER_STARTING = new EventRegister<>();
	public static EventRegister<MinecraftServer> SERVER_STARTED = new EventRegister<>();
	public static EventRegister<MinecraftServer> SERVER_STOPPING = new EventRegister<>();
	public static EventRegister<MinecraftServer> SERVER_STOPPED = new EventRegister<>();
}
