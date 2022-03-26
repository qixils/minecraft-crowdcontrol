package dev.qixils.crowdcontrol.plugin.mojmap.event;

import lombok.experimental.Accessors;
import net.minecraft.server.level.ServerPlayer;

// TODO: impl
@Accessors(fluent = true)
public record Join(ServerPlayer player) implements Event {
}
