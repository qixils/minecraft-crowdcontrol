package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Getter
@EventListener
public class FlightCommand extends TimedVoidCommand {
	private final String effectName = "flight";
	private final Duration defaultDuration = Duration.ofSeconds(15);

	public FlightCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(ResultType.RETRY)
							.message("Target is already flying or able to fly");
					for (ServerPlayer player : players) {
						GameType gamemode = player.gameMode.getGameModeForPlayer();
						if (gamemode == GameType.CREATIVE)
							continue;
						if (gamemode == GameType.SPECTATOR)
							continue;
						Abilities abilities = player.getAbilities();
						if (abilities.mayfly)
							continue;
						if (abilities.flying)
							continue;
						response.type(ResultType.SUCCESS).message("SUCCESS");
						sync(() -> {
							abilities.mayfly = true;
							abilities.flying = true;
							player.addDeltaMovement(new Vec3(0, 0.2, 0));
							player.hurtMarked = true;
							player.onUpdateAbilities();
							// TODO: set abilities.flying=true; again after 1 tick
						});
					}
					if (response.type() == ResultType.SUCCESS)
						playerAnnounce(players, request);
					return response;
				})
				.completionCallback($ -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					sync(() -> players.forEach(player -> {
						Abilities abilities = player.getAbilities();
						abilities.mayfly = false;
						abilities.flying = false;
						player.onUpdateAbilities();
					}));
				})
				.build().queue();
	}

	// clear flight on login if they disconnected mid-effect
	@Listener
	public void onJoin(Join event) {
		ServerPlayer player = event.player();
		GameType gamemode = player.gameMode.getGameModeForPlayer();
		if (gamemode == GameType.CREATIVE)
			return;
		if (gamemode == GameType.SPECTATOR)
			return;
		Abilities abilities = player.getAbilities();
		if (!abilities.flying && !abilities.mayfly)
			return;
		abilities.mayfly = false;
		abilities.flying = false;
		player.onUpdateAbilities();
	}
}
