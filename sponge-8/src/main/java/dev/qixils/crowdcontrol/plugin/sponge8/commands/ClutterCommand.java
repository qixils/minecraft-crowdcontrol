package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
public class ClutterCommand extends ImmediateCommand {
	private final String effectName = "clutter";

	public ClutterCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// swaps random items in player's inventory
		boolean success = false;
		try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
			frame.pushCause(plugin.getPluginContainer());
			for (ServerPlayer player : players) {
				PlayerInventory inventory = player.inventory();
				List<ItemStack> original = inventory.slots().stream().map(Inventory::peek).collect(Collectors.toList());
				List<ItemStack> shuffled = new ArrayList<>(original);
				Collections.shuffle(shuffled);

				if (shuffled.equals(original)) continue;

				for (int i = 0; i < shuffled.size(); i++) {
					inventory.set(i, shuffled.get(i));
				}
				success = true;
			}
			frame.popCause();
		}
		if (success)
			return request.buildResponse().type(Response.ResultType.SUCCESS);
		else
			return request.buildResponse().type(Response.ResultType.RETRY).message("Could not find items to swap");
	}
}
