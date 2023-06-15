package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final Component displayName;

	private static String effectNameOf(Difficulty difficulty) {
		return "difficulty_" + difficulty.name();
	}

	public DifficultyCommand(PaperCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = effectNameOf(difficulty);
		this.displayName = Component.translatable("cc.effect.difficulty.name", Component.translatable(difficulty));
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (difficulty.equals(getCurrentDifficulty()))
			return request.buildResponse().type(ResultType.FAILURE).message("Server difficulty is already on " + plugin.getTextUtil().asPlain(displayName));
		sync(() -> plugin.getServer().getWorlds().forEach(world -> world.setDifficulty(difficulty)));
		async(() -> {
			for (Difficulty dif : Difficulty.values())
				plugin.updateEffectStatus(plugin.getCrowdControl(), dif.equals(difficulty) ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE, effectNameOf(dif));
			for (EntityCommand command : plugin.commandRegister().getCommands(EntityCommand.class)) {
				TriState state = command.isSelectable();
				if (state != TriState.UNKNOWN)
					plugin.updateEffectStatus(plugin.getCrowdControl(), state == TriState.TRUE ? ResultType.SELECTABLE : ResultType.NOT_SELECTABLE, command);
			}
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Nullable
	private Difficulty getCurrentDifficulty() {
		Difficulty difficulty = null;
		for (World world : plugin.getServer().getWorlds()) {
			if (difficulty == null)
				difficulty = world.getDifficulty();
			else if (!difficulty.equals(world.getDifficulty()))
				return null;
		}
		return difficulty;
	}

	@Override
	public TriState isSelectable() {
		return difficulty.equals(getCurrentDifficulty()) ? TriState.FALSE : TriState.TRUE;
	}
}
