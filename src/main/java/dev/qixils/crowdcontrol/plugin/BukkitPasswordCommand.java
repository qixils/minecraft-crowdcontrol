package dev.qixils.crowdcontrol.plugin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import lombok.RequiredArgsConstructor;
import me.lucko.commodore.Commodore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class BukkitPasswordCommand implements CommandExecutor {
    private static final String PERMISSION = "crowdcontrol.password";
    private static final Component PERMISSION_ERROR = TextBuilder.fromPrefix(CrowdControlPlugin.PREFIX).group()
            .color(NamedTextColor.RED)
            .rawNext("You must be operator or have the permission node ")
            .next(PERMISSION, NamedTextColor.YELLOW)
            .rawNext(" to use this command.")
            .build();
    private static final Component SUCCESS = TextBuilder.fromPrefix(CrowdControlPlugin.PREFIX)
            .rawNext("The password has been updated. Please use ")
            .next("/crowdcontrol reconnect", NamedTextColor.YELLOW)
            .rawNext(" or click here to apply this change.")
            .suggest("/crowdcontrol reconnect")
            .hover(new TextBuilder().rawNext("Click here to run ").next("/crowdcontrol reconnect", NamedTextColor.YELLOW))
            .build();
    private final CrowdControlPlugin plugin;

    public static void register(@NotNull CrowdControlPlugin plugin, @NotNull Commodore commodore, @NotNull PluginCommand command) {
        command.setExecutor(new BukkitPasswordCommand(plugin));

        LiteralArgumentBuilder<?> node = LiteralArgumentBuilder.literal("password")
                .then(RequiredArgumentBuilder.argument("password", StringArgumentType.greedyString()));

        commodore.register(command, node);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        if (!sender.isOp() && sender.permissionValue(PERMISSION) != TriState.TRUE) {
            sender.sendMessage(PERMISSION_ERROR);
            return true;
        }
        plugin.manualPassword = String.join(" ", args);
        sender.sendMessage(SUCCESS);
        return true;
    }
}
