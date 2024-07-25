package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.VoidCommand;
import dev.qixils.crowdcontrol.plugin.paper.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.paper.commands.LootboxCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

					CompletableFuture<SuccessCondition> cond = new CompletableFuture<>();
					for (SuccessCondition condition : conditions) {
						AtomicInteger applicable = new AtomicInteger();
						for (Player player : players) {
							player.getScheduler().run(plugin, $ -> {
								if (condition.canApply(player) && applicable.addAndGet(1) == players.size())
									cond.complete(condition);
							}, null);
						}
					}

					SuccessCondition condition = cond.completeOnTimeout(null, 1000, TimeUnit.MILLISECONDS).join(); // TODO: thhis sucks
					if (condition == null) {
						return request.buildResponse()
							.type(Response.ResultType.RETRY)
							.message("Could not find a challenge to apply");
					}

					Component subtitle = condition.getComponent();

					Set<UUID> notCompleted = players.stream().map(Player::getUniqueId).collect(Collectors.toSet());
					players.forEach(player -> player.getScheduler().run(plugin, $ -> condition.track(player), null));

					AtomicInteger pastValue = new AtomicInteger();
					AtomicInteger ticks = new AtomicInteger(1);
					int tickRate = 2;
					Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
						int ticksElapsed = ticks.addAndGet(tickRate);
						int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
						boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
						boolean isTimeUp = secondsLeft <= 0;
						for (UUID uuid : notCompleted) {
							Player player = Bukkit.getPlayer(uuid);
							if (player == null) continue;

							player.getScheduler().run(plugin, $ -> {
								if (condition.hasSucceeded(player)) {
									ItemStack reward = LootboxCommand.createRandomItem(condition.getRewardLuck(), null);
									player.showTitle(doOrDieSuccess(reward.getType()));
									notCompleted.remove(uuid);
									player.playSound(Sounds.DO_OR_DIE_SUCCESS_CHIME.get(), player);
									GiveItemCommand.giveItemTo(player, reward);
								} else if (isTimeUp) {
									condition.reset(player);
									player.showTitle(DO_OR_DIE_FAILURE);
									player.setHealth(0);
								} else {
									Component main = Component.text(secondsLeft).color(doOrDieColor(secondsLeft));
									player.showTitle(Title.title(main, subtitle, DO_OR_DIE_TIMES));
									if (isNewValue)
										player.playSound(Sounds.DO_OR_DIE_TICK.get(), player);
								}
							}, null);
						}

						if (isTimeUp)
							task.cancel();
					}, 1, tickRate);

					announce(players, request);
					return null;
				})
				.build().queue();
	}
}
