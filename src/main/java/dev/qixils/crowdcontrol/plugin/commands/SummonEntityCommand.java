package dev.qixils.crowdcontrol.plugin.commands;

import com.destroystokyo.paper.loottable.LootableInventory;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
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
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
    private static final Set<LootTables> CHEST_LOOT_TABLES;
    static {
        EnumSet<LootTables> lootTables = EnumSet.noneOf(LootTables.class);
        for (LootTables lootTable : LootTables.values()) {
            String key = lootTable.getKey().getKey();
            if (key.startsWith("chests/"))
                lootTables.add(lootTable);
        }
        CHEST_LOOT_TABLES = Collections.unmodifiableSet(lootTables);
    }

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
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        if (entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
            for (World world : Bukkit.getWorlds()) {
                if (world.getDifficulty() == Difficulty.PEACEFUL)
                    return request.buildResponse().type(Response.ResultType.FAILURE).message("Hostile mobs cannot be spawned while on Peaceful difficulty");
            }
        }
        Bukkit.getScheduler().runTask(plugin, () -> players.forEach(player -> spawnEntity(request.getViewer(), player)));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }

    protected Entity spawnEntity(String viewer, Player player) {
        Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);
        entity.setCustomName(viewer);
        entity.setCustomNameVisible(true);
        if (entity instanceof Tameable tameable)
            tameable.setOwner(player);
        if (entity instanceof LootableInventory lootable)
            lootable.setLootTable(RandomUtil.randomElementFrom(CHEST_LOOT_TABLES).getLootTable());
        return entity;
    }
}
