package io.github.lexikiq.crowdcontrol.commands;

import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControlPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnchantmentCommand extends ChatCommand {
    protected final Enchantment enchantment;
    public EnchantmentCommand(CrowdControlPlugin plugin, Enchantment enchantment) {
        super(plugin);
        this.enchantment = enchantment;
    }

    @Override
    public int getCooldownSeconds() {
        return 60*10;
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.ENCHANTMENT;
    }

    @Override
    public @NotNull String getCommand() {
        // Enchantment technically isn't an enum so i can't do a switch/case :(
        if (Enchantment.BINDING_CURSE.equals(enchantment)) {
            return "bind";
        } else if (Enchantment.VANISHING_CURSE.equals(enchantment)) {
            return "vanish";
        }
        return enchantment.getKey().getKey();
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        // input parsing
        int level;
        if (args.length < 1) {
            level = enchantment.getStartLevel();
        } else {
            try {
                level = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return false;
            }
            if (level < 1) {
                return false;
            }
        }
        level = Math.min(level, 32767);

        int finalLevel = level;
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Player player : players) {
                    player.getInventory().getItemInMainHand().addUnsafeEnchantment(enchantment, finalLevel);
                    player.updateInventory();
                }
            }
        }.runTask(plugin);
        return true;
    }
}
