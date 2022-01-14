package dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

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

	public DoOrDieCommand(@NotNull SpongeCrowdControlPlugin plugin) {
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
					Server server = plugin.getGame().getServer();
					int startedAt = server.getRunningTimeTicks();

					AtomicInteger pastValue = new AtomicInteger(0);
					Task.builder()
							.intervalTicks(2)
							.execute(task -> {
								int ticksElapsed = server.getRunningTimeTicks() - startedAt;
								int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
								boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
								boolean isTimeUp = secondsLeft <= 0;
								for (UUID uuid : notCompleted) {
									Player player = server.getPlayer(uuid).orElse(null);
									if (player == null) continue;

									if (finalCondition.hasSucceeded(player)) {
										plugin.asAudience(player).showTitle(DO_OR_DIE_SUCCESS);
										notCompleted.remove(uuid);
										plugin.asAudience(player).playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get());
									} else if (isTimeUp) {
										plugin.asAudience(player).showTitle(DO_OR_DIE_FAILURE);
										player.offer(Keys.HEALTH, 0d);
									} else {
										Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
										plugin.asAudience(player).showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
										if (isNewValue)
											plugin.asAudience(player).playSound(Sounds.DO_OR_DIE_TICK.get());
									}
								}

								if (isTimeUp)
									task.cancel();
							})
							.submit(plugin);

					playerAnnounce(players, request);
					return null;
				})
				.build().queue();
	}
}
