package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.plugin.paper.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.paper.commands.LootboxCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class DoOrDieCommand extends VoidCommand {
	private final String effectName = "do_or_die";

	public DoOrDieCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
		for (SuccessCondition condition : Condition.items()) {
			if (condition instanceof Listener listener) {
				Bukkit.getPluginManager().registerEvents(listener, plugin);
			}
		}
	}

	@Override
	public void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
		new TimedEffect.Builder()
				.request(request)
				.duration(DO_OR_DIE_COOLDOWN)
				.startCallback(effect -> {
					List<Player> players = plugin.getPlayers(request);
					List<SuccessCondition> conditions = new ArrayList<>(Condition.items());
					Collections.shuffle(conditions, random);
					SuccessCondition condition = conditions.stream()
							.filter(cond -> cond.canApply(players))
							.findAny()
							.orElseThrow(() -> new IllegalStateException("Could not find a condition that can be applied to all targets"));
					Component subtitle = condition.getComponent();

					Set<UUID> notCompleted = players.stream().map(Player::getUniqueId).collect(Collectors.toSet());
					players.forEach(condition::track);
					int startedAt = Bukkit.getCurrentTick();

					AtomicInteger pastValue = new AtomicInteger(0);
					Bukkit.getScheduler().runTaskTimer(plugin, task -> {
						int ticksElapsed = Bukkit.getCurrentTick() - startedAt;
						int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
						boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
						boolean isTimeUp = secondsLeft <= 0;
						for (UUID uuid : notCompleted) {
							Player player = Bukkit.getPlayer(uuid);
							if (player == null) continue;
							Audience audience = plugin.translator().wrap(player);

							if (condition.hasSucceeded(player)) {
								ItemStack reward = LootboxCommand.createRandomItem(condition.getRewardLuck());
								audience.showTitle(doOrDieSuccess(reward.getType()));
								notCompleted.remove(uuid);
								audience.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), player);
								sync(() -> GiveItemCommand.giveItemTo(player, reward));
							} else if (isTimeUp) {
								condition.reset(player);
								audience.showTitle(DO_OR_DIE_FAILURE);
								player.setHealth(0);
							} else {
								Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
								audience.showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
								if (isNewValue)
									audience.playSound(Sounds.DO_OR_DIE_TICK.get(), player);
							}
						}

						if (isTimeUp)
							task.cancel();
					}, 1, 2);

					announce(players, request);
					return null;
				})
				.build().queue();
	}
}
