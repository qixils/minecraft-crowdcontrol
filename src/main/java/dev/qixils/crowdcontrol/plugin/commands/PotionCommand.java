package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.Command;
import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public class PotionCommand extends Command {
    private final PotionEffectType potionEffectType;
    private final int duration;
    private final String effectName;
    private final String displayName;

    private static final int MAX_DURATION = 20*15;

    public PotionCommand(CrowdControlPlugin plugin, PotionEffectType potionEffectType) {
        super(plugin);
        this.potionEffectType = potionEffectType;
        boolean isMinimal = potionEffectType.isInstant();
        duration = isMinimal ? 1 : MAX_DURATION;
        this.effectName = "potion-" + potionEffectType.getName();
        this.displayName = "Apply " + potionEffectType.getName() + " Potion Effect"; // TODO: proper potion name
    }

    @Override
    public Response.Result execute(Request request) {
        PotionEffect potionEffect = potionEffectType.createEffect(duration, rand.nextInt(2));
        Bukkit.getScheduler().runTask(plugin, () -> CrowdControlPlugin.getPlayers().forEach(player -> player.addPotionEffect(potionEffect))); // TODO: can this be async?
        return Response.Result.SUCCESS;
    }
}
