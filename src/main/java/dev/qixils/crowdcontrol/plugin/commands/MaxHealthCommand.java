package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class MaxHealthCommand extends Command {
	private final String effectName;
	private final String displayName;
	private final int amount;
	private static final UUID modifierUUID = new UUID(-899185282624176127L, -7747914881652381318L);

	public MaxHealthCommand(CrowdControlPlugin plugin, int amount) {
		super(plugin);
		String amountText;
		if (amount == 0)
			amountText = "0";
		else if (amount < 0)
			amountText = String.valueOf(amount);
		else
			amountText = "+" + amount;
		this.effectName = "max-health" + amountText;
		this.displayName = amountText + " Max Health";
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Response.Result result = new Response.Result(Response.ResultType.FAILURE, "All players are at minimum health");
		for (Player player : CrowdControlPlugin.getPlayers()) {
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (maxHealth == null) {
				plugin.getLogger().fine("Player missing GENERIC_MAX_HEALTH attribute?");
				continue;
			}
			AttributeModifier modifier = null;
			for (AttributeModifier attributeModifier : maxHealth.getModifiers()) {
				if (attributeModifier.getUniqueId() == modifierUUID) {
					modifier = attributeModifier;
					maxHealth.removeModifier(modifier);
					break;
				}
			}
			double current = modifier == null ? 0 : modifier.getAmount();
			double newVal = Math.max(-10, current + amount);
			if (current != newVal)
				result = Response.Result.SUCCESS;
			maxHealth.addModifier(new AttributeModifier(modifierUUID, "max-health-cc", newVal, AttributeModifier.Operation.ADD_NUMBER));
		}
		return result;
	}
}
