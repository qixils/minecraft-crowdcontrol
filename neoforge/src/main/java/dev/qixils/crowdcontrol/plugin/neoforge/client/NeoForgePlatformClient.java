package dev.qixils.crowdcontrol.plugin.neoforge.client;

import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.client.neoforge.ClientPacketContextImpl;
import dev.qixils.crowdcontrol.plugin.fabric.packets.MovementStatusS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.RequestVersionS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

@Mod(value = "crowdcontrol", dist = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class NeoForgePlatformClient extends ModdedPlatformClient {

	public NeoForgePlatformClient(ModContainer container, IEventBus modBus) {
		super();

		modBus.addListener(this::registerClient);

		onInitializeClient();
		container.registerExtensionPoint(IConfigScreenFactory.class, (cont, screen) -> createConfigScreen(screen));
	}

	public void registerClient(final RegisterClientPayloadHandlersEvent event) {
		logger.debug("Registering listeners (client)");

		event.register(SetShaderS2C.PACKET_ID, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleSetShader(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		event.register(RequestVersionS2C.PACKET_ID, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleRequestVersion(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		event.register(MovementStatusS2C.PACKET_ID, (payload, context) -> {
			if (!(context.player() instanceof LocalPlayer localPlayer)) return;
			handleMovementStatus(payload, new ClientPacketContextImpl(context, localPlayer));
		});
		event.register(SetLanguageS2C.PACKET_ID, (payload, context) -> {});

		logger.debug("Actually registered listeners (client)");
	}

	@Override
	public void sendToServer(@NotNull CustomPacketPayload payload) {
		// TODO: check can send?
		try {
			ClientPacketDistributor.sendToServer(payload);
		} catch (UnsupportedOperationException e) {
			logger.debug("Server cannot receive packet {}", payload);
		}
	}
}
