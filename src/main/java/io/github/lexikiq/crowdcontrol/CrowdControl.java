package io.github.lexikiq.crowdcontrol;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;

public final class CrowdControl extends JavaPlugin {
    // actual stuff
    public static final String PREFIX = "!";
    private final Map<String, ChatCommand> commands = new HashMap<>();
    private TwitchClient twitchClient;
    private final FileConfiguration config = getConfig();
    public static final ChatColor USER_COLOR = ChatColor.of(new Color(0x9f44db));
    public static final ChatColor CMD_COLOR = ChatColor.of(new Color(0xb15be3));
    private final Map<ClassCooldowns, LocalDateTime> cooldowns = new HashMap<>();
    private final boolean hasChatToken;

    public CrowdControl() {
        // default config
        config.addDefault("channel", "lexikiq");
        String ircDefault = "YOUR_IRC_TOKEN";
        config.addDefault("irc", ircDefault);
        config.options().copyDefaults(true);
        saveConfig();

        // placeholder cooldowns
        for (ClassCooldowns cooldown : ClassCooldowns.values()) {
            cooldowns.put(cooldown, LocalDateTime.MIN);
        }

        String ircToken = config.getString("irc");
        hasChatToken = ircToken != null && !ircToken.equals(ircDefault);

        // register twitch commands
        RegisterCommands.register(this);
    }

    @Override
    public void onEnable() {
        // twitch stuff
        TwitchClientBuilder twitchClientBuilder = TwitchClientBuilder.builder()
                .withEnableChat(true);
        if (hasChatToken) {
            OAuth2Credential credential = new OAuth2Credential("twitch", Objects.requireNonNull(config.getString("irc")));
            twitchClientBuilder = twitchClientBuilder.withChatAccount(credential).withEnablePubSub(true);
        }
        twitchClient = twitchClientBuilder.build();
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

        List<Player> players = getPlayers();
        if (players.isEmpty()) {
            return;
        }

        ChatCommand chatCommand = commands.get(command);
        if (!chatCommand.canUse()) {
            if (hasChatToken) {
                sendMessage(event, event.getUser().getName() + ": !" + command + " is on cooldown for " + formatTime(chatCommand.refreshesAt()));
            }
            return;
        }

        // global cooldowns
        ClassCooldowns cooldownType = chatCommand.getClassCooldown();
        String cooldownTypeName;
        if (cooldownType != null) {
            cooldownTypeName = WordUtils.capitalizeFully(cooldownType.name().replace('_',' '));
            LocalDateTime refreshesAt = cooldowns.get(cooldownType).plusSeconds(cooldownType.getSeconds());
            if (LocalDateTime.now().isBefore(refreshesAt)) {
                if (hasChatToken) {
                    sendMessage(event, event.getUser().getName() + ": " + cooldownTypeName + " commands are on cooldown for " + formatTime(refreshesAt));
                }
                return;
            }
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

    public List<Player> getPlayers() {
        return ImmutableList.copyOf(getServer().getOnlinePlayers());
    }

    public void registerCommand(String name, ChatCommand command) throws AlreadyRegisteredException {
//        name = name.toLowerCase(java.util.Locale.ENGLISH);
        if (commands.containsKey(name)) {
            throw new AlreadyRegisteredException(name);
        }
        commands.put(name, command);
        getLogger().fine("Registered Twitch command '"+name+"'");
    }

    public static String formatTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        int totalSeconds = (int) now.until(dateTime, ChronoUnit.SECONDS);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String output = "";
        if (minutes > 0) {
            output += minutes + "m";
        }
        if (seconds > 0 || output.isEmpty()) {
            output += seconds + "s";
        }
        return output;
    }

    public static void sendMessage(ChannelMessageEvent event, String message) {
        event.getTwitchChat().sendMessage(event.getChannel().getName(), message);
    }
}
