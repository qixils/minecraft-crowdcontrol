package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.BukkitCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public class PotionCommand extends ImmediateCommand {
    private final PotionEffectType potionEffectType;
    private final int duration;
    private final String effectName;
    private final String displayName;

    private static final int SECONDS = 15;
    private static final int MAX_DURATION = 20 * SECONDS;

    public PotionCommand(BukkitCrowdControlPlugin plugin, PotionEffectType potionEffectType) {
        super(plugin);
        this.potionEffectType = potionEffectType;
        boolean isMinimal = potionEffectType.isInstant();
        duration = isMinimal ? 1 : MAX_DURATION;
        this.effectName = "potion_" + nameOf(potionEffectType);
        this.displayName = "Apply " + TextUtil.titleCase(nameOf(potionEffectType)) + " Potion Effect (" + SECONDS + "s)";
    }

    @Override
    public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
        PotionEffect potionEffect = potionEffectType.createEffect(duration, random.nextInt(2));
        Bukkit.getScheduler().runTask(plugin, () -> players.forEach(player -> player.addPotionEffect(potionEffect)));
        return request.buildResponse().type(Response.ResultType.SUCCESS);
    }

    private static String nameOf(PotionEffectType type) {
        return switch (type.getName()) {
            case "SLOW" -> "SLOWNESS";
            case "FAST_DIGGING" -> "HASTE";
            case "SLOW_DIGGING" -> "MINING_FATIGUE";
            case "INCREASE_DAMAGE" -> "STRENGTH";
            case "HEAL" -> "HEALING";
            case "HARM" -> "HARMING";
            case "JUMP" -> "JUMP_BOOST";
            case "CONFUSION" -> "NAUSEA";
            case "DAMAGE_RESISTANCE" -> "RESISTANCE";
            case "UNLUCK" -> "BAD_LUCK";
            default -> type.getName();
        };
    }
}
