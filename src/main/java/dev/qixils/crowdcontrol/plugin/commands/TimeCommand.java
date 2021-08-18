package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class TimeCommand extends Command {
    private final String effectName = "zip";
    private final String displayName = "Zip Time";
    protected static final int ADD_TICKS = 400; // a minute in-game i think??
    public TimeCommand(CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    public Response.Result execute(Request request) {
        Bukkit.getWorlds().forEach(world -> world.setFullTime(world.getFullTime() + ADD_TICKS));
        return Response.Result.SUCCESS;
    }
}
