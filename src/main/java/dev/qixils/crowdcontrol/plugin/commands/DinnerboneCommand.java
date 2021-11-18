package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DinnerboneCommand extends ImmediateCommand {
    private static final String NAME = "Dinnerbone";
    private static final int RADIUS = 15;

    public DinnerboneCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Getter
    private final String effectName = "dinnerbone";
    @Getter
    private final String displayName = "Dinnerbone";

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Set<LivingEntity> entities = new HashSet<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : players) {
                entities.addAll(player.getLocation().getNearbyLivingEntities(RADIUS, x -> x.getType() != EntityType.PLAYER && (x.getCustomName() == null || x.getCustomName().isEmpty() || x.getCustomName().equals(NAME))));
            }
            entities.forEach(x -> x.setCustomName(Objects.equals(x.getCustomName(), NAME) ? null : NAME));
        });
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
