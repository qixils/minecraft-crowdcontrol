package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@Getter
public class ToastCommand extends ImmediateCommand {
	private final String effectName = "toast";
	private final String displayName = "Annoying Pop-Ups";

	public ToastCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (Player player : players) {
				player.playSound(Sounds.ANNOYING, player);
				Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
				player.undiscoverRecipes(recipes);
				player.discoverRecipes(recipes);
				// TODO open an inventory (not sure what to put in it, if anything. poison potatoes?)
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
