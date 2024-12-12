package dev.qixils.crowdcontrol.plugin.fabric.packets;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record ServerPacketContext(@NotNull ServerPlayer player) {
}
