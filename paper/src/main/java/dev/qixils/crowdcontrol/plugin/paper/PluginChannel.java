package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class PluginChannel implements PluginMessageListener {
	private final PaperCrowdControlPlugin plugin;
	private final Map<String, BiConsumer<Player, byte[]>> incomingMessageHandlers = new HashMap<>();

	public void registerIncomingPluginChannel(String channel, BiConsumer<Player, byte[]> handler) {
		incomingMessageHandlers.put(channel, handler);
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, this);
	}

	public void registerOutgoingPluginChannel(String channel) {
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel);
	}

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
		plugin.getSLF4JLogger().debug("Received message {} from {}: {}", channel, player.getUniqueId(), new String(message, StandardCharsets.UTF_8));
		var handler = incomingMessageHandlers.get(channel);
		if (handler == null) return;
		handler.accept(player, message);
	}

	public void sendMessage(Player player, PluginPacket packet) {
		player.sendPluginMessage(plugin, packet.channel(), packet.message().copy().array());
	}
}
