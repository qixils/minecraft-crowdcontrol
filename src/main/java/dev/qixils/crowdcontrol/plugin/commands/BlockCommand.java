package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class BlockCommand extends ImmediateCommand {
    protected final Material material;
    private final String effectName;
    private final String displayName;

    public BlockCommand(CrowdControlPlugin plugin, Material block) {
        super(plugin);
        this.material = block;
        this.effectName = "block_" + block.name();
        this.displayName = "Place " + TextUtil.translate(block) + " Block";
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        Response.Builder result = request.buildResponse().type(Response.ResultType.RETRY).message("No available locations to set blocks");
        for (Player player : players) {
            Block block = player.getLocation().getBlock();
            if (block.isReplaceable()) {
                result.type(Response.ResultType.SUCCESS).message("SUCCESS");
                Bukkit.getScheduler().runTask(plugin, () -> block.setType(material));
            }
        }
        return result;
    }
}
