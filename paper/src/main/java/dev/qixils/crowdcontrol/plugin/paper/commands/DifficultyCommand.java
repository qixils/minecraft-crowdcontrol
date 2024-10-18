package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

@Getter
@Global
public class DifficultyCommand extends PaperCommand {
	private static final Logger log = LoggerFactory.getLogger(DifficultyCommand.class);
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
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (difficulty.equals(getCurrentDifficulty()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Server difficulty is already on " + plugin.getTextUtil().asPlain(displayName));
			sync(() -> Bukkit.getServer().getWorlds().forEach(world -> world.setDifficulty(difficulty)));

			plugin.optionalCrowdControl().ifPresent(cc -> cc.getPlayers().forEach(plugin::updateConditionalEffectVisibility));

			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}

	@Nullable
	private Difficulty getCurrentDifficulty() {
		Difficulty difficulty = null;
		for (World world : Bukkit.getServer().getWorlds()) {
			if (difficulty == null)
				difficulty = world.getDifficulty();
			else if (!difficulty.equals(world.getDifficulty()))
				return null;
		}
		return difficulty;
	}

	@Override
	public TriState isSelectable(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		return difficulty.equals(getCurrentDifficulty()) ? TriState.FALSE : TriState.TRUE;
	}
}
