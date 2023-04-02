package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.server.network.ServerPlayerEntity;

public record Join(ServerPlayerEntity player) implements Event {
}
