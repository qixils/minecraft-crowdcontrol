package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.CommandConstants.*;

@Getter
public class DoOrDieCommand extends VoidCommand {
	private final String effectName = "do_or_die";
	private final String displayName = "Do-or-Die";

	public DoOrDieCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
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
					SuccessCondition condition = null;
					while (condition == null && !conditions.isEmpty()) {
						SuccessCondition next = conditions.remove(0);
						boolean isPassing = false;
						for (Player player : players) {
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

					final SuccessCondition finalCondition = condition;
					Component subtitle = condition.getComponent();

					Set<UUID> notCompleted = players.stream().map(Player::getUniqueId).collect(Collectors.toSet());
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

							if (finalCondition.hasSucceeded(player)) {
								player.showTitle(DO_OR_DIE_SUCCESS);
								notCompleted.remove(uuid);
								player.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), player);
							} else if (isTimeUp) {
								player.showTitle(DO_OR_DIE_FAILURE);
								player.setHealth(0);
							} else {
								Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
								player.showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
								if (isNewValue)
									player.playSound(Sounds.DO_OR_DIE_TICK.get(), player);
							}
						}

						if (isTimeUp)
							task.cancel();
					}, 0, 2);

					announce(players, request);
					return null;
				})
				.build().queue();
	}
}
