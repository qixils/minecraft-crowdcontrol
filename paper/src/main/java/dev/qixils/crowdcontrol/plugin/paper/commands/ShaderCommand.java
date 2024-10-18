package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.packets.ShaderPacketS2C;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil.toPlayers;

@Getter
public class ShaderCommand extends PaperCommand implements CCTimedEffect {
	private static final @NotNull Set<UUID> ACTIVE_SHADERS = new HashSet<>();
	private final @NotNull String effectName;
	private final @NotNull String shader;
	private final @NotNull SemVer minimumModVersion;
	private final @NotNull Duration defaultDuration = Duration.ofSeconds(30);
	private final @NotNull Map<UUID, List<UUID>> idMap = new HashMap<>();

	public ShaderCommand(@NotNull PaperCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.minimumModVersion = shader.addedIn();
		this.shader = shader.getShaderId();
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			if (isActive(ccPlayer, getEffectArray()))
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Conflicting effects active");

			List<Player> players = playerSupplier.get();

			players.removeIf(player -> ACTIVE_SHADERS.contains(player.getUniqueId()));
			if (players.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "All players already have an active screen effect");

			idMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());

			// create byte buf
			Duration duration = Duration.ofSeconds(request.getEffect().getDuration());
			ShaderPacketS2C packet = new ShaderPacketS2C(shader, duration);

			// send packet
			for (Player player : players) {
				ACTIVE_SHADERS.add(player.getUniqueId());
				plugin.getPluginChannel().sendMessage(player, packet);
			}

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, duration.toMillis());
		}));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		toPlayers(idMap.remove(request.getRequestId())).forEach(player -> ACTIVE_SHADERS.remove(player.getUniqueId()));
	}
}
