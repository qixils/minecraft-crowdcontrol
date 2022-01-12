package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.Command<Player> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final PaperCrowdControlPlugin plugin;

	protected Command(@NotNull PaperCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	protected void sync(Runnable runnable) {
		Bukkit.getScheduler().runTask(plugin, runnable);
	}
}