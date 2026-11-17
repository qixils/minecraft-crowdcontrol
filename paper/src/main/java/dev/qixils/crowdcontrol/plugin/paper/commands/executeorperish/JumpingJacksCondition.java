package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class JumpingJacksCondition extends AbstractListeningCondition<Integer> {
	private final int goal;
	private final @NotNull Component component;

	public JumpingJacksCondition(int goal) {
		super(2, 0, null);
		this.goal = goal;
		this.component = Component.translatable(
				"cc.effect.do_or_die.condition.jump",
				Component.text(goal, NamedTextColor.GREEN)
		);
	}

	@EventHandler
	public void onJump(@NotNull PlayerJumpEvent event) {
		computeStatus(event.getPlayer(), i -> i + 1);
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		return getStatus(player) >= goal;
	}

	@Override
	public @NotNull Component getComponent(UUID player) {
		return component;
	}
}
