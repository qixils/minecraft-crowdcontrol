package dev.qixils.crowdcontrol.common.command.impl.health;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandGroups;
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

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@RequiredArgsConstructor
@Getter
public class DamageCommand<P> implements Command<P> {
	private final @NotNull String effectName = "damage";
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND;
	private final @NotNull Plugin<P, ?> plugin;

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		int amount = request.getQuantity() * 2;
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, CommandGroups.HEALTH_MODIFIERS)) {
				return new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_TEMPORARY,
					"Cannot damage players under the effects of health modifiers"
				);
			}
			boolean success = false;
			for (P rawPlayer : playerSupplier) {
				MCCCPlayer player = plugin.getPlayer(rawPlayer);
				double oldHealth = player.health();
				double newHealth = Math.max(1, oldHealth - amount);
				double appliedDamage = oldHealth - newHealth;
				// don't apply effect unless it is 100% utilized
				if (appliedDamage == amount) {
					player.damage(appliedDamage);
					success = true;
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Players would have been killed by this command");
		}));
	}
}

