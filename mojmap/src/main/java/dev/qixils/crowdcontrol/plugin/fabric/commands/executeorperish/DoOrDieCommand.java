package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.fabric.commands.LootboxCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Data;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
@EventListener
public class DoOrDieCommand extends ModdedCommand {
	private final String effectName = "do_or_die";
	private final List<Task> tasks = new ArrayList<>();

	public DoOrDieCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
		for (SuccessCondition condition : Condition.items()) {
			if (condition.getClass().isAnnotationPresent(EventListener.class)) {
				plugin.getEventManager().registerListeners(condition);
			}
		}
	}

	@Listener
	public void onTick(Tick tick) {
		Iterator<Task> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			Task task = iterator.next();
			if (task.ticksElapsed(tick) % 2 != 0)
				continue;
			if (task.run(tick))
				iterator.remove();
		}
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		// TODO: cooldown
		List<ServerPlayer> players = playerSupplier.get();
		List<SuccessCondition> conditions = new ArrayList<>(Condition.items());
		Collections.shuffle(conditions, random);
		SuccessCondition condition = conditions.stream()
			.filter(cond -> cond.canApply(players))
			.findAny()
			.orElseThrow(() -> new IllegalStateException("Could not find a condition that can be applied to all targets"));
		players.forEach(condition::track);

		Task task = new Task(
			plugin,
			plugin.server().getTickCount(),
			players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet()),
			condition,
			condition.getComponent()
		);
		if (!task.run(null))
			task.register();
	}

	@Data
	private final class Task {
		private final ModdedCrowdControlPlugin plugin;
		private final int startedAt;
		private final Set<UUID> notCompleted;
		private final SuccessCondition condition;
		private final Component subtitle;
		private int pastValue = 0;

		public int ticksElapsed(@Nullable Tick event) {
			int tick = event == null ? plugin.server().getTickCount() : event.tickCount();
			return Math.max(tick - startedAt, 1);
		}

		public boolean run(@Nullable Tick event) {
			int ticksElapsed = ticksElapsed(event);
			int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
			boolean isNewValue = secondsLeft != pastValue;
			pastValue = secondsLeft;
			boolean isTimeUp = secondsLeft <= 0;
			for (UUID uuid : notCompleted) {
				ServerPlayer player = plugin.server().getPlayerList().getPlayer(uuid);
				if (player == null) continue;

				if (condition.hasSucceeded(player)) {
					ItemStack reward = plugin.commandRegister().getCommandByName("lootbox", LootboxCommand.class).createRandomItem(condition.getRewardLuck(), player.registryAccess());
					player.showTitle(doOrDieSuccess(plugin.toAdventure(reward.getItem().getName(reward))));
					notCompleted.remove(uuid);
					player.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), Sound.Emitter.self());
					sync(() -> GiveItemCommand.giveItemTo(player, reward));
				} else if (isTimeUp) {
					condition.reset(player);
					player.showTitle(DO_OR_DIE_FAILURE);
					player.kill(player.serverLevel());
				} else {
					Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
					player.showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
					if (isNewValue)
						player.playSound(Sounds.DO_OR_DIE_TICK.get(), Sound.Emitter.self());
				}
			}
			return isTimeUp;
		}

		public void register() {
			tasks.add(this);
		}
	}
}
