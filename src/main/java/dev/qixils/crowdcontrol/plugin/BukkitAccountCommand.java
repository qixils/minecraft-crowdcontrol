package dev.qixils.crowdcontrol.plugin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import lombok.RequiredArgsConstructor;
import me.lucko.commodore.Commodore;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BukkitAccountCommand implements CommandExecutor {
    private final PlayerMapper mapper;

    public static void register(@NotNull PlayerMapper mapper, @NotNull Commodore commodore, @NotNull PluginCommand command) {
        command.setExecutor(new BukkitAccountCommand(mapper));

        var usernameNode = RequiredArgumentBuilder.argument("twitch_name", StringArgumentType.word());
        var node = LiteralArgumentBuilder.literal("account")
                .then(LiteralArgumentBuilder.literal("link").then(usernameNode))
                .then(LiteralArgumentBuilder.literal("unlink").then(usernameNode));

        commodore.register(command, node);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Optional<UUID> optionalUUID = sender.get(Identity.UUID);
        if (optionalUUID.isEmpty()) {
            sender.sendMessage(Component.text("This command can only be run by players").color(NamedTextColor.RED));
            return true;
        }

        UUID uuid = optionalUUID.get();

        if (args.length != 2)
            return false;

        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        String username = args[1].toLowerCase(Locale.ENGLISH);
        if (subcommand.equals("unlink")) {
            mapper.twitchToUserMap.remove(username, uuid);

            sender.sendMessage(new TextBuilder()
                    .next(username, NamedTextColor.AQUA)
                    .next(" has been removed from your linked Twitch accounts", NamedTextColor.WHITE));
        } else if (subcommand.equals("link")) {
            mapper.twitchToUserMap.put(username, uuid);

            sender.sendMessage(new TextBuilder()
                    .next(username, NamedTextColor.AQUA)
                    .next(" has been added to your linked Twitch accounts", NamedTextColor.WHITE));
        } else {
            return false;
        }

        return true;
    }
}
