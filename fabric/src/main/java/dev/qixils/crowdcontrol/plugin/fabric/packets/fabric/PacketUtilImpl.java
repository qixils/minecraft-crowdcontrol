package dev.qixils.crowdcontrol.plugin.fabric.packets.fabric;

import dev.qixils.crowdcontrol.common.packets.ExtraFeaturePacketC2S;
import dev.qixils.crowdcontrol.common.packets.VersionResponsePacketC2S;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered = false;

	public static void registerPackets() {
		if (registered) return;
		registered = true;

		ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation(VersionResponsePacketC2S.METADATA.channel()), ($1, serverPlayer, $3, buf, $5) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			FabricCrowdControlPlugin.getInstance().handleVersionResponse(new VersionResponsePacketC2S(buf), new ServerPacketContext(serverPlayer));
		});
		ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation(ExtraFeaturePacketC2S.METADATA.channel()), ($1, serverPlayer, $3, buf, $5) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			FabricCrowdControlPlugin.getInstance().handleExtraFeatures(new ExtraFeaturePacketC2S(buf), new ServerPacketContext(serverPlayer));
		});
	}
}
