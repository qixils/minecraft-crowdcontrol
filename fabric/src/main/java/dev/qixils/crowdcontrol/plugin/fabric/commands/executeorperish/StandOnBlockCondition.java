package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

import java.util.List;

@Getter
public final class StandOnBlockCondition implements SuccessCondition {
	private final int rewardLuck;
	private final List<Block> blocks;
	private final Component component;

	public StandOnBlockCondition(int rewardLuck, String key, Block... blocks) {
		this.rewardLuck = rewardLuck;
		this.blocks = List.of(blocks);
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.stand." + key,
				blocks[0].getName().withStyle(ChatFormatting.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(ServerPlayer player) {
		Location location = new Location(player);
		return blocks.contains(location.block().getBlock())
				|| blocks.contains(location.add(0, -1, 0).block().getBlock());
	}
}
