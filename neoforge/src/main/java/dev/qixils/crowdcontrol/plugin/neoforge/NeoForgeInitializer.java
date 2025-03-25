package dev.qixils.crowdcontrol.plugin.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod("crowdcontrol")
public class NeoForgeInitializer {
	public static ModContainer container;
	public static NeoForgeCrowdControlPlugin plugin;

	public NeoForgeInitializer(ModContainer container, IEventBus modBus) {
		NeoForgeInitializer.container = container;
		NeoForgeInitializer.plugin = new NeoForgeCrowdControlPlugin(container, modBus);
	}
}
