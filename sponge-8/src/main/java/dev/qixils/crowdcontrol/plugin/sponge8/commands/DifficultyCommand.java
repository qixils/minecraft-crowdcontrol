package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.List;

@Getter
@Global
public class DifficultyCommand extends ImmediateCommand {
	private final Difficulty difficulty;
	private final String effectName;
	private final Component displayName;

	private static String effectNameOf(Difficulty difficulty) {
		return "difficulty_" + difficulty.key(RegistryTypes.DIFFICULTY).value();
	}

	public DifficultyCommand(SpongeCrowdControlPlugin plugin, Difficulty difficulty) {
		super(plugin);
		this.difficulty = difficulty;
		this.effectName = effectNameOf(difficulty);
		this.displayName = Component.translatable("cc.effect.difficulty.name", difficulty);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (difficulty.equals(getCurrentDifficulty()))
			return request.buildResponse().type(ResultType.FAILURE).message("Server difficulty is already on " + plugin.getTextUtil().asPlain(displayName));
		sync(() -> plugin.getGame().server().worldManager().worlds().forEach(world -> world.properties().setDifficulty(difficulty)));
		async(() -> {
			for (Difficulty dif : plugin.registryIterable(RegistryTypes.DIFFICULTY))
				plugin.updateEffectStatus(plugin.getCrowdControl(), effectNameOf(dif), dif.equals(difficulty) ? ResultType.NOT_SELECTABLE : ResultType.SELECTABLE);
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Nullable
	private Difficulty getCurrentDifficulty() {
		Difficulty difficulty = null;
		for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
			if (difficulty == null)
				difficulty = world.difficulty();
			else if (!difficulty.equals(world.difficulty()))
				return null;
		}
		return difficulty;
	}

	@Override
	public TriState isSelectable() {
		return difficulty.equals(getCurrentDifficulty()) ? TriState.FALSE : TriState.TRUE;
	}
}
