package dev.qixils.crowdcontrol.plugin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import lombok.RequiredArgsConstructor;
import me.lucko.commodore.Commodore;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;

import static dev.qixils.crowdcontrol.plugin.CrowdControlPlugin.PREFIX;

@RequiredArgsConstructor
public class BukkitCrowdControlCommand implements CommandExecutor {
    private final CrowdControlPlugin plugin;

    public static void register(@NotNull CrowdControlPlugin plugin, @NotNull Commodore commodore, @NotNull PluginCommand command) {
        command.setExecutor(new BukkitCrowdControlCommand(plugin));

        LiteralArgumentBuilder<?> node = LiteralArgumentBuilder.literal("crowdcontrol");
        for (Subcommand subcommand : Subcommand.values())
            node.then(LiteralArgumentBuilder.literal(subcommand.getName()));

        commodore.register(command, node);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("crowdcontrol.command") && !sender.isOp()) {
            sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cYou do not have permission to use this command"));
        }

        if (args.length == 0) {
            sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "Available subcommands:"));
            for (Subcommand subcommand : Subcommand.values()) {
                sender.sendMessage(new TextBuilder(subcommand.getName(), NamedTextColor.RED)
                        .next(" • ", NamedTextColor.GOLD)
                        .next(subcommand.getDescription(), NamedTextColor.YELLOW)
                        .command("/" + label + " " + subcommand.getName())
                        .hover(new TextBuilder("Click to run command"))
                );
            }
        } else {
            String subcommandName = args[0].toLowerCase(Locale.ENGLISH);
            String[] subcommandArgs = Arrays.copyOfRange(args, 1, args.length);
            for (Subcommand subcommand : Subcommand.values()) {
                if (subcommand.getName().equals(subcommandName)) {
                    subcommand.execute(plugin, sender, command, label, subcommandArgs);
                    return true;
                }
            }
            sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cUnknown subcommand &6" + subcommandName));
        }
        return true;
    }

    @RequiredArgsConstructor
    private enum Subcommand {
        CONNECT("Connect to the Crowd Control server") {
            @Override
            public void execute(@NotNull CrowdControlPlugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 0) {
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cExpected 0 arguments, received " + args.length));
                } else if (plugin.crowdControl != null) {
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cServer is already connected or attempting to establish a connection"));
                } else {
                    plugin.initCrowdControl();
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "Server connections have been re-enabled and will be attempted in the background"));
                }
            }
        },
        DISCONNECT("Disconnect from the Crowd Control server") {
            @Override
            public void execute(@NotNull CrowdControlPlugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 0) {
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cExpected 0 arguments, received " + args.length));
                } else if (plugin.crowdControl == null) {
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cServer is already disconnected"));
                } else {
                    plugin.crowdControl.shutdown("Disconnection issued by server administrator");
                    plugin.crowdControl = null;
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "Disconnected from the Crowd Control server"));
                }
            }
        },
        RECONNECT("Reconnects to the Crowd Control server") {
            @Override
            public void execute(@NotNull CrowdControlPlugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
                if (args.length != 0) {
                    sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "&cExpected 0 arguments, received " + args.length));
                    return;
                }

                if (plugin.crowdControl != null)
                    plugin.crowdControl.shutdown("Reconnection issued by server administrator");
                plugin.initCrowdControl();

                sender.sendMessage(TextBuilder.fromPrefix(PREFIX, "Server connection has been reset"));
            }
        };

        private final @NotNull String description;
        private String name;

        public @NotNull String getName() {
            if (name == null)
                name = name().toLowerCase(Locale.ENGLISH);
            return name;
        }

        public @NotNull String getDescription() {
            return description;
        }

        public abstract void execute(@NotNull CrowdControlPlugin plugin, @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args);
    }
}
