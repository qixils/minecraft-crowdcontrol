package dev.qixils.crowdcontrol.plugin.neoforge.client;

import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import dev.qixils.crowdcontrol.plugin.neoforge.client.neoforge.ClientPacketContextImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "crowdcontrol", dist = Dist.CLIENT)
public class NeoForgePlatformClient extends ModdedPlatformClient {

	public NeoForgePlatformClient(ModContainer container) {
		super();
		onInitializeClient();
		container.registerExtensionPoint(IConfigScreenFactory.class, (cont, screen) -> createConfigScreen(screen));
	}

	@SubscribeEvent
	public void register(final RegisterPayloadHandlersEvent event) {
		// Sets the current network version
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC, (payload, context) -> handleSetShader(payload, new ClientPacketContextImpl(context)));
		registrar.playToClient(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC, (payload, context) -> handleRequestVersion(payload, new ClientPacketContextImpl(context)));
		registrar.playToClient(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC, (payload, context) -> handleMovementStatus(payload, new ClientPacketContextImpl(context)));
	}
}
