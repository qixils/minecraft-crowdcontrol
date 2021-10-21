package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
    protected final EntityType entityType;
    private final String effectName;
    private final String displayName;

    public SummonEntityCommand(CrowdControlPlugin plugin, EntityType entityType) {
        super(plugin);
        this.entityType = entityType;
        this.effectName = "entity_" + entityType.name();
        this.displayName = "Summon " + TextUtil.translate(entityType);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
        if (entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            for (World world : Bukkit.getWorlds()) {
                if (world.getDifficulty() == Difficulty.PEACEFUL)
                    return Response.builder().type(Response.ResultType.FAILURE).message("Hostile mobs cannot be spawned while on Peaceful difficulty");
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> spawnEntity(request.getViewer(), player)));
        return Response.builder().type(Response.ResultType.SUCCESS);
    }

    protected Entity spawnEntity(String viewer, Player player) {
        Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);
        entity.setCustomName(viewer);
        entity.setCustomNameVisible(true);
        if (entity instanceof Tameable tameable)
            tameable.setOwner(player);
        return entity;
    }
}
