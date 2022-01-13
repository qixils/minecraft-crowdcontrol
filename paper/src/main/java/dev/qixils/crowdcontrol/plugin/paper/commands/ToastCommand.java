package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

@Getter
public class ToastCommand extends ImmediateCommand {
	private final String effectName = "toast";
	private final String displayName = "Obnoxious Pop-Ups";

	public ToastCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (Player player : players) {
				player.playSound(Sound.sound(org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, Source.MASTER, 1, 1), player);
				Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
				player.undiscoverRecipes(recipes);
				player.discoverRecipes(recipes);
				// TODO open an inventory (not sure what to put in it, if anything. poison potatoes?)
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
