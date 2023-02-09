package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public final class JumpingJacksCondition extends AbstractListeningCondition<Integer> {
	private final int goal;
	@Getter
	private final @NotNull Component component;

	public JumpingJacksCondition(int goal) {
		super(0, 0, null);
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
}
