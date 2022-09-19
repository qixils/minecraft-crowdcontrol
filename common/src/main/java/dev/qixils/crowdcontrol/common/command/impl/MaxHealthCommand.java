package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.ImmediateCommand;
import dev.qixils.crowdcontrol.common.mc.CCPlayer;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.MIN_MAX_HEALTH;

@Getter
@RequiredArgsConstructor
public class MaxHealthCommand<P> implements ImmediateCommand<P> {
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

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull P> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");

		for (P rawPlayer : players) {
			CCPlayer player = plugin.getPlayer(rawPlayer);
			double current = player.maxHealthOffset();
			double newVal = Math.max(-MIN_MAX_HEALTH, current + amount);
			if (current != newVal) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				player.maxHealthOffset(newVal);
				if (amount > 0)
					player.health(player.health() + amount);
			}
		}

		return result;
	}
}
