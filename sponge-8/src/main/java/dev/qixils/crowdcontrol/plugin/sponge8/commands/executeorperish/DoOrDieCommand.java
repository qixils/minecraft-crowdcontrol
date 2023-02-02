package dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.VoidCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.LootboxCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class DoOrDieCommand extends VoidCommand {
	private final String effectName = "do_or_die";

	public DoOrDieCommand(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
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

					final SuccessCondition finalCondition = condition;
					Component subtitle = condition.getComponent();

					Set<UUID> notCompleted = players.stream().map(Player::uniqueId).collect(Collectors.toSet());
					Server server = plugin.getGame().server();
					long startedAt = server.runningTimeTicks().ticks();

					AtomicInteger pastValue = new AtomicInteger(0);
					plugin.getSyncScheduler().submit(Task.builder()
							.delay(Ticks.of(1))
							.interval(Ticks.of(2))
							.execute(task -> {
								long ticksElapsed = server.runningTimeTicks().ticks() - startedAt;
								int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
								boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
								boolean isTimeUp = secondsLeft <= 0;
								for (UUID uuid : notCompleted) {
									ServerPlayer player = server.player(uuid).orElse(null);
									if (player == null) continue;

									if (finalCondition.hasSucceeded(player)) {
										ItemStack item = plugin.commandRegister()
												.getCommandByName("lootbox", LootboxCommand.class)
												.createRandomItem(finalCondition.getRewardLuck());
										player.showTitle(doOrDieSuccess(item.type()));
										notCompleted.remove(uuid);
										player.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), Sound.Emitter.self());
										GiveItemCommand.giveItemTo(plugin, player, item);
									} else if (isTimeUp) {
										player.showTitle(DO_OR_DIE_FAILURE);
										player.offer(Keys.HEALTH, 0d);
									} else {
										Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
										player.showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
										if (isNewValue)
											player.playSound(Sounds.DO_OR_DIE_TICK.get(), Sound.Emitter.self());
									}
								}

								if (isTimeUp)
									task.cancel();
							})
							.plugin(plugin.getPluginContainer())
							.build());

					playerAnnounce(players, request);
					return null;
				})
				.build().queue();
	}
}
