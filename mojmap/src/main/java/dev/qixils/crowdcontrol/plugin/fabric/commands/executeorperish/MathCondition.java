package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.command.impl.executeorperish.MathConditionGenerator;
import dev.qixils.crowdcontrol.common.command.impl.executeorperish.MathConditionState;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.event.Chat;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.level.ServerPlayer;
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

	@Listener
	public void onChat(@NotNull Chat event) {
		if (!hasStatus(event.player())) return;

		String message = event.message().trim();
		if (!message.equals(Integer.toString(getStatus(event.player()).getGoal()))) return;

		computeStatus(event.player(), state -> new MathConditionState(state.getGoal(), state.getComponent(), true));
	}

	@Override
	public boolean hasSucceeded(@NotNull ServerPlayer player) {
		return hasStatus(player) && getStatus(player).isSucceeded();
	}

	@Override
	public @NotNull Component getComponent(UUID player) {
		return Objects.requireNonNullElseGet(getStatus(player).getComponent(), Component::empty);
	}
}
