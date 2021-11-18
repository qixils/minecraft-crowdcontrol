package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class MoveCommand extends ImmediateCommand {
    protected final Vector vector;
    protected final String effectName;
    protected final String displayName;
    public MoveCommand(CrowdControlPlugin plugin, Vector displacement, String effectName, String displayName) {
        super(plugin);
        vector = displacement;
        this.effectName = effectName;
        this.displayName = "Move " + displayName;
    }

    public MoveCommand(CrowdControlPlugin plugin, int x, int y, int z, String effectName, String displayName) {
        this(plugin, new Vector(x, y, z), effectName, displayName);
    }

    public MoveCommand(CrowdControlPlugin plugin, Vector displacement, String effectName) {
        this(plugin, displacement, effectName, effectName);
    }

    public MoveCommand(CrowdControlPlugin plugin, int x, int y, int z, String effectName) {
        this(plugin, x, y, z, effectName, effectName);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Bukkit.getScheduler().runTask(plugin, () -> players.forEach(player -> player.setVelocity(vector)));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
