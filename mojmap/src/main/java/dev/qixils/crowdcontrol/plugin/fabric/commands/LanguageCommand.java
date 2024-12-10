package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.packets.util.LanguageState;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.PacketUtil;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetLanguageS2C;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Getter
public class LanguageCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull String effectName = "language_random";
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);
	private final @NotNull SemVer minimumModVersion = LanguageState.RANDOM.addedIn();
	@Accessors(fluent = true)
	private final @NotNull Set<ExtraFeature> requiredExtraFeatures = EnumSet.of(ExtraFeature.LANGUAGE_RELOAD);

	public LanguageCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<List<ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players already have an active language effect");
			List<ServerPlayer> players = playerSupplier.get();

			Duration duration = Duration.ofSeconds(request.getEffect().getDuration());
			SetLanguageS2C packet = new SetLanguageS2C(LanguageState.RANDOM, duration);

			// send packet
			for (ServerPlayer player : players)
				PacketUtil.sendToPlayer(player, packet);

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, duration.toMillis());
		}));
	}
}
