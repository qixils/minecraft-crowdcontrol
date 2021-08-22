package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DinnerboneCommand extends Command {
    private static final String NAME = "Dinnerbone";
    private static final int RADIUS = 15;

    public DinnerboneCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "dinnerbone";

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        Set<LivingEntity> entities = new HashSet<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : CrowdControlPlugin.getPlayers()) {
                entities.addAll(player.getLocation().getNearbyLivingEntities(RADIUS, x -> x.getType() != EntityType.PLAYER && (x.getCustomName() == null || x.getCustomName().isEmpty() || x.getCustomName().equals(NAME))));
            }
            entities.forEach(x -> x.setCustomName(Objects.equals(x.getCustomName(), NAME) ? null : NAME));
        });
        return Response.Result.SUCCESS;
    }
}
