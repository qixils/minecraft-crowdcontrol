package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DamageCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;
	private final float amount;

	public DamageCommand(MojmapPlugin plugin, String effectName, String displayName, float amount) {
		super(plugin);
		this.effectName = effectName;
		this.displayName = displayName;
		this.amount = amount;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		boolean success = false;
		for (ServerPlayer player : players) {
			float oldHealth = player.getHealth();
			float maxHealth = player.getMaxHealth();
			if (amount < 0) {
				float newHealth = Math.max(0, Math.min(maxHealth, oldHealth - amount));
				if (newHealth != oldHealth) {
					success = true;
					sync(() -> player.setHealth(newHealth));
				}
			} else if (amount >= Short.MAX_VALUE) {
				success = true;
				sync(() -> player.setHealth(0f));
			} else {
				float newHealth = Math.max(1, oldHealth - amount);
				float appliedDamage = oldHealth - newHealth;
				if (appliedDamage > 0) {
					success = true;
					sync(() -> player.hurt(DamageSource.MAGIC, appliedDamage));
				}
			}
		}

		if (success)
			return request.buildResponse()
					.type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse()
					.type(Response.ResultType.FAILURE)
					.message("Players would have been killed by this command");
	}
}
