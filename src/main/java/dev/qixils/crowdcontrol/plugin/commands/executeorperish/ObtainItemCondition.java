package dev.qixils.crowdcontrol.plugin.commands.executeorperish;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

@Getter
public class ObtainItemCondition implements SuccessCondition {
    private final Material item;
    private final Component component;

    public ObtainItemCondition(String displayText, Material item) {
        this.item = item;
        component = Component.text("Obtain ").append(Component.text(displayText)
                .replaceText(builder -> builder.matchLiteral("%s").once()
                        .replacement(Component.translatable(item).color(NamedTextColor.GREEN))));
    }

    @Override
    public boolean hasSucceeded(Player player) {
        return player.getInventory().contains(item);
    }
}
