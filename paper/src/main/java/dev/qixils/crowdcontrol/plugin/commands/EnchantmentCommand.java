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
        final String translate = plugin.getTextUtil().translate(enchantment);
        this.effectName = "enchant_" + translate.replace(' ', '_');
        this.displayName = "Apply " + plugin.getTextUtil().asPlain(enchantment.displayName(enchantment.getMaxLevel()));
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        int level = enchantment.getMaxLevel();
        Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY);
        for (Player player : players) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isEmpty())
                item = player.getInventory().getItemInOffHand();
            if (item.getType().isEmpty())
                continue;
            if (item.getEnchantmentLevel(enchantment) == level)
                continue;
            if (!enchantment.getItemTarget().includes(item))
                continue;
            item.addUnsafeEnchantment(enchantment, level);
            result.type(Response.ResultType.SUCCESS);
        }
        return result;
    }
}
