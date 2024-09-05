package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.TimedImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class LanguageCommand extends TimedImmediateCommand {
	private static final @NotNull Set<UUID> ACTIVE = new HashSet<>();
	private final @NotNull String effectName = "language_random";
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);
	private final @NotNull SemVer minimumModVersion = LanguageState.RANDOM.addedIn();
	@Accessors(fluent = true)
	private final @NotNull Set<ExtraFeature> requiredExtraFeatures = EnumSet.of(ExtraFeature.LANGUAGE_RELOAD);

	public LanguageCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		players.removeIf(player -> ACTIVE.contains(player.getUUID()));
		if (players.isEmpty())
			return request.buildResponse().type(Response.ResultType.RETRY).message("All players already have an active language effect");

		// create byte buf
		Duration duration = getDuration(request);
		SetLanguageS2C packet = new SetLanguageS2C(LanguageState.RANDOM, duration);

		// send packet
		for (ServerPlayer player : players) {
			ACTIVE.add(player.getUUID());
			ServerPlayNetworking.send(player, packet);
		}

		// schedule removal
		plugin.getScheduledExecutor().schedule(
			() -> players.forEach(player -> ACTIVE.remove(player.getUUID())),
			duration.toMillis(),
			TimeUnit.MILLISECONDS
		);

		return request.buildResponse().type(Response.ResultType.SUCCESS).timeRemaining(duration);
	}
}
