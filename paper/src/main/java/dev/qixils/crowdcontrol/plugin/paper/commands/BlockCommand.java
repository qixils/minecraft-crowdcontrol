package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.FeatureElementCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class BlockCommand extends RegionalCommandSync implements FeatureElementCommand {
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
	public boolean isFeatureEnabled(@NotNull World world) {
		return material.asBlockType() != null && world.isEnabled(material.asBlockType());
	}

	@Nullable
	protected Location getLocation(Player player) {
		Location location = player.getLocation();
		if (!location.getBlock().isReplaceable())
			return null;
		return location;
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Location location = getLocation(player);
		if (location == null)
			return false;

		Block block = location.getBlock();
		Material mat = getMaterial();
		if (!block.isReplaceable() || block.getType() == mat)
			return false;

		block.setType(mat);
		return true;
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No available locations to set blocks");
	}
}
