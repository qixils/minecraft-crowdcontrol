package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedVoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Join;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

@Getter
@EventListener
public class FlightCommand extends TimedVoidCommand {
	private final String effectName = "flight";
	private final Duration defaultDuration = Duration.ofSeconds(15);

	public FlightCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull ServerPlayerEntity> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.effectGroup("gamemode")
				.duration(getDuration(request))
				.startCallback($ -> {
					List<ServerPlayerEntity> players = plugin.getPlayers(request);
					Response.Builder response = request.buildResponse()
							.type(ResultType.RETRY)
							.message("Target is already flying or able to fly");
					for (ServerPlayerEntity player : players) {
						GameMode gamemode = player.interactionManager.getGameMode();
						if (gamemode == GameMode.CREATIVE)
							continue;
						if (gamemode == GameMode.SPECTATOR)
							continue;
						PlayerAbilities abilities = player.getAbilities();
						if (abilities.allowFlying)
							continue;
						if (abilities.flying)
							continue;
						response.type(ResultType.SUCCESS).message("SUCCESS");
						sync(() -> {
							abilities.allowFlying = true;
							abilities.flying = true;
							player.addVelocity(new Vec3d(0, 0.2, 0));
							player.velocityModified = true;
							player.sendAbilitiesUpdate();
							// TODO: set abilities.flying=true; again after 1 tick
						});
					}
					if (response.type() == ResultType.SUCCESS)
						playerAnnounce(players, request);
					return response;
				})
				.completionCallback($ -> {
					List<ServerPlayerEntity> players = plugin.getPlayers(request);
					sync(() -> players.forEach(player -> {
						PlayerAbilities abilities = player.getAbilities();
						abilities.allowFlying = false;
						abilities.flying = false;
						player.sendAbilitiesUpdate();
					}));
				})
				.build().queue();
	}

	// clear flight on login if they disconnected mid-effect
	@Listener
	public void onJoin(Join event) {
		ServerPlayerEntity player = event.player();
		GameMode gamemode = player.interactionManager.getGameMode();
		if (gamemode == GameMode.CREATIVE)
			return;
		if (gamemode == GameMode.SPECTATOR)
			return;
		PlayerAbilities abilities = player.getAbilities();
		if (!abilities.flying && !abilities.allowFlying)
			return;
		abilities.allowFlying = false;
		abilities.flying = false;
		player.sendAbilitiesUpdate();
	}
}
