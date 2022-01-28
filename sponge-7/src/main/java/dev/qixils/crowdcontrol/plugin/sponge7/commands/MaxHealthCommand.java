package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.MIN_MAX_HEALTH;

@Getter
public class MaxHealthCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final int amount;

	public MaxHealthCommand(SpongeCrowdControlPlugin plugin, int amount) {
		super(plugin);
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
		this.displayName = displayText + " Max Health";
		this.amount = amount;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");
		for (Player player : players) {
			MutableBoundedValue<Double> data = player.maxHealth();
			double newVal = data.get() + amount;
			if (newVal < MIN_MAX_HEALTH)
				continue;
			result.type(ResultType.SUCCESS).message("SUCCESS");
			player.offer(data.set(newVal));
			if (amount > 0)
				player.transform(Keys.HEALTH, health -> health == null ? null : health + amount);
		}
		return result;
	}
}
