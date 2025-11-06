package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.packets.SetShaderS2C;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class ShaderCommand extends ModdedCommand implements CCTimedEffect {
	private final @NotNull String effectName;
	private final @NotNull String shader;
	private final @NotNull SemVer minimumModVersion;
	private final @NotNull String effectGroup = "shaders";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);

	public ShaderCommand(@NotNull ModdedCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = shader.getShaderId();
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players already have an active screen effect");

			// create byte buf
			Duration duration = Duration.ofMillis(request.getEffect().getDurationMillis());
			SetShaderS2C packet = new SetShaderS2C(shader, duration);

			// send packet
			for (ServerPlayer player : playerSupplier.get())
				plugin.sendToPlayer(player, packet);

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, duration.toMillis());
		}));
	}
}
