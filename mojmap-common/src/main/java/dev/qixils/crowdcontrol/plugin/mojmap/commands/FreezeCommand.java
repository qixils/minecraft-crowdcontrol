package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.plugin.mojmap.TimedCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.Location;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.qixils.crowdcontrol.common.CommandConstants.FREEZE_DURATION;

@Getter
public final class FreezeCommand extends TimedCommand {
	public static final Map<UUID, Location> FROZEN_PLAYERS = new HashMap<>();

	private final String effectName = "freeze";
	private final String displayName = "Freeze";

	public FreezeCommand(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	@Override
	public @NotNull Duration getDuration() {
		return FREEZE_DURATION;
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		AtomicReference<Map<UUID, Location>> atomic = new AtomicReference<>();
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration())
				.startCallback($ -> {
					List<ServerPlayer> players = getPlugin().getPlayers(request);
					Map<UUID, Location> locations = new HashMap<>();
					players.forEach(player -> locations.put(player.getUUID(), new Location(player)));
					atomic.set(locations);
					FROZEN_PLAYERS.putAll(locations);
					playerAnnounce(players, request);
					return null;
				})
				.completionCallback($ -> atomic.get().forEach(FROZEN_PLAYERS::remove))
				.build().queue();
	}
}
