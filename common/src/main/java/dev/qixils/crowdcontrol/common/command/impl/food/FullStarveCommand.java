package dev.qixils.crowdcontrol.common.command.impl.food;

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

@Getter
@RequiredArgsConstructor
public class FullStarveCommand<P> implements Command<P> {
	private final @NotNull String effectName = "full_starve";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			boolean success = false;
			for (P rawPlayer : playerSupplier) {
				MCCCPlayer player = plugin.getPlayer(rawPlayer);

				if (player.foodLevel() > 0) {
					sync(() -> {
						player.foodLevel(0);
						player.saturation(0);
					});
					success = true;
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Player's hunger is already empty");
		}));
	}
}
