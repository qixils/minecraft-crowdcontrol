package dev.qixils.crowdcontrol.plugin.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.TimedCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

@Getter
public class DisableJumpingCommand extends TimedCommand implements Listener {
	private static final Duration DURATION = Duration.ofSeconds(10);
	private static final int JUMP_BLOCK_DURATION = (int) (DURATION.toSeconds() * 20);

	private final String effectName = "disable_jumping";
	private final String displayName = "Disable Jumping";
	private int jumpsBlockedAt = 0;

	public DisableJumpingCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDuration() {
		return DURATION;
	}

	@Override
	public void voidExecute(@NotNull Request request) {
		new TimedEffect(Objects.requireNonNull(plugin.getCrowdControl(), "CC not initialized"),
				request, DURATION, $ -> {
			this.jumpsBlockedAt = Bukkit.getCurrentTick();
			announce(request);
		}, $ -> {}).queue();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onJumpEvent(PlayerJumpEvent event) {
		if ((jumpsBlockedAt + JUMP_BLOCK_DURATION) >= Bukkit.getCurrentTick())
			event.setCancelled(true);
	}
}
