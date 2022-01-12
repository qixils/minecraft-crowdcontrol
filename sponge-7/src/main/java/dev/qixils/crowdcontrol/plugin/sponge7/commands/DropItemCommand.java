package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.MinecraftMath;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Getter
public class DropItemCommand extends ImmediateCommand {
	private static final double SIN_MULT_CONSTANT = 0.017453292d;
	private final String effectName = "drop_item";
	private final String displayName = "Drop Held Item";

	public DropItemCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	// rotation->vector stuff that is totally definitely not decompiled/reverse-engineered
	private static Vector3d asItemVector(Vector3d headRotation) {
		Random rng = RandomUtil.RNG;

		double xRot = headRotation.getX();
		double yRot = headRotation.getY();
		double f1 = MinecraftMath.sin(xRot * SIN_MULT_CONSTANT);
		double f2 = MinecraftMath.cos(xRot * SIN_MULT_CONSTANT);
		double f3 = MinecraftMath.sin(yRot * SIN_MULT_CONSTANT);
		double f4 = MinecraftMath.cos(yRot * SIN_MULT_CONSTANT);
		double f5 = rng.nextFloat() * 6.2831855d;
		double f6 = 0.02d * RandomUtil.RNG.nextFloat();

		// these don't use the weird sine table for some reason
		double x = (-f3 * f2 * 0.3d) + Math.cos(f5) * f6;
		double y = -f1 * 0.3d + 0.1d + (rng.nextDouble() - rng.nextDouble()) * 0.1d;
		double z = (f4 * f2 * 0.3d) + Math.sin(f5) * f6;
		return new Vector3d(x, y, z);
	}

	public static boolean dropItem(Game game, Player player) {
		for (HandType hand : game.getRegistry().getAllOf(HandType.class)) {
			Optional<ItemStack> optionalItem = player.getItemInHand(hand);
			if (!optionalItem.isPresent())
				continue;
			ItemStack itemStack = optionalItem.get();
			if (itemStack.isEmpty())
				continue;
			// API8: drop item naturally if available
			Vector3d rotation = asItemVector(player.getHeadRotation());
			Entity item = player.getLocation() // API8: use eye location (if available)
					.add(0, 1.4, 0)
					.createEntity(EntityTypes.ITEM);
			item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot());
			item.setVelocity(rotation);
			item.offer(Keys.PICKUP_DELAY, 40);

			// spawn the entity
			try (StackFrame frame = game.getCauseStackManager().pushCauseFrame()) {
				frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
				player.getWorld().spawnEntity(item);
				player.setItemInHand(hand, null);
			}
			return true;
		}
		return false;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No players were holding items");

		for (Player player : players) {
			if (dropItem(plugin.getGame(), player))
				response.type(ResultType.SUCCESS).message("SUCCESS");
		}

		return response;
	}
}
