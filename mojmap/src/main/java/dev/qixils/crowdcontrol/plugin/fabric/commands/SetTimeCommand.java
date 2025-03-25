package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.NIGHT;

@Getter
@Global
public class SetTimeCommand extends ModdedCommand {
	private final @NotNull String effectName;
	private final long time;

	public SetTimeCommand(ModdedCrowdControlPlugin plugin, @NotNull String effectName, long time) {
		super(plugin);
		this.effectName = effectName;
		this.time = time;
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			for (ServerLevel level : plugin.server().getAllLevels()) {
				final long ogTime = level.getDayTime();
				final long setTime = (ogTime - (ogTime % 24000)) + time;
				sync(() -> level.setDayTime(setTime));
			}
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}));
	}

	@NotNull
	public static SetTimeCommand day(ModdedCrowdControlPlugin plugin) {
		return new SetTimeCommand(plugin, "time_day", DAY);
	}

	@NotNull
	public static SetTimeCommand night(ModdedCrowdControlPlugin plugin) {
		return new SetTimeCommand(plugin, "time_night", NIGHT);
	}
}
