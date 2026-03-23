package dev.qixils.crowdcontrol.plugin.fabric.packets.fabric;

import dev.qixils.crowdcontrol.common.packets.ExtraFeaturePacketC2S;
import dev.qixils.crowdcontrol.common.packets.VersionResponsePacketC2S;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.ServerPacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtilImpl {
	private static final Logger log = LoggerFactory.getLogger("CrowdControl/PacketUtil");
	private static boolean registered = false;

	public static void registerPackets() {
		if (registered) return;
		registered = true;

		ServerSidePacketRegistry.INSTANCE.register(new ResourceLocation(VersionResponsePacketC2S.METADATA.channel()), (ctx, buf) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			if (!(ctx.getPlayer() instanceof ServerPlayer serverPlayer)) return;
			FabricCrowdControlPlugin.getInstance().handleVersionResponse(new VersionResponsePacketC2S(buf), new ServerPacketContext(serverPlayer));
		});
		ServerSidePacketRegistry.INSTANCE.register(new ResourceLocation(ExtraFeaturePacketC2S.METADATA.channel()), (ctx, buf) -> {
			if (!FabricCrowdControlPlugin.isInstanceAvailable()) return;
			if (!(ctx.getPlayer() instanceof ServerPlayer serverPlayer)) return;
			FabricCrowdControlPlugin.getInstance().handleExtraFeatures(new ExtraFeaturePacketC2S(buf), new ServerPacketContext(serverPlayer));
		});
	}
}
