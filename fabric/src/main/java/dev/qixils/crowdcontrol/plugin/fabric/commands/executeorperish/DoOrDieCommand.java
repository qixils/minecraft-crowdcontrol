package dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.VoidCommand;
import dev.qixils.crowdcontrol.plugin.fabric.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.fabric.commands.LootboxCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import dev.qixils.crowdcontrol.socket.Request;
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
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

// TODO: first tick or two displays the timer as "31" instead of "30"

@Getter
@EventListener
public class DoOrDieCommand extends VoidCommand {
	private final String effectName = "do_or_die";
	private final List<Task> tasks = new ArrayList<>();

	public DoOrDieCommand(@NotNull FabricCrowdControlPlugin plugin) {
		super(plugin);
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
	public void voidExecute(@NotNull List<@NotNull ServerPlayer> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.duration(DO_OR_DIE_COOLDOWN)
				.startCallback(effect -> {
					List<ServerPlayer> players = plugin.getPlayers(request);
					List<SuccessCondition> conditions = new ArrayList<>(Condition.items());
					Collections.shuffle(conditions, random);
					SuccessCondition condition = null;
					while (condition == null && !conditions.isEmpty()) {
						SuccessCondition next = conditions.remove(0);
						boolean isPassing = false;
						for (ServerPlayer player : players) {
							if (next.hasSucceeded(player)) {
								isPassing = true;
								break;
							}
						}
						if (!isPassing)
							condition = next;
					}

					if (condition == null)
						throw new IllegalStateException("Could not find a condition that is not already being met by all targets");

					Task task = new Task(
							plugin,
							plugin.server().getTickCount(),
							players.stream().map(ServerPlayer::getUUID).collect(Collectors.toSet()),
							condition,
							condition.getComponent()
					);
					if (!task.run(null))
						task.register();

					announce(players, request);
					return null;
				})
				.build().queue();
	}

	@Data
	private final class Task {
		private final FabricCrowdControlPlugin plugin;
		private final int startedAt;
		private final Set<UUID> notCompleted;
		private final SuccessCondition condition;
		private final Component subtitle;
		private int pastValue = 0;

		public int ticksElapsed(@Nullable Tick event) {
			int tick = event == null ? plugin.server().getTickCount() : event.tickCount();
			return tick - startedAt;
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
					ItemStack reward = plugin.commandRegister().getCommandByName("lootbox", LootboxCommand.class).createRandomItem(condition.getRewardLuck());
					player.showTitle(doOrDieSuccess(reward.getItem().getName(reward)));
					notCompleted.remove(uuid);
					player.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), Sound.Emitter.self());
					sync(() -> GiveItemCommand.giveItemTo(player, reward));
				} else if (isTimeUp) {
					player.showTitle(DO_OR_DIE_FAILURE);
					player.setHealth(0);
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
