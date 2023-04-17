package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<ServerPlayerEntity> {
	protected static final @NotNull Random random = RandomUtil.RNG;
	@Getter
	protected final @NotNull FabricCrowdControlPlugin plugin;

	protected Command(@NotNull FabricCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayerEntity> possiblePlayers, @NotNull Request request) {
		return plugin.isClientAvailable(possiblePlayers, request);
	}

	protected static String csIdOf(Identifier id) {
		if (!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
			return id.getPath();
		String path = id.getPath();
		return switch (path) {
			case "lightning_bolt" -> "lightning";
			case "chest_minecart" -> "minecart_chest";
			case "mooshroom" -> "mushroom_cow";
			case "tnt" -> "primed_tnt";
			case "snow_golem" -> "snowman";
			case "binding_curse" -> "curse_of_binding";
			case "vanishing_curse" -> "curse_of_vanishing";
			case "sweeping" -> "sweeping_edge";
			default -> path;
		};
	}
}
