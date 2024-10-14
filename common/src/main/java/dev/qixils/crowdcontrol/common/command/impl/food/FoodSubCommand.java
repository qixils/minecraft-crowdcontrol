package dev.qixils.crowdcontrol.common.command.impl.food;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
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
public class FoodSubCommand<P> implements Command<P> {
	private final @NotNull String effectName = "starve";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		int amount = request.getQuantity() * 2;
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			boolean success = false;
			for (P rawPlayer : playerSupplier) {
				MCCCPlayer player = plugin.getPlayer(rawPlayer);
				int currFood = player.foodLevel();
				double currSaturation = player.saturation();

				int newFood = Math.max(0, currFood - amount);
				double newSaturation = Math.min(newFood, currSaturation);
				// don't apply effect unless it is 100% utilized
				if ((currFood - newFood) == amount) {
					sync(() -> {
						player.foodLevel(newFood);
						player.saturation(newSaturation);
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
