package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MojmapTextUtil implements TextUtil {
	private final ModdedCrowdControlPlugin plugin;
	private ComponentFlattener flattener = ComponentFlattener.basic();
	private PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
	private boolean initialized = false;

	public MojmapTextUtil(@NotNull ModdedCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	private void initialize() {
		if (initialized) return;
		plugin.adventureOptional().ifPresent(adventure -> {
			this.initialized = true;
			this.flattener = adventure.flattener();
			this.serializer = PlainTextComponentSerializer.builder().flattener(flattener).build();
		});
	}

	// boilerplate

	@Override
	public @Nullable ComponentFlattener flattener() {
		initialize();
		return flattener;
	}

	@Override
	public @NotNull PlainTextComponentSerializer serializer() {
		initialize();
		return serializer;
	}

	// helper methods for native objects

	public @NotNull String asPlain(@NotNull Component component) {
		return asPlain(plugin.toAdventure(component));
	}
}
