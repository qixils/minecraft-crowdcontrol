package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.ParticleUtil;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class TeleportCommand extends Command {
    private final String effectName = "chorus-fruit";
    private final String displayName = "Eat Chorus Fruit";

    public TeleportCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Location destination = RandomUtil.randomNearbyBlock(player.getLocation(), 3, 15, true, BlockUtil.AIR_ARRAY);
            if (destination == null) {
                continue;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(destination);
                ParticleUtil.spawnPlayerParticles(player, Particle.PORTAL, 100);
                player.getWorld().playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1.0f, 1.0f);
            });
        }
        return Response.Result.SUCCESS;
    }
}
