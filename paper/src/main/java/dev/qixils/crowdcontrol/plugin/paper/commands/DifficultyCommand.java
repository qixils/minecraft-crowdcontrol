package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.websocket.data.CCEffectReport;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ReportStatus;
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

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Global
public class DifficultyCommand extends Command {
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
		ThreadUtil.waitForSuccess(() -> {
			if (difficulty.equals(getCurrentDifficulty()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Server difficulty is already on " + plugin.getTextUtil().asPlain(displayName));
			sync(() -> Bukkit.getServer().getWorlds().forEach(world -> world.setDifficulty(difficulty)));

			// effect report update
			CrowdControl cc = plugin.getCrowdControl();
			if (cc == null) {
				log.warn("getCrowdControl undefined!?");
			} else {
				Map<ReportStatus, List<String>> reports = new HashMap<>();
				reports.put(ReportStatus.MENU_AVAILABLE, Arrays.stream(Difficulty.values()).filter(dif -> dif != difficulty).map(dif -> effectNameOf(difficulty)).collect(Collectors.toList()));
				reports.put(ReportStatus.MENU_UNAVAILABLE, Collections.singletonList(effectName));
				// TODO: this is a cute idea but it would make more sense to just store the state of every sent report for every player and just update from that
				//  probably add some smart logic for this to cc4j
				for (EntityCommand command : plugin.commandRegister().getCommands(EntityCommand.class)) {
					TriState state = command.isSelectable();
					if (state != TriState.UNKNOWN)
						reports.get(state == TriState.TRUE ? ReportStatus.MENU_AVAILABLE : ReportStatus.MENU_UNAVAILABLE).add(command.getEffectName().toLowerCase(Locale.US));
				}
				CCEffectReport[] reportList = reports.entrySet().stream().map((entry) -> new CCEffectReport(entry.getKey(), entry.getValue())).toArray(CCEffectReport[]::new);
				cc.getPlayers().forEach(ccp -> ccp.sendReport(reportList));
			}

			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		});
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
	public TriState isSelectable() {
		return difficulty.equals(getCurrentDifficulty()) ? TriState.FALSE : TriState.TRUE;
	}
}
