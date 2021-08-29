package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.utils.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Getter
public class DifficultyCommand extends Command {
    private final Difficulty difficulty;
    private final String effectName;
    private final String displayName;

    public DifficultyCommand(CrowdControlPlugin plugin, Difficulty difficulty) {
        super(plugin);
        this.difficulty = difficulty;
        this.effectName = "difficulty_" + difficulty.name();
        this.displayName = "Set Difficulty: " + TextUtil.translate(difficulty);
    }

    @Override
    public Response.@NotNull Result execute(@NotNull Request request) {
        for (World world : plugin.getServer().getWorlds()) {
            world.setDifficulty(difficulty);
        }
        return Response.Result.SUCCESS;
    }
}
