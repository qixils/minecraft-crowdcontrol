package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends ImmediateCommand {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players have items");
		for (ServerPlayer player : players) {
			Inventory inv = player.getInventory();
			if (inv.isEmpty())
				continue;
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			List<Integer> indices = IntStream.range(0, inv.getContainerSize()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			Collections.shuffle(indices);
			sync(() -> {
				for (int i : indices) {
					ItemStack stack = inv.getItem(i);
					if (stack.isEmpty())
						continue;
					inv.setItem(i, ItemStack.EMPTY);
					break;
				}
			});
		}
		return result;
	}
}
