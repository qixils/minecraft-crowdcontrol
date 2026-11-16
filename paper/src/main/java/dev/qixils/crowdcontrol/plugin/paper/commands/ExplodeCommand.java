package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.explosionPower;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.shouldSpawnFire;

@Getter
public class ExplodeCommand extends RegionalCommandSync implements Listener {
	private final String effectName = "explode";
	private static final Set<UUID> EXPLODING = Collections.synchronizedSet(new HashSet<>());

	public ExplodeCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Explosions were cancelled by another plugin");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		float power = (float) explosionPower();
		// whether the explosion should place fire blocks (5% chance)
		boolean fire = shouldSpawnFire();

		EXPLODING.add(player.getUniqueId());
		boolean success = player.getWorld().createExplosion(
			player.getLocation(),
			power,
			fire,
			true
		);
		EXPLODING.remove(player.getUniqueId());

		if (success) {
			player.setVelocity(new Vector(0, 1, 0));
			return true;
		}

		return false;
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return; // must be player
		if (EXPLODING.isEmpty()) return; // SOMEONE must be exploding
		var damageType = event.getDamageSource().getDamageType();
		if (damageType != DamageType.EXPLOSION && damageType != DamageType.PLAYER_EXPLOSION) return; // must be explosion

		event.setCancelled(true);
	}
}
