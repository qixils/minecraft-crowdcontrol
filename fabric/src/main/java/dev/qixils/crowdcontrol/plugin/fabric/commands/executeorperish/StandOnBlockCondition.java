package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public final class StandOnBlockCondition extends AbstractCondition {
	private final List<Block> blocks;
	private final Component component;

	public StandOnBlockCondition(int rewardLuck, String key, Block... blocks) {
		this(rewardLuck, key, null, blocks);
	}

	public StandOnBlockCondition(int rewardLuck, String key, @Nullable ConditionFlags flags, Block... blocks) {
		super(rewardLuck, flags);
		this.blocks = List.of(blocks);
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.stand." + key,
				blocks[0].getName().formatted(Formatting.GREEN)
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayerEntity player) {
		Location location = new Location(player);
		return blocks.contains(location.block().getBlock())
				|| blocks.contains(location.add(0, -1, 0).block().getBlock());
	}
}
