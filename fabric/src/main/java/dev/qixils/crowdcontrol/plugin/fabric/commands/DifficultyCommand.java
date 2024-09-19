package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final Component displayName;

	private static String effectNameOf(Difficulty difficulty) {
		return "difficulty_" + difficulty.getKey();
	}

	public DifficultyCommand(FabricCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = effectNameOf(difficulty);
		this.displayName = Component.translatable("cc.effect.difficulty.name", plugin.toAdventure(difficulty.getDisplayName()));
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder response = request.buildResponse().type(ResultType.SUCCESS);

		if (plugin.server().getWorldData().isDifficultyLocked())
			return response.type(ResultType.UNAVAILABLE).message("Server difficulty is locked");
		if (plugin.server().getWorldData().getDifficulty() == difficulty)
			return response.type(ResultType.FAILURE).message("Server difficulty is already set to " + displayName);

		async(() -> {
			for (Difficulty dif : Difficulty.values())
				plugin.updateEffectStatus(plugin.getCrowdControl(), dif.equals(difficulty) ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE, effectNameOf(dif));
			for (Command<ServerPlayer> command : plugin.commandRegister().getCommands()) {
				if (!(command instanceof EntityCommand<?>))
					continue;
				TriState state = command.isSelectable();
				if (state != TriState.UNKNOWN)
					plugin.updateEffectStatus(plugin.getCrowdControl(), state == TriState.TRUE ? ResultType.SELECTABLE : ResultType.NOT_SELECTABLE, command);
			}
		});

		sync(() -> plugin.server().setDifficulty(difficulty, true));
		return response;
	}

	@Override
	public TriState isSelectable() {
		if (plugin.server().getWorldData().isDifficultyLocked())
			return TriState.FALSE;
		if (plugin.server().getWorldData().getDifficulty() == difficulty)
			return TriState.FALSE;
		return TriState.TRUE;
	}
}
