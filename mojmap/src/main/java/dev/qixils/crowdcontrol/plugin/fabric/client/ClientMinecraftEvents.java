package dev.qixils.crowdcontrol.plugin.fabric.client;

import dev.qixils.crowdcontrol.plugin.fabric.event.EventRegister;
import net.minecraft.client.Minecraft;

public class ClientMinecraftEvents {
	public static EventRegister<Minecraft> CLIENT_STARTED = new EventRegister<>();
	public static EventRegister<Minecraft> CLIENT_STOPPING = new EventRegister<>();
}
