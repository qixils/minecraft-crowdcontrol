package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.ChatCommand;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DifficultyCommand extends ChatCommand {
    private final Difficulty difficulty;

    public DifficultyCommand(CrowdControlPlugin plugin, Difficulty difficulty) {
        super(plugin);
        this.difficulty = difficulty;
    }

    @Override
    public @NotNull String getCommand() {
        return difficulty.name();
    }

    @Override
    public int getCooldownSeconds() {
        return 0;
    }

    @Override
    public boolean execute(String authorName, List<Player> players, String... args) {
        for (World world : plugin.getServer().getWorlds()) {
            world.setDifficulty(difficulty);
        }
        return true;
    }
}
