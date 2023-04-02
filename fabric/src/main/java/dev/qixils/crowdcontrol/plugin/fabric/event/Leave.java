package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.server.network.ServerPlayerEntity;

public record Leave(ServerPlayerEntity player) implements Event {
}
