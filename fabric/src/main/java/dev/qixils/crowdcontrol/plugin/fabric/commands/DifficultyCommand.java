package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
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
		this.displayName = Component.translatable("cc.effect.difficulty.name", difficulty.getDisplayName());
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Builder response = request.buildResponse()
				.type(ResultType.FAILURE)
				.message("Server difficulty is locked or already set to " + displayName);

		if (plugin.server().getWorldData().isDifficultyLocked())
			return response;
		if (plugin.server().getWorldData().getDifficulty() == difficulty)
			return response;

		async(() -> {
			for (Difficulty dif : Difficulty.values())
				plugin.updateEffectStatus(plugin.getCrowdControl(), effectNameOf(dif), dif.equals(difficulty) ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE);
		});

		sync(() -> plugin.server().setDifficulty(difficulty, true));
		return response.type(ResultType.SUCCESS).message("SUCCESS");
	}
}
