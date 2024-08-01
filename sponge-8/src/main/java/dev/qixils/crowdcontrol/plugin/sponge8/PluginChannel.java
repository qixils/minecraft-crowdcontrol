package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.function.BiConsumer;

public interface PluginChannel {

	default <T extends PluginPacket> void registerIncomingPluginChannel(PluginPacket.Metadata<T> type, BiConsumer<ServerPlayer, T> consumer) {
	}

	default void registerOutgoingPluginChannel(PluginPacket.Metadata<?> type) {
	}

	default void sendMessage(ServerPlayer player, PluginPacket packet) {
	}
}
