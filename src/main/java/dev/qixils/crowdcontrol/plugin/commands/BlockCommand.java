package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlockCommand extends Command {
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
    public Response.@NotNull Result execute(@NotNull Request request) {
        Response.Result result = Response.Result.RETRY;
        for (Player player : CrowdControlPlugin.getPlayers()) {
            Block block = player.getLocation().getBlock();
            if (block.getType().isEmpty()) {
                result = Response.Result.SUCCESS;
                Bukkit.getScheduler().runTask(plugin, () -> block.setType(material));
            }
        }
        return result;
    }
}
