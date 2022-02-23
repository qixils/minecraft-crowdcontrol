package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.MinecraftMath;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
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
	private static Vector3d asItemVector(final Vector3d headRotation) {
		Random rng = RandomUtil.RNG;

		double xRot = headRotation.x();
		double yRot = headRotation.y();
		double f1 = MinecraftMath.sin(xRot * SIN_MULT_CONSTANT);
		double f2 = MinecraftMath.cos(xRot * SIN_MULT_CONSTANT);
		double f3 = MinecraftMath.sin(yRot * SIN_MULT_CONSTANT);
		double f4 = MinecraftMath.cos(yRot * SIN_MULT_CONSTANT);
		double f5 = rng.nextDouble() * 6.2831855d;
		double f6 = 0.02d * rng.nextDouble();

		// these don't use the weird sine table for some reason
		double x = (-f3 * f2 * 0.3d) + Math.cos(f5) * f6;
		double y = -f1 * 0.3d + 0.1d + (rng.nextDouble() - rng.nextDouble()) * 0.1d;
		double z = (f4 * f2 * 0.3d) + Math.sin(f5) * f6;
		// todo why the hell is this broken
		return new Vector3d(x, y, z);
	}

	public static boolean dropItem(SpongeCrowdControlPlugin plugin, ServerPlayer player) {
		for (HandType hand : plugin.registryIterable(RegistryTypes.HAND_TYPE)) {
			ItemStack itemStack = player.itemInHand(hand);
			if (itemStack.isEmpty())
				continue;

			Vector3d rotation = asItemVector(player.headRotation().get());

			// spawn the entity
			plugin.getSyncExecutor().execute(() -> {
				Entity item = player.world().createEntity(
						EntityTypes.ITEM,
						player.position().add(0, 1.4, 0)
				);
				item.offer(Keys.ITEM_STACK_SNAPSHOT, itemStack.createSnapshot());
				item.velocity().set(rotation);
				item.offer(Keys.PICKUP_DELAY, Ticks.of(40));

				try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
					frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
					player.world().spawnEntity(item);
					player.setItemInHand(hand, null);
				}
			});
			return true;
		}
		return false;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No players were holding items");

		for (ServerPlayer player : players) {
			if (dropItem(plugin, player))
				response.type(ResultType.SUCCESS).message("SUCCESS");
		}

		return response;
	}
}
