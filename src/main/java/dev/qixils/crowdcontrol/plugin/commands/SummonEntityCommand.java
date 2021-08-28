package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.BlockUtil;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class SummonEntityCommand extends Command {
    protected final EntityType entityType;
    protected static final int SPAWN_RADIUS = 7;
    private final String effectName;
    private final String displayName;

    public SummonEntityCommand(CrowdControlPlugin plugin, EntityType entityType) {
        super(plugin);
        this.entityType = entityType;
        this.effectName = "entity-" + entityType.name();
        this.displayName = "Summon " + TextUtil.translate(entityType);
    }

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Location loc = getSpawnLocation(player.getLocation());
            if (loc != null)
                Bukkit.getScheduler().runTask(plugin, () -> spawnEntity(player, loc));
        }
        return Response.Result.SUCCESS;
    }

    protected Entity spawnEntity(Player player, Location location) {
        return player.getWorld().spawnEntity(location, entityType);
    }

    protected Location getSpawnLocation(Location location) {
        return BlockUtil.blockFinderBuilder()
                .origin(location)
                .locationValidator(BlockUtil.SPAWNING_SPACE)
                .minRadius(3)
                .maxRadius(SPAWN_RADIUS)
                .build().next();
    }
}
