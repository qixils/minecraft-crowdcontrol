package dev.qixils.crowdcontrol.plugin.commands.executeorperish;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface SuccessCondition {
    Component getComponent();

    boolean hasSucceeded(Player player);
}