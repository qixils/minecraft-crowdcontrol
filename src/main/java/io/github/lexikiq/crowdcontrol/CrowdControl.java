package io.github.lexikiq.crowdcontrol;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CrowdControl extends JavaPlugin {
    public static final String PREFIX = "!";
    private final Map<String, ChatCommand> commands = new HashMap<>();
    private TwitchClient twitchClient;
    private final FileConfiguration config = getConfig();
    public static final ChatColor USER_COLOR = ChatColor.of(new Color(0x9f44db));
    public static final ChatColor CMD_COLOR = ChatColor.of(new Color(0xb15be3));

    @Override
    public void onEnable() {
        // default config
        config.addDefault("channel", "lexikiq");
        config.options().copyDefaults(true);
        saveConfig();

        // twitch stuff
        twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .build();
        twitchClient.getChat().joinChannel(config.getString("channel"));
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(this); // registers all events with @EventSubscriber

        RegisterCommands.register(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        twitchClient.close();
    }

    @EventSubscriber
    public void handleMessage(ChannelMessageEvent event) {
        String message = event.getMessage();
        if (!message.startsWith(PREFIX)) {return;}
        String command = message.substring(PREFIX.length()).toLowerCase();
        if (commands.containsKey(command)) {
            Collection<? extends Player> players = getPlayers();
            ChatCommand chatCommand = commands.get(command);
            if (!players.isEmpty() && chatCommand.canUse()) {
                getServer().broadcastMessage(USER_COLOR+event.getUser().getName()+ChatColor.RESET+" used command "+CMD_COLOR+event.getMessage());
                chatCommand.execute(event, players);
            }
        }
    }

    public Collection<? extends Player> getPlayers() {
        return getServer().getOnlinePlayers();
    }

    public void registerCommand(String name, ChatCommand command) throws AlreadyRegisteredException {
        name = name.toLowerCase();
        if (commands.containsKey(name)) {
            throw new AlreadyRegisteredException(name);
        }
        commands.put(name, command);
        getLogger().info("Registered Twitch command '"+name+"'");
    }
}
