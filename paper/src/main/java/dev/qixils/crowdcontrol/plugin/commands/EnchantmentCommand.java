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

@Getter
public class EnchantmentCommand extends ImmediateCommand {
    protected final Enchantment enchantment;
    private final String effectName;
    private final String displayName;

    public EnchantmentCommand(BukkitCrowdControlPlugin plugin, Enchantment enchantment) {
        super(plugin);
        this.enchantment = enchantment;
        this.effectName = "enchant_" + plugin.getTextUtil().translate(enchantment).replace(' ', '_');
        this.displayName = "Apply " + plugin.getTextUtil().asPlain(enchantment.displayName(enchantment.getMaxLevel()));
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Response.Builder result = request.buildResponse()
                .type(Response.ResultType.RETRY)
                .message("No items could be enchanted");
        for (Player player : players) {
			int level = enchantment.getMaxLevel();
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType().isEmpty()) {
				item = player.getInventory().getItemInOffHand();
				if (item.getType().isEmpty())
					continue;
			}
			int curLevel = item.getEnchantmentLevel(enchantment);
			if (curLevel >= level)
				level = curLevel + 1;
			else if (curLevel == 0 && !enchantment.getItemTarget().includes(item))
				continue;
			item.addUnsafeEnchantment(enchantment, level);
			result.type(Response.ResultType.SUCCESS);
		}
        return result;
    }
}
