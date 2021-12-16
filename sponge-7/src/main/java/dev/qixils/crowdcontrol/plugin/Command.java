package dev.qixils.crowdcontrol.plugin;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.Command<Player> {
	protected static final Random random = new Random();
	@Getter
	protected final SpongeCrowdControlPlugin plugin;
	@Getter
	private final boolean isEventListener;

	protected Command(@NotNull SpongeCrowdControlPlugin plugin, boolean isEventListener) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.isEventListener = isEventListener;
	}

	protected Command(@NotNull SpongeCrowdControlPlugin plugin) {
		this(plugin, false);
	}
}
