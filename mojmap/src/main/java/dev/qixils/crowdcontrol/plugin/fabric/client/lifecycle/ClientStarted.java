package dev.qixils.crowdcontrol.plugin.fabric.client.lifecycle;

import dev.qixils.crowdcontrol.plugin.fabric.event.Event;
import net.minecraft.client.Minecraft;

//@Environment(EnvType.CLIENT)
public record ClientStarted(Minecraft client) implements Event {
}
