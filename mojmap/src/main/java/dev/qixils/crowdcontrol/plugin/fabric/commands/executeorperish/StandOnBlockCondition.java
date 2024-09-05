package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.utils.Location;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
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
			ModdedCrowdControlPlugin.getInstance().toAdventure(blocks[0].getName().withStyle(ChatFormatting.GREEN))
		);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayer player) {
		Location location = new Location(player);
		return blocks.contains(location.block().getBlock())
				|| blocks.contains(location.add(0, -1, 0).block().getBlock());
	}
}
