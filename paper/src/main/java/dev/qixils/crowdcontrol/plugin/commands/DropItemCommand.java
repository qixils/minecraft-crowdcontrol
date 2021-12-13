package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DropItemCommand extends ImmediateCommand {
	public DropItemCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "drop_item";
	private final String displayName = "Drop Held Item";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY).message("No players were holding items");
		for (Player player : players) {
			if (!player.getInventory().getItemInMainHand().getType().isEmpty()) {
				Bukkit.getScheduler().runTask(plugin, () -> {
					player.dropItem(true);
					player.updateInventory();
				});
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return result;
	}
}
