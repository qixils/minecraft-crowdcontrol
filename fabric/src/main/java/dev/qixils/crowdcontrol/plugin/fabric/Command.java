package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public abstract class Command implements dev.qixils.crowdcontrol.common.command.Command<ServerPlayer> {
	protected static final Random random = RandomUtil.RNG;
	@Getter
	protected final FabricCrowdControlPlugin plugin;

	protected Command(@NotNull FabricCrowdControlPlugin plugin) {
		this.plugin = Objects.requireNonNull(plugin, "plugin");
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		return plugin.isClientAvailable(possiblePlayers, request);
	}

	protected static String csIdOf(ResourceLocation id) {
		if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
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

	@SuppressWarnings("ConstantValue") // overworld can indeed be null early in initialization
	@Override
	public TriState isVisible() {
		if (this instanceof FeatureElement element)
			return TriState.fromBoolean(plugin.isEnabled(element));
		return TriState.UNKNOWN; // avoid sending any packet
	}
}
