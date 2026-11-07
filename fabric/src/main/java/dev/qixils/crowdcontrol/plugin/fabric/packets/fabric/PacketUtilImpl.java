package dev.qixils.crowdcontrol.plugin.fabric.packets.fabric;

import dev.qixils.crowdcontrol.common.packets.PluginPacket;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ExtraFeatureC2S;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ResponseVersionC2S;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ServerPacketContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered = false;

	public static @NotNull ResourceLocation channel(@NotNull PluginPacket payload) {
		return new ResourceLocation(payload.metadata().channel());
	}

	public static @NotNull FriendlyByteBuf encode(@NotNull PluginPacket payload) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		payload.write(buf);
		return buf;
	}

	public static void registerPackets() {
		if (registered) return;
		registered = true;

		ServerPlayNetworking.registerGlobalReceiver(ResponseVersionC2S.PACKET_ID, (server, player, handler, buf, responseSender) -> {
			ResponseVersionC2S payload = new ResponseVersionC2S(buf);
			server.execute(() -> {
				if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
				FabricCrowdControlPlugin.getInstance().handleVersionResponse(payload, new ServerPacketContext(player));
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(ExtraFeatureC2S.PACKET_ID, (server, player, handler, buf, responseSender) -> {
			ExtraFeatureC2S payload = new ExtraFeatureC2S(buf);
			server.execute(() -> {
				if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
				FabricCrowdControlPlugin.getInstance().handleExtraFeatures(payload, new ServerPacketContext(player));
			});
		});
	}
}
