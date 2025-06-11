package dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.PaperCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.paper.commands.LootboxCommand;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CCTimedEffect;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCTimedEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class DoOrDieCommand extends PaperCommand implements CCTimedEffect {
	private final String effectName = "do_or_die";

	public DoOrDieCommand(@NotNull PaperCrowdControlPlugin plugin) {
		super(plugin);
		for (SuccessCondition condition : Condition.items()) {
			if (condition instanceof Listener listener) {
				Bukkit.getPluginManager().registerEvents(listener, plugin.getPaperPlugin());
			}
		}
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull Player>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<Player> players = playerSupplier.get();
			List<SuccessCondition> conditions = new ArrayList<>(Condition.items());
			Collections.shuffle(conditions, random);

			CompletableFuture<SuccessCondition> cond = new CompletableFuture<>();
			for (SuccessCondition condition : conditions) {
				AtomicInteger applicable = new AtomicInteger();
				for (Player player : players) {
					player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
						if (condition.canApply(player) && applicable.addAndGet(1) == players.size())
							cond.complete(condition);
					}, null);
				}
			}

			SuccessCondition condition = cond.completeOnTimeout(null, 1000, TimeUnit.MILLISECONDS).join(); // TODO: thhis sucks
			if (condition == null) {
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Could not find a challenge to apply");
			}

			Component subtitle = condition.getComponent();

			Set<UUID> notCompleted = players.stream().map(Player::getUniqueId).collect(Collectors.toSet());
			players.forEach(player -> player.getScheduler().run(plugin.getPaperPlugin(), $ -> condition.track(player), null));

			AtomicInteger pastValue = new AtomicInteger();
			AtomicInteger ticks = new AtomicInteger(1);
			int tickRate = 2;
			Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin.getPaperPlugin(), task -> {
				int ticksElapsed = ticks.addAndGet(tickRate);
				int secondsLeft = (int) DO_OR_DIE_DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
				boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
				boolean isTimeUp = secondsLeft <= 0;
				for (UUID uuid : notCompleted) {
					Player player = Bukkit.getPlayer(uuid);
					if (player == null) continue;

					player.getScheduler().run(plugin.getPaperPlugin(), $ -> {
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

			return new CCTimedEffectResponse(request.getRequestId(), ResponseStatus.TIMED_BEGIN, request.getEffect().getDuration() * 1000L);
		}));
	}
}
