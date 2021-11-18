package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class EntityChaosCommand extends ImmediateCommand {
    private final String displayName = "Entity Chaos";
    private final String effectName = "entity_chaos";

    public EntityChaosCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            List<Entity> entities = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getType() == EntityType.PLAYER) continue;
                    entities.add(entity);
                }
            }
            for (int i = 0; i < entities.size(); i++) {
                entities.get(i).teleport(players.get(i % players.size()));
            }
        });
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
