package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.ClassCooldowns;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PotionCommand extends ChatCommand {
    private final PotionEffectType potionEffectType;
    private final int duration;
    private static final int MAX_DURATION = 20*15;

    public PotionCommand(CrowdControl plugin, PotionEffectType potionEffectType) {
        super(plugin);
        this.potionEffectType = potionEffectType;
        boolean isMinimal = potionEffectType.isInstant();
        duration = isMinimal ? 1 : MAX_DURATION;
    }

    @Override
    public int getCooldownSeconds() {
        return (int) (60*7.5);
    }

    @Override
    public ClassCooldowns getClassCooldown() {
        return ClassCooldowns.POTION;
    }

    @Override
    public @NotNull String getCommand() {
        return potionEffectType.getName();
    }

    @Override
    public boolean execute(ChannelMessageEvent event, Collection<? extends Player> players) {
        PotionEffect potionEffect = potionEffectType.createEffect(duration, rand.nextInt(2));
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
