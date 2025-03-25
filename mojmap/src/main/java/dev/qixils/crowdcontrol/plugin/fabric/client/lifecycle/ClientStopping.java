package dev.qixils.crowdcontrol.plugin.fabric.client.lifecycle;

import dev.qixils.crowdcontrol.plugin.fabric.event.Event;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public record ClientStopping(Minecraft client) implements Event {
}
