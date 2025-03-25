package dev.qixils.crowdcontrol.common.command.impl.exp;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
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
public class ExpAddCommand<P> implements Command<P> {
	private final @NotNull String effectName = "xp_add";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		List<P> players = playerSupplier.get();
		int amount = Math.max(1, request.getQuantity());
		sync(() -> players.stream().map(plugin::getPlayer).forEach(player -> player.addXpLevel(amount)));
		ccPlayer.sendResponse(new CCInstantEffectResponse(
			request.getRequestId(),
			ResponseStatus.SUCCESS
		));
	}
}
