package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Collection;

public class PotionCommand extends ChatCommand {
    private static LocalDateTime globalUsage = null;
    private final PotionEffectType potionEffectType;

    public PotionCommand(CrowdControl plugin, PotionEffectType potionEffectType) {
        super(plugin);
        this.potionEffectType = potionEffectType;
    }

    @Override
    public int getCooldownSeconds() {
        return 60*15;
    }

    @Override
    public void setCooldown() {
        super.setCooldown();
        globalUsage = LocalDateTime.now();
    }

    @Override
    public boolean canUse() {
        return super.canUse() && (globalUsage == null || globalUsage.plusMinutes(1).isBefore(LocalDateTime.now()));
    }

    @Override
    public @NotNull String getCommand() {
        return potionEffectType.getName();
    }

    @Override
    public boolean execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        PotionEffect potionEffect = potionEffectType.createEffect(20*15, rand.nextInt(2));
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Player player : players) {
                    player.addPotionEffect(potionEffect);
                }
            }
        }.runTask(plugin);
        return true;
    }
}
