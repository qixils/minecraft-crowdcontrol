package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public class RemoveEnchantsCommand extends Command {
	public RemoveEnchantsCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "remove_enchants";
	private final String displayName = "Remove Enchants";

	@Override
	public Response.@NotNull Result execute(@NotNull Request request) {
		Response.Result result = Response.Result.RETRY;
		for (Player player : CrowdControlPlugin.getPlayers()) {
			ItemStack item = player.getInventory().getItemInMainHand();
			Set<Enchantment> enchants = item.getEnchantments().keySet();
			if (!enchants.isEmpty()) {
				result = Response.Result.SUCCESS;
				enchants.forEach(item::removeEnchantment);
			}
		}
		return result;
	}
}
