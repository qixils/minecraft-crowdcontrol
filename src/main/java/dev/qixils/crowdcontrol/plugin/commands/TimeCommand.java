package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class TimeCommand extends ImmediateCommand {
    private final String effectName = "zip";
    private final String displayName = "Zip Time";
    protected static final int ADD_TICKS = 6000; // roughly 15 minutes in-game
    public TimeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getWorlds().forEach(world -> world.setFullTime(world.getFullTime() + ADD_TICKS)));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }
}
