package dev.qixils.crowdcontrol.plugin.commands;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@Getter
public class DisableJumpingCommand extends ImmediateCommand implements Listener {
	private final String effectName = "disable_jumping";
	private final String displayName = "Disable Jumping";
	private int jumpsBlockedAt = 0;
	private static final int JUMP_BLOCK_DURATION = 200;

	public DisableJumpingCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		jumpsBlockedAt = Bukkit.getCurrentTick();
		return Response.builder().type(Response.ResultType.SUCCESS).timeRemaining(Duration.ofMillis(JUMP_BLOCK_DURATION * 500));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onJumpEvent(PlayerJumpEvent event) {
		if ((jumpsBlockedAt + JUMP_BLOCK_DURATION) >= Bukkit.getCurrentTick())
			event.setCancelled(true);
	}
}
