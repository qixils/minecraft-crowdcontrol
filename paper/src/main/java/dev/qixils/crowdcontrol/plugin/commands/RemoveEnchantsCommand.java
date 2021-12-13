package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

@Getter
public class RemoveEnchantsCommand extends ImmediateCommand {
	public RemoveEnchantsCommand(BukkitCrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "remove_enchants";
	private final String displayName = "Remove Enchants";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY);
		for (Player player : players) {
			ItemStack item = player.getInventory().getItemInMainHand();
			Set<Enchantment> enchants = item.getEnchantments().keySet();
			if (!enchants.isEmpty()) {
				result.type(Response.ResultType.SUCCESS);
				enchants.forEach(item::removeEnchantment);
			}
		}
		return result;
	}
}
