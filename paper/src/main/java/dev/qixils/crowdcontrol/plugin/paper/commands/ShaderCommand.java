package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.command.impl.Shader;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.PaperUtil;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class ShaderCommand extends PaperCommand implements Listener, CCTimedEffect {
	private final @NotNull String effectName;
	private final @NotNull Key postEffect;
	private final @NotNull Shader shader;
	private final @NotNull String effectGroup = "shaders";
	private final @NotNull List<String> effectGroups = Collections.singletonList(effectGroup);

	private final Map<UUID, List<UUID>> uuidMap = new HashMap<>();

	public ShaderCommand(@NotNull PaperCrowdControlPlugin plugin, @NotNull Shader shader) {
		super(plugin);
		this.effectName = shader.getEffectId();
		this.shader = shader;
		this.postEffect = shader.getIdentifier();
	}

	/**
	 * Whether the given player's client knows about this post effect.
	 */
	private boolean isAvailableTo(@NotNull Player player) {
		return plugin.getShaderPackManager().canRender(player, shader);
	}

	/**
	 * Can't stack on ourselves or lidar
	 */
	@Override
	public String[] getEffectArray() {
		return new String[]{effectName, "lidar"};
	}

	@Override
	public TriState isVisible(@NotNull IUserRecord user, @NotNull List<Player> potentialPlayers) {
		if (!shader.bundled()) return TriState.UNKNOWN;
		return TriState.fromBoolean(potentialPlayers.stream().anyMatch(this::isAvailableTo));
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = new ArrayList<>();
			for (Player player : playerSupplier.get()) {
				if (!isAvailableTo(player)) continue;
				if (player.postEffects().values().contains(postEffect)) continue;
				players.add(player);
			}
			if (players.isEmpty())
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Target already has shader or cannot receive it");
			sync(() -> players.forEach(player -> player.postEffects().add(postEffect)));
			uuidMap.put(request.getRequestId(), players.stream().map(Player::getUniqueId).toList());
			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDurationMillis());
		}));
	}

	@Override
	public void onPause(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		forEachTarget(uuidMap.get(request.getRequestId()), player -> player.postEffects().remove(postEffect));
	}

	@Override
	public void onResume(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		forEachTarget(uuidMap.get(request.getRequestId()), player -> player.postEffects().add(postEffect));
	}

	@Override
	public void onEnd(@NotNull PublicEffectPayload request, @NotNull CCPlayer source) {
		forEachTarget(uuidMap.remove(request.getRequestId()), player -> player.postEffects().remove(postEffect));
	}

	private void forEachTarget(@Nullable List<UUID> uuids, @NotNull Consumer<Player> action) {
		List<Player> players = PaperUtil.toPlayers(uuids);
		if (players.isEmpty()) return;
		sync(() -> players.forEach(action));
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		event.getPlayer().postEffects().clear();
	}
}
