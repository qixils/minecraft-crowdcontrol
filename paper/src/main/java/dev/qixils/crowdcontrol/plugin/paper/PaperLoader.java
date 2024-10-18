package dev.qixils.crowdcontrol.plugin.paper;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperLoader extends JavaPlugin {
	private final PaperCrowdControlPlugin plugin = new PaperCrowdControlPlugin(this);

	@Override
	public void onLoad() {
		super.onLoad();
		plugin.onLoad();
	}

	@Override
	public void onEnable() {
		super.onEnable();
		plugin.onEnable();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		plugin.onDisable();
	}
}
