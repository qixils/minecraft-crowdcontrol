package dev.qixils.crowdcontrol.plugin.neoforge.client;

import dev.qixils.crowdcontrol.plugin.fabric.client.ModdedPlatformClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "crowdcontrol", dist = Dist.CLIENT)
public class NeoForgePlatformClient extends ModdedPlatformClient {

	public NeoForgePlatformClient(ModContainer container) {
		super();
		onInitializeClient();
		container.registerExtensionPoint(IConfigScreenFactory.class, (cont, screen) -> createConfigScreen(screen));
	}
}
