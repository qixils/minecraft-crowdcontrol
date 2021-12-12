package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
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
import java.util.concurrent.CompletableFuture;

public class DinnerboneCommand extends Command {
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
    protected @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Set<LivingEntity> entities = new HashSet<>();
        CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player player : players) {
                entities.addAll(player.getLocation().getNearbyLivingEntities(RADIUS,
                        x -> x.getType() != EntityType.PLAYER && (x.getCustomName() == null || x.getCustomName().isEmpty() || x.getCustomName().equals(NAME) || SummonEntityCommand.isMobViewerSpawned(plugin, x))
                ));
            }
            successFuture.complete(!entities.isEmpty());
            entities.forEach(x -> {
                // TODO: save/restore old name
                x.setCustomNameVisible(false);
                x.setCustomName(Objects.equals(x.getCustomName(), NAME) ? null : NAME);
            });
        });
        return successFuture.thenApply(success -> success
                ? request.buildResponse().type(ResultType.SUCCESS)
                : request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
    }
}
