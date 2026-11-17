package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import dev.qixils.crowdcontrol.common.command.impl.executeorperish.MathConditionGenerator;
import dev.qixils.crowdcontrol.common.command.impl.executeorperish.MathConditionState;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class MathCondition extends AbstractListeningCondition<MathConditionState> {
	public MathCondition() {
		super(2, new MathConditionState(), null);
	}

	@Override
	public void track(@NotNull UUID player) {
		MathConditionGenerator generator = RandomUtil.randomElementFrom(MathConditionGenerator.values());
		MathConditionState result = generator.generate();
		result.setComponent(Component.translatable(
			"cc.effect.do_or_die.condition.math",
			result.getComponent().color(NamedTextColor.GREEN)
		));

		statuses.put(player, result);
	}

	@EventHandler
	public void onChat(@NotNull AsyncChatEvent event) {
		if (!hasStatus(event.getPlayer())) return;

		String message = event.signedMessage().message().trim();
		if (!message.equals(Integer.toString(getStatus(event.getPlayer()).getGoal()))) return;

		computeStatus(event.getPlayer(), state -> new MathConditionState(state.getGoal(), state.getComponent(), true));
	}

	@Override
	public boolean hasSucceeded(@NotNull Player player) {
		return hasStatus(player) && getStatus(player).isSucceeded();
	}

	@Override
	public @NotNull Component getComponent(UUID player) {
		return Objects.requireNonNullElseGet(getStatus(player).getComponent(), Component::empty);
	}
}
