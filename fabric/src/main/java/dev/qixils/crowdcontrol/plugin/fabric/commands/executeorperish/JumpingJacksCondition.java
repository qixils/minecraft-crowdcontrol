package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.plugin.fabric.event.Jump;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class JumpingJacksCondition extends AbstractListeningCondition<Integer> {
	private final int goal;
	@Getter
	private final @NotNull Component component;

	public JumpingJacksCondition(int goal) {
		super(1, 0, null);
		this.goal = goal;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.jump",
				Component.text(goal, NamedTextColor.GREEN)
		);
	}

	@Listener
	public void onJump(@NotNull Jump event) {
		if (event.player() instanceof ServerPlayer player)
			computeStatus(player, i -> i + 1);
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayer player) {
		return getStatus(player) >= goal;
	}
}
