package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class PluginChannel implements PluginMessageListener {
	private final PaperCrowdControlPlugin plugin;
	private final Map<String, BiConsumer<Player, byte[]>> incomingMessageHandlers = new HashMap<>();

	public <T extends PluginPacket> void registerIncomingPluginChannel(PluginPacket.Metadata<T> type, BiConsumer<Player, T> handler) {
		incomingMessageHandlers.put(type.channel(), ((player, bytes) -> handler.accept(player, type.factory().apply(Unpooled.wrappedBuffer(bytes)))));
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin.getPaperPlugin(), type.channel(), this);
	}

	public void registerOutgoingPluginChannel(PluginPacket.Metadata<?> type) {
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin.getPaperPlugin(), type.channel());
	}

	@Override
	@ApiStatus.Internal
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
		plugin.getSLF4JLogger().debug("Received message {} from {}: {}", channel, player.getUniqueId(), new String(message, StandardCharsets.UTF_8));
		var handler = incomingMessageHandlers.get(channel);
		if (handler == null) return;
		handler.accept(player, message);
	}

	public void sendMessage(Player player, PluginPacket packet) {
		ByteBuf buf = Unpooled.buffer();
		packet.write(buf);
		player.sendPluginMessage(plugin.getPaperPlugin(), packet.metadata().channel(), buf.copy().array());
	}
}
