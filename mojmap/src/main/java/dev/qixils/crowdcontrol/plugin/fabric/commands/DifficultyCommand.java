package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Global
public class DifficultyCommand extends ModdedCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final Component displayName;

	private static String effectNameOf(Difficulty difficulty) {
		return "difficulty_" + difficulty.getKey();
	}

	public DifficultyCommand(ModdedCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = effectNameOf(difficulty);
		this.displayName = Component.translatable("cc.effect.difficulty.name", plugin.toAdventure(difficulty.getDisplayName()));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			playerSupplier.get(); // validate now is ok to start
			if (plugin.server().getWorldData().isDifficultyLocked())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_PERMANENT, "Server difficulty is locked");
			if (plugin.server().getWorldData().getDifficulty() == difficulty)
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Server difficulty is already set to " + displayName);

			async(plugin::updateConditionalEffectVisibility);
			sync(() -> plugin.server().setDifficulty(difficulty, true));

			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (plugin.server().getWorldData().isDifficultyLocked())
			return TriState.FALSE;
		return TriState.TRUE;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<ServerPlayer> potentialPlayers) {
		if (plugin.server().getWorldData().getDifficulty() == difficulty)
			return TriState.FALSE;
		return TriState.TRUE;
	}
}
