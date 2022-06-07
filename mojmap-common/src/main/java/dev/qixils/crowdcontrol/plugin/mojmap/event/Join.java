package dev.qixils.crowdcontrol.plugin.mojmap.event;

import net.minecraft.server.level.ServerPlayer;

public record Join(ServerPlayer player) implements Event {
}
