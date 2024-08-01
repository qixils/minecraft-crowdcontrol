package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent;
import org.spongepowered.api.network.ServerConnectionState;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class PluginChannelImpl implements PluginChannel {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PluginChannel");
	private final Map<Key, RawDataChannel> channels = new HashMap<>();
	private final Map<ResourceKey, @Nullable BiConsumer<ServerPlayer, ChannelBuf>> pendingChannels = new HashMap<>();
	private boolean canRegister = true;

	@Listener
	public void onRegisterChannels(RegisterChannelEvent event) {
		for (Map.Entry<ResourceKey, BiConsumer<ServerPlayer, ChannelBuf>> entry : pendingChannels.entrySet()) {
			ResourceKey key = entry.getKey();
			BiConsumer<ServerPlayer, ChannelBuf> consumer = entry.getValue();

			RawDataChannel channel = event.register(key, RawDataChannel.class);
			if (consumer != null) {
				channel.play().addHandler(ServerConnectionState.Game.class, (buf, state) -> consumer.accept(state.player(), buf));
			} else {
				channels.put(key, channel);
			}
		}
		canRegister = false;
		pendingChannels.clear();
	}

	public <T extends PluginPacket> void registerIncomingPluginChannel(PluginPacket.Metadata<T> type, BiConsumer<ServerPlayer, T> consumer) {
		if (!canRegister) throw new IllegalStateException("Too late to register channels");
		pendingChannels.put(ResourceKey.resolve(type.channel()), (player, buf) -> consumer.accept(player, type.factory().apply((ByteBuf) buf)));
	}

	public void registerOutgoingPluginChannel(PluginPacket.Metadata<?> type) {
		if (!canRegister) throw new IllegalStateException("Too late to register channels");
		pendingChannels.put(ResourceKey.resolve(type.channel()), null);
	}

	public void sendMessage(ServerPlayer player, PluginPacket packet) {
		RawDataChannel channel = channels.get(ResourceKey.resolve(packet.metadata().channel()));
		if (channel == null) throw new IllegalArgumentException("Unknown packet " + packet.metadata().channel());
		log.info("Sending message {} to {}", packet.metadata().channel(), player.uniqueId());
		channel.play().sendTo(player, buf -> packet.write((ByteBuf) buf));
	}
}
