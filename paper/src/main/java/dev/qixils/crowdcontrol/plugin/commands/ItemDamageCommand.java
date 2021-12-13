package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
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

import java.util.List;
import java.util.function.BiFunction;

@Getter
public class ItemDamageCommand extends ImmediateCommand {
	private final BiFunction<Integer, Material, Integer> handleItem;
	private final String effectName;
	private final String displayName;

	public ItemDamageCommand(BukkitCrowdControlPlugin plugin, boolean repair) {
		super(plugin);
		handleItem = repair
				? (damage, type) -> 0
				: (damage, type) -> (type.getMaxDurability() + damage) / 2;
		displayName = (repair ? "Repair" : "Damage") + " Item";
		effectName = displayName.replace(' ', '_');
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY).message("Player(s) not holding a damaged item");
		for (Player player : players) {
			PlayerInventory inv = player.getInventory();
			ItemStack item = inv.getItemInMainHand();
			ItemMeta meta = item.getItemMeta();
			// only allowing items with damage because apparently "instanceof Damageable" isn't good enough
			if (meta instanceof Damageable damageable && damageable.hasDamage()) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				damageable.setDamage(handleItem.apply(damageable.getDamage(), item.getType()));
			}
			item.setItemMeta(meta);
		}
		return result;
	}
}
