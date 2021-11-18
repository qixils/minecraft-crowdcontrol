package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class SoundCommand extends ImmediateCommand {
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

    private final String effectName = "sfx";
    private final String displayName = "Spooky Sound Effect";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        Sound sound = RandomUtil.randomElementFrom(SOUNDS);
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Location playAt = player.getLocation().add(player.getFacing().getOppositeFace().getDirection());
            player.getWorld().playSound(playAt, sound, SoundCategory.MASTER, 2.0f, 1.0f);
        }
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
