package io.github.lexikiq.crowdcontrol;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final Map<ClassCooldowns, LocalDateTime> cooldowns = new HashMap<>();

    public CrowdControl() {
        // default config
        config.addDefault("channel", "lexikiq");
        config.options().copyDefaults(true);
        saveConfig();

        // placeholder cooldowns
        for (ClassCooldowns cooldown : ClassCooldowns.values()) {
            cooldowns.put(cooldown, LocalDateTime.MIN);
        }

        // register twitch commands
        RegisterCommands.register(this);
    }

    @Override
    public void onEnable() {
        // twitch stuff
        twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .build();
        twitchClient.getChat().joinChannel(config.getString("channel"));
        twitchClient.getEventManager().getEventHandler(SimpleEventHandler.class).registerListener(this); // registers all events with @EventSubscriber
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        twitchClient.close();
    }

    @EventSubscriber
    public void handleMessage(ChannelMessageEvent event) {
        // get command/message
        String message = event.getMessage();
        if (!message.startsWith(PREFIX)) {return;}
        String text = message.substring(PREFIX.length()).toLowerCase(java.util.Locale.ENGLISH);
        String[] split = text.split(" ");
        String command = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        // if message isn't command, exit
        if (!commands.containsKey(command)) {
            return;
        }

        Collection<? extends Player> players = getPlayers();
        ChatCommand chatCommand = commands.get(command);

        // global cooldowns
        ClassCooldowns cooldownType = chatCommand.getClassCooldown();
        boolean cooldownUsable = cooldownType == null || cooldowns.get(cooldownType).plusSeconds(cooldownType.getSeconds()).isBefore(LocalDateTime.now());
        if (!(!players.isEmpty() && chatCommand.canUse() && cooldownUsable)) {
            return;
        }

        // actually execute the command!
        boolean executed = chatCommand.execute(event, players, args);
        // exit if execution failed
        if (!executed) {
            return;
        }

        // set cooldown & display output
        chatCommand.setCooldown();
        getServer().broadcastMessage(USER_COLOR + event.getUser().getName() + ChatColor.RESET + " used command " + CMD_COLOR + event.getMessage());
        // display when command is usable
        if (chatCommand.getCooldownSeconds() > 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    getServer().broadcastMessage(CMD_COLOR + ChatColor.ITALIC.toString() + PREFIX + chatCommand.getCommand().toLowerCase(java.util.Locale.ENGLISH) + ChatColor.RESET + ChatColor.ITALIC + " has refreshed.");
                }
            }.runTaskLaterAsynchronously(this, 20L*chatCommand.getCooldownSeconds());
        }
        // display when command group is usable
        if (cooldownType != null) {
            cooldowns.put(cooldownType, LocalDateTime.now());
            new BukkitRunnable(){
                @Override
                public void run() {
                    getServer().broadcastMessage(CMD_COLOR + ChatColor.ITALIC.toString() + WordUtils.capitalizeFully(cooldownType.name().replace('_',' ')) + ChatColor.RESET + ChatColor.ITALIC + " commands have refreshed.");
                }
            }.runTaskLaterAsynchronously(this, 20L*cooldownType.getSeconds());
        }
    }

    public Collection<? extends Player> getPlayers() {
        return getServer().getOnlinePlayers();
    }

    public void registerCommand(String name, ChatCommand command) throws AlreadyRegisteredException {
//        name = name.toLowerCase(java.util.Locale.ENGLISH);
        if (commands.containsKey(name)) {
            throw new AlreadyRegisteredException(name);
        }
        commands.put(name, command);
        getLogger().info("Registered Twitch command '"+name+"'");
    }
}
