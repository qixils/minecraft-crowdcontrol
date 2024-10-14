package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class DropItemCommand extends ImmediateCommand {
	private final String effectName = "drop_item";

	public DropItemCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No players were holding items");
		for (ServerPlayer player : players) {
			if (!player.getInventory().getSelected().isEmpty()) {
				sync(() -> {
					player.drop(true);
					// for some reason the player's inventory is not getting updated
					// my code seems identical to the paper implementation, but maybe they have some
					//  weird listener that updates the inventory?
					// either way, this workaround is fine
					player.containerMenu.sendAllDataToRemote();
				});
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
			}
		}
		return result;
	}
}
