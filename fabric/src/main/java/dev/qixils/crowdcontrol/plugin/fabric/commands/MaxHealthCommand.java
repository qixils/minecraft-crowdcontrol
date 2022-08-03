package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static dev.qixils.crowdcontrol.common.CommandConstants.MIN_MAX_HEALTH;

@Getter
public class MaxHealthCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final int amount;
	private static final UUID MODIFIER_UUID = new UUID(-899185282624176127L, -7747914881652381318L);
	private static final String MODIFIER_NAME = "max-health-cc";

	public MaxHealthCommand(FabricCrowdControlPlugin plugin, int amount) {
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

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.FAILURE)
				.message("All players are at minimum health (" + (MIN_MAX_HEALTH / 2) + " hearts)");
		for (ServerPlayer player : players) {
			AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
			if (maxHealth == null) {
				plugin.getSLF4JLogger().warn("Player missing GENERIC_MAX_HEALTH attribute");
				continue;
			}
			AttributeModifier modifier = null;
			for (AttributeModifier attributeModifier : maxHealth.getModifiers()) {
				if (attributeModifier.getId() == MODIFIER_UUID || attributeModifier.getName().equals(MODIFIER_NAME)) {
					if (modifier == null)
						modifier = attributeModifier;
					maxHealth.removeModifier(modifier);
				}
			}
			double current = modifier == null ? 0 : modifier.getAmount();
			double newVal = Math.max(-MIN_MAX_HEALTH, current + amount);
			if (current != newVal)
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			maxHealth.addPermanentModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, newVal, AttributeModifier.Operation.ADDITION));
			float computedMaxHealth = (float) (20 + newVal);
			if (amount > 0)
				player.setHealth(player.getHealth() + amount);
			else
				player.setHealth(Math.min(player.getHealth(), computedMaxHealth));
		}
		return result;
	}
}
