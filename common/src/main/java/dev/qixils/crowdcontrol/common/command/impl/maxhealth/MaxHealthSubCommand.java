package dev.qixils.crowdcontrol.common.command.impl.maxhealth;

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

import static dev.qixils.crowdcontrol.common.command.CommandConstants.MIN_MAX_HEALTH;

@Getter
@RequiredArgsConstructor
public class MaxHealthSubCommand<P> implements Command<P> {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull String effectName = "max_health_sub";
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		int amount = request.getQuantity();
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<P> players = playerSupplier.get();
			boolean success = false;
			for (P rawPlayer : players) {
				MCCCPlayer player = plugin.getPlayer(rawPlayer);
				double current = player.maxHealthOffset();
				double newVal = Math.max(-MIN_MAX_HEALTH, current - amount);
				if ((current - newVal) == amount) {
					sync(() -> player.maxHealthOffset(newVal)); // TODO: this was a broader sync: make sync a supplier?
					success = true;
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");
		}));
	}
}
