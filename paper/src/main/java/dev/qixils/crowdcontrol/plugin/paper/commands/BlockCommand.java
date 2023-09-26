package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil.cbClass;

@Getter
public class BlockCommand extends ImmediateCommand implements FeatureElementCommand {
	protected final Material material;
	private final String effectName;
	private final Component displayName;

	public BlockCommand(PaperCrowdControlPlugin plugin, Material block) {
		this(
				plugin,
				block,
				"block_" + block.name(),
				Component.translatable("cc.effect.block.name", Component.translatable(block))
		);
	}

	protected BlockCommand(PaperCrowdControlPlugin plugin, Material block, String effectName, Component displayName) {
		super(plugin);
		this.material = block;
		this.effectName = effectName;
		this.displayName = displayName;
	}

	@Override
	public @NotNull Optional<Object> requiredFeatures() {
		//return CraftMagicNumbers.getBlock(material).requiredFeatures();
		return ReflectionUtil.getClazz(cbClass("util.CraftMagicNumbers")).flatMap(clazz -> ReflectionUtil.invokeMethod(
				(Object) null,
				clazz,
				"getBlock",
				new Class<?>[]{Material.class},
				material
		));
	}

	@Nullable
	protected Location getLocation(Player player) {
		Location location = player.getLocation();
		if (!location.getBlock().isReplaceable())
			return null;
		return location;
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(Response.ResultType.RETRY)
				.message("No available locations to set blocks");
		for (Player player : players) {
			Location location = getLocation(player);
			if (location == null)
				continue;
			Block block = location.getBlock();
			if (block.isReplaceable() && block.getType() != material) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> block.setType(material));
			}
		}
		return result;
	}
}
