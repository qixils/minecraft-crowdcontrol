package dev.qixils.crowdcontrol.common.command.impl.health;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter
public class KillCommand<P> implements Command<P> {
	private final @NotNull String effectName = "kill";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<P> players = playerSupplier.get();
			if (isActive(ccPlayer, "health_modifiers", "freeze")) {
				return new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_TEMPORARY,
					"Cannot kill players under the effects of health or location modifiers"
				);
			}
			var streamers = players.stream().map(plugin::getPlayer);
			sync(() -> streamers.forEach(MCCCPlayer::kill));
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}
}

