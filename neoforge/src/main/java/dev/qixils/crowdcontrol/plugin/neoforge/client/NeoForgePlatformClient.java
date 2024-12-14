package dev.qixils.crowdcontrol.plugin.neoforge.client;

import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.client.neoforge.ClientPacketContextImpl;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.neoforge.PacketUtilImpl;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(value = "crowdcontrol", dist = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class NeoForgePlatformClient extends ModdedPlatformClient {

	public NeoForgePlatformClient(ModContainer container, IEventBus modBus) {
		super();
		onInitializeClient();
		container.registerExtensionPoint(IConfigScreenFactory.class, (cont, screen) -> createConfigScreen(screen));

		modBus.addListener(this::register);
	}

	@SubscribeEvent
	public void register(final RegisterPayloadHandlersEvent event) {
		PacketUtilImpl.registerPackets(event);

		final PayloadRegistrar registrar = event.registrar("1").optional();
		registrar.playToClient(SetShaderS2C.PACKET_ID, SetShaderS2C.PACKET_CODEC, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleSetShader(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		registrar.playToClient(RequestVersionS2C.PACKET_ID, RequestVersionS2C.PACKET_CODEC, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleRequestVersion(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		registrar.playToClient(MovementStatusS2C.PACKET_ID, MovementStatusS2C.PACKET_CODEC, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleMovementStatus(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		registrar.playToClient(SetLanguageS2C.PACKET_ID, SetLanguageS2C.PACKET_CODEC, (payload, context) -> {});
	}
}
