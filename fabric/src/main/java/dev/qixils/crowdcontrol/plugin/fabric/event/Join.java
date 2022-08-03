package dev.qixils.crowdcontrol.plugin.fabric.event;

import net.minecraft.server.level.ServerPlayer;

public record Join(ServerPlayer player) implements Event {
}
