package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

@Getter
public class ItemDamageCommand extends ImmediateCommand {
	private final BiFunction<Integer, Material, Integer> handleItem;
	private final String effectName;
	private final String displayName;

	public ItemDamageCommand(CrowdControlPlugin plugin, boolean repair) {
		super(plugin);
		handleItem = repair
				? (damage, type) -> 0
				: (damage, type) -> (type.getMaxDurability() + damage)/2;
		displayName = (repair ? "Repair" : "Damage") + " Item";
		effectName = displayName.replace(' ', '_');
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY);
		for (Player player : CrowdControlPlugin.getPlayers()) {
			PlayerInventory inv = player.getInventory();
			ItemStack item = inv.getItemInMainHand();
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof Damageable damageable) {
				result.type(Response.ResultType.SUCCESS);
				damageable.setDamage(handleItem.apply(damageable.getDamage(), item.getType()));
			}
			item.setItemMeta(meta);
		}
		return result;
	}
}
