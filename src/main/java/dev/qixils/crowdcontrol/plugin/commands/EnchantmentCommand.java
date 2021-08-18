package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class EnchantmentCommand extends Command {
    protected final Enchantment enchantment;
    private final String effectName;
    private final String displayName;
    public EnchantmentCommand(CrowdControlPlugin plugin, Enchantment enchantment) {
        super(plugin);
        this.enchantment = enchantment;
        final String translate = TextUtil.translate(enchantment);
        this.effectName = "enchant-" + translate.replace(' ', '_');
        this.displayName = "Apply " + TextUtil.asPlain(enchantment.displayName(enchantment.getMaxLevel()));
    }

    @Override
    public Response.Result execute(Request request) {
        int level = enchantment.getMaxLevel();
        Response.Result result = Response.Result.RETRY;
        for (Player player : CrowdControlPlugin.getPlayers()) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isEmpty())
                continue;
            if (item.getEnchantmentLevel(enchantment) != level) {
                item.addUnsafeEnchantment(enchantment, level);
                result = Response.Result.SUCCESS;
            }
        }
        return result;
    }
}
