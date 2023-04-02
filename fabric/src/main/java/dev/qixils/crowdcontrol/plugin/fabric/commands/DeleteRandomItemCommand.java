package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class DeleteRandomItemCommand extends ImmediateCommand {
	private final String effectName = "delete_random_item";

	public DeleteRandomItemCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players have items");
		for (ServerPlayerEntity player : players) {
			PlayerInventory inv = player.getInventory();
			if (inv.isEmpty())
				continue;
			result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			List<Integer> indices = IntStream.range(0, inv.size()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
			Collections.shuffle(indices);
			sync(() -> {
				for (int i : indices) {
					ItemStack stack = inv.getStack(i);
					if (stack.isEmpty())
						continue;
					inv.setStack(i, ItemStack.EMPTY);
					break;
				}
			});
		}
		return result;
	}
}
