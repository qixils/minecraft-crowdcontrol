package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
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

    public MoveCommand(BukkitCrowdControlPlugin plugin, Vector displacement, String effectName, String displayName) {
        super(plugin);
        vector = displacement;
        this.effectName = effectName;
        this.displayName = "Fling " + displayName;
    }

    public MoveCommand(BukkitCrowdControlPlugin plugin, double x, double y, double z, String effectName, String displayName) {
        this(plugin, new Vector(x, y, z), effectName, displayName);
    }

    public MoveCommand(BukkitCrowdControlPlugin plugin, Vector displacement, String effectName) {
        this(plugin, displacement, effectName, effectName);
    }

    public MoveCommand(BukkitCrowdControlPlugin plugin, double x, double y, double z, String effectName) {
        this(plugin, x, y, z, effectName, effectName);
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		// TODO test if this works with Disable Jumping
        Response.Builder resp = request.buildResponse().type(ResultType.RETRY).message("All players were grounded");
        boolean isDownwards = vector.getY() < 0.0;
        for (Player player : players) {
            // we are not worried about hackers or desync, just ensuring viewers get what they paid for
            //noinspection deprecation
            if (isDownwards && player.isOnGround())
                continue;
            resp.type(ResultType.SUCCESS).message("SUCCESS");
            Bukkit.getScheduler().runTask(plugin, () -> player.setVelocity(vector));
        }
        return resp;
    }
}
