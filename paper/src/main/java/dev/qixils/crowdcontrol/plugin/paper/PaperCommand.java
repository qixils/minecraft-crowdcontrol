package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class PaperCommand implements Command<Player> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final @NotNull PaperCrowdControlPlugin plugin;

	protected PaperCommand(@NotNull PaperCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isEventListener() {
		return this instanceof Listener;
	}
}
