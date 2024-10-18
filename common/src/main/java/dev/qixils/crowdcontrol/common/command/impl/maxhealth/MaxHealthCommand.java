package dev.qixils.crowdcontrol.common.command.impl.maxhealth;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.mc.MCCCPlayer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.MIN_MAX_HEALTH;

@Getter
public class MaxHealthCommand<P> implements Command<P> {
	private final String effectName;
	private final Component displayName;
	private final int amount;
	private final Plugin<P, ?> plugin;

	public MaxHealthCommand(Plugin<P, ?> plugin, int amount) {
		this.plugin = plugin;
		String amountText;
		String displayText;
		if (amount == 0) {
			amountText = "0";
			displayText = "0";
		} else if (amount < 0) {
			amountText = "sub" + (amount * -1);
			displayText = String.valueOf(amount);
		} else {
			amountText = "plus" + amount;
			displayText = "+" + amount;
		}
		this.effectName = "max_health_" + amountText;
		this.displayName = Component.translatable("cc.effect.max_health.name", Component.text(displayText));
		this.amount = amount;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<P> players = playerSupplier.get();
			boolean success = false;
			for (P rawPlayer : players) {
				MCCCPlayer player = plugin.getPlayer(rawPlayer);
				double current = player.maxHealthOffset();
				double newVal = Math.max(-MIN_MAX_HEALTH, current + amount);
				if (current != newVal) {
					success = true;
					player.maxHealthOffset(newVal);
					if (amount > 0)
						player.health(player.health() + amount);
				}
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");
		}));
	}
}
