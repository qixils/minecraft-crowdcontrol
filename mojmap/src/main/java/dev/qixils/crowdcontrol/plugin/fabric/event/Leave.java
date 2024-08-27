package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.server.level.ServerPlayer;

public record Leave(ServerPlayer player) implements Event {
}
