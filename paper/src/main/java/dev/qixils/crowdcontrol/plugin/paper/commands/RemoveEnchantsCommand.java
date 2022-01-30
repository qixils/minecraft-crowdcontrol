package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

@Getter
public class RemoveEnchantsCommand extends ImmediateCommand {
	private final String effectName = "remove_enchants";
	private final String displayName = "Remove Enchants";

	public RemoveEnchantsCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("Target was not holding an enchanted item");

		for (Player player : players) {
			PlayerInventory inv = player.getInventory();
			if (tryRemoveEnchants(result, inv.getItemInMainHand()))
				continue;
			if (tryRemoveEnchants(result, inv.getItemInOffHand()))
				continue;
			if (tryRemoveEnchants(result, inv.getChestplate()))
				continue;
			if (tryRemoveEnchants(result, inv.getLeggings()))
				continue;
			if (tryRemoveEnchants(result, inv.getHelmet()))
				continue;
			tryRemoveEnchants(result, inv.getBoots());
		}
		return result;
	}

	private boolean tryRemoveEnchants(Response.@NotNull Builder result, @Nullable ItemStack item) {
		if (item == null)
			return false;
		if (item.getType().isEmpty())
			return false;
		if (!item.hasItemMeta())
			return false;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasEnchants())
			return false;
		// immutable copy of enchants
		Set<Enchantment> enchants = meta.getEnchants().keySet();
		if (enchants.isEmpty())
			return false;
		result.type(Response.ResultType.SUCCESS).message("SUCCESS");
		enchants.forEach(item::removeEnchantment);
		return true;
	}
}
