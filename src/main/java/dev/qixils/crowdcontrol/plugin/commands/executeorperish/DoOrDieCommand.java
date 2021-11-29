package dev.qixils.crowdcontrol.plugin.commands.executeorperish;

import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.VoidCommand;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public class DoOrDieCommand extends VoidCommand {
    private static final Duration DURATION = Duration.ofSeconds(31);
    private static final Duration COOLDOWN = DURATION.multipliedBy(3);
    private static final int TICKS = (int) (DURATION.getSeconds() * 20);
    private static final TextColor START_COLOR = TextColor.color(0xE4F73D);
    private static final TextColor END_COLOR = TextColor.color(0xF42929);
    private static final Title.Times TIMES = Title.Times.of(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1));
    private static final Title SUCCESS_TITLE = Title.title(
            Component.empty(),
            Component.text("Task Completed!").color(NamedTextColor.GREEN),
            TIMES
    );
    private static final Title FAILURE_TITLE = Title.title(
            Component.empty(),
            Component.text("Task Failed").color(NamedTextColor.RED),
            TIMES
    );
    private final String effectName = "do_or_die";
    private final String displayName = "Do-or-Die";

    public DoOrDieCommand(@NotNull CrowdControlPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void voidExecute(@NotNull List<@NotNull Player> ignored, @NotNull Request request) {
        new TimedEffect(request, COOLDOWN, effect -> {
            List<Player> players = plugin.getPlayers(request);
            List<SuccessCondition> conditions = new ArrayList<>(Condition.items());
            Collections.shuffle(conditions, rand);
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
                int secondsLeft = (int) DURATION.getSeconds() - (int) Math.ceil(ticksElapsed / 20f);
                boolean isNewValue = secondsLeft != pastValue.getAndSet(secondsLeft);
                boolean isTimeUp = secondsLeft <= 0;
                for (UUID uuid : notCompleted) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;

                    if (finalCondition.hasSucceeded(player)) {
                        player.showTitle(SUCCESS_TITLE);
                        notCompleted.remove(uuid);
                        player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, Source.MASTER, 1, 1.5f), player);
                    } else if (isTimeUp) {
                        player.showTitle(FAILURE_TITLE);
                        player.damage(1);
                        if (!player.isDead())
                            player.setHealth(0);
                    } else {
                        Component main = Component.text(secondsLeft)
                                .color(TextColor.lerp((float) secondsLeft / DURATION.getSeconds(), END_COLOR, START_COLOR));
                        player.showTitle(Title.title(main, subtitle, TIMES));
                        if (isNewValue)
                            player.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, Source.MASTER, 1, 1), player);
                    }
                }

                if (isTimeUp)
                    task.cancel();
            }, 0, 2);

            announce(players, request);
        }, null).queue();
    }
}
