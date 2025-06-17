package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.plugin.fabric.event.EventRegister;
import net.minecraft.server.TheGame;

public class MinecraftEvents {
	public static EventRegister<TheGame> SERVER_STARTING = new EventRegister<>();
	public static EventRegister<TheGame> SERVER_STARTED = new EventRegister<>();
	public static EventRegister<TheGame> SERVER_STOPPING = new EventRegister<>();
	public static EventRegister<TheGame> SERVER_STOPPED = new EventRegister<>();
}
