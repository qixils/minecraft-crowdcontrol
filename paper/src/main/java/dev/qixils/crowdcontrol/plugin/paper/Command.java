package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import net.minecraft.world.flag.FeatureElement;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<Player> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final PaperCrowdControlPlugin plugin;

	protected Command(@NotNull PaperCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isEventListener() {
		return this instanceof Listener;
	}

	@Override
	public TriState isVisible() {
		if (this instanceof FeatureElement element)
			return TriState.fromBoolean(PaperCrowdControlPlugin.isEnabled(element));
		return TriState.UNKNOWN; // avoid sending any packet
	}
}
