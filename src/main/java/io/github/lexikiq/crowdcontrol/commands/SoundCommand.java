package io.github.lexikiq.crowdcontrol.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import io.github.lexikiq.crowdcontrol.ChatCommand;
import io.github.lexikiq.crowdcontrol.CrowdControl;
import io.github.lexikiq.crowdcontrol.utils.RandomUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SoundCommand extends ChatCommand {
    public static final List<Sound> SOUNDS = List.of(
            Sound.ENTITY_CREEPER_PRIMED,
            Sound.ENTITY_ENDERMAN_STARE,
            Sound.ENTITY_ENDERMAN_SCREAM,
            Sound.ENTITY_ENDER_DRAGON_GROWL,
            Sound.ENTITY_GHAST_HURT,
            Sound.ENTITY_GENERIC_EXPLODE,
            Sound.AMBIENT_CAVE
    );

    public SoundCommand(CrowdControl plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getCommand() {
        return "sound";
    }

    @Override
    public int getCooldownSeconds() {
        return 60*5;
    }

    @Override
    public boolean execute(ChannelMessageEvent event, List<Player> players, String... args) {
        Sound sound = (Sound) RandomUtil.randomElementFrom(SOUNDS);
        for (Player player : players) {
            Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
            player.getWorld().playSound(playAt, sound, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
        return true;
    }
}
