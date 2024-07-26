package dev.qixils.crowdcontrol.plugin.sponge8;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class SpongeAddon {
	private final SpongeCrowdControlPlugin plugin;

	public SpongeAddon(SpongeCrowdControlPlugin plugin) {
		this.plugin = plugin;
		plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), this);
	}

	@Listener
	public void onQuit(ServerSideConnectionEvent.Leave event) {
		plugin.onPlayerLeave(event.player());
	}
}
