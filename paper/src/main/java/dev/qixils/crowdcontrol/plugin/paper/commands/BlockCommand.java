package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class BlockCommand extends ImmediateCommand implements FeatureElementCommand {
	protected final Material material;
	private final String effectName;
	private final Component displayName;

	public BlockCommand(PaperCrowdControlPlugin plugin, Material block) {
		this(
				plugin,
				block,
				"block_" + block.key().value(),
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
	public @NotNull FeatureFlagSet requiredFeatures() {
		return CraftMagicNumbers.getBlock(material).requiredFeatures();
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
			Material mat = getMaterial();
			if (block.isReplaceable() && block.getType() != mat) {
				result.type(Response.ResultType.SUCCESS).message("SUCCESS");
				sync(() -> block.setType(mat));
			}
		}
		return result;
	}
}
