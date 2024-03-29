package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import lombok.Getter;
import net.kyori.adventure.text.serializer.spongeapi.SpongeComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<Player> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final @NotNull SpongeCrowdControlPlugin plugin;
	protected final SpongeComponentSerializer spongeSerializer;

	protected Command(@NotNull SpongeCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
		this.spongeSerializer = Objects.requireNonNull(plugin.getSpongeSerializer());
	}
}
