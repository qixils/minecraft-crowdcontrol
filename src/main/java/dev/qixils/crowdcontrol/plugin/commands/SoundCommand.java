package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
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

    public SoundCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getCommand() {
        return "sound";
    }

    @Override
    public int getCooldownSeconds() {
        return 60;
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        Sound sound = (Sound) RandomUtil.randomElementFrom(SOUNDS);
        for (Player player : players) {
            Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
            player.getWorld().playSound(playAt, sound, SoundCategory.HOSTILE, 1.0f, 1.0f);
        }
        return true;
    }
}
