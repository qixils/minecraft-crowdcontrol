package io.github.lexikiq.crowdcontrol;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.domain.Event;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.philippheuer.events4j.simple.domain.EventSubscriber;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.pubsub.domain.ChannelBitsData;
import com.github.twitch4j.pubsub.domain.ChannelPointsRedemption;
import com.github.twitch4j.pubsub.events.ChannelBitsEvent;
import com.github.twitch4j.pubsub.events.ChannelPointsRedemptionEvent;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
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
    private final Map<Integer, CommandWrapper> bitCommands = new HashMap<>();
    private final Map<Integer, CommandWrapper> pointCommands = new HashMap<>();

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
        config.addDefault("command_replies", true);
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
            twitchClientBuilder = twitchClientBuilder
                    .withChatAccount(credential)
                    .withEnablePubSub(true)
            ;
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

    protected void executeCommand(Event event, ChatCommand command, String[] args, CommandType commandType) {
        List<Player> players = getPlayers();
        if (players.isEmpty()) {
            return;
        }

        String username = null;
        switch (commandType) {
            case CHAT:
                username = ((ChannelMessageEvent) event).getUser().getName();
                break;
            case BITS:
                username = ((ChannelBitsEvent) event).getData().getUserName();
                break;
            case POINTS:
                username = ((ChannelPointsRedemptionEvent) event).getRedemption().getUser().getLogin();
        }
        assert username != null;

        // cooldowns
        ClassCooldowns cooldownType = command.getClassCooldown();
        if (commandType.usesCooldown()) {
            // per-cmd cooldown
            if (!command.canUse()) {
                if (hasChatToken && config.getBoolean("command_replies") && commandType == CommandType.CHAT) {
                    sendMessage((ChannelMessageEvent) event, String.format(
                            "%s: %s is on cooldown for %s",
                            username,
                            command.getCommand().toLowerCase(Locale.ENGLISH),
                            formatTime(command.refreshesAt())
                    ));
                }
                return;
            }

            // global cooldowns
            String cooldownTypeName;
            if (cooldownType != null) {
                cooldownTypeName = WordUtils.capitalizeFully(cooldownType.name().replace('_', ' '));
                LocalDateTime refreshesAt = cooldowns.get(cooldownType).plusSeconds(cooldownType.getSeconds());
                if (LocalDateTime.now().isBefore(refreshesAt)) {
                    if (hasChatToken && config.getBoolean("command_replies") && commandType == CommandType.CHAT) {
                        sendMessage((ChannelMessageEvent) event, String.format(
                                "%s: %s commands are on cooldown for %s",
                                username,
                                cooldownTypeName,
                                formatTime(refreshesAt)
                        ));
                    }
                    return;
                }
            }
        }

        // actually execute the command!
        boolean executed = command.execute(username, players, args);
        // exit if execution failed
        if (!executed) {
            return;
        }

        // set cooldown & display output
        String commandText = PREFIX+command.getCommand().toLowerCase(Locale.ENGLISH) + " " + String.join(" ", args);
        getServer().broadcastMessage(String.format(
                "%s%s%s used command %s%s",
                USER_COLOR,
                username,
                ChatColor.RESET,
                CMD_COLOR,
                commandText
        ));

        // exit if cmd type doesn't do cooldowns
        if (!commandType.usesCooldown()) return;

        //// cooldown stuff ////
        command.setCooldown();

        // display when command is usable
//        if (command.getCooldownSeconds() > 0) {
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    getServer().broadcastMessage(String.format(
//                            "%s%s%s%s%s has refreshed.",
//                            CMD_COLOR,
//                            ChatColor.ITALIC,
//                            PREFIX + command.getCommand().toLowerCase(Locale.ENGLISH),
//                            ChatColor.RESET,
//                            ChatColor.ITALIC
//                    ));
//                }
//            }.runTaskLaterAsynchronously(this, 20L*command.getCooldownSeconds());
//        }

        // display when command group is usable
        if (cooldownType != null) {
            cooldowns.put(cooldownType, LocalDateTime.now());
            new BukkitRunnable(){
                @Override
                public void run() {
                    getServer().broadcastMessage(String.format(
                            "%s%s%s%s%s commands have refreshed.",
                            CMD_COLOR,
                            ChatColor.ITALIC,
                            WordUtils.capitalizeFully(cooldownType.name().replace('_',' ')),
                            ChatColor.RESET,
                            ChatColor.ITALIC
                    ));
                }
            }.runTaskLaterAsynchronously(this, 20L*cooldownType.getSeconds());
        }
    }

    @EventSubscriber
    public void handleMessage(ChannelMessageEvent event) {
        // get command/message
        String message = event.getMessage();
        if (!message.startsWith(PREFIX)) {return;}
        String text = message.substring(PREFIX.length()).toLowerCase(Locale.ENGLISH);
        String[] split = text.split(" ");
        String command = split[0];
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        // if message isn't command, exit
        if (!commands.containsKey(command)) {
            return;
        }

        executeCommand(event, commands.get(command), args, CommandType.CHAT);
    }

    @EventSubscriber
    public void handleBits(ChannelBitsEvent event) {
        ChannelBitsData bitsData = event.getData();
        CommandWrapper commandWrapper = getFlooredMapObject(bitCommands, bitsData.getBitsUsed());
        if (commandWrapper == null) return;
        ChatCommand command = commandWrapper.getCommand();
        String[] args = commandWrapper.getArgs();
        executeCommand(event, command, args, CommandType.BITS);
    }

    @EventSubscriber
    public void handlePoints(ChannelPointsRedemptionEvent event) {
        ChannelPointsRedemption redemption = event.getRedemption();
        CommandWrapper commandWrapper = getFlooredMapObject(pointCommands, (int) redemption.getReward().getCost());
        if (commandWrapper == null) return;
        ChatCommand command = commandWrapper.getCommand();
        String[] args = commandWrapper.getArgs();
        executeCommand(event, command, args, CommandType.POINTS);
    }

    public static CommandWrapper getFlooredMapObject(Map<Integer, CommandWrapper> map, int value) {
        int max = 0;
        CommandWrapper object = null;
        for (Map.Entry<Integer, CommandWrapper> entry : map.entrySet()) {
            int key = entry.getKey();
            if (key > max && value >= key) {
                max = key;
                object = entry.getValue();
            }
        }
        return object;
    }

    public List<Player> getPlayers() {
        return ImmutableList.copyOf(getServer().getOnlinePlayers());
    }

    public void registerCommand(String name, ChatCommand command) throws AlreadyRegisteredException {
        name = name.toLowerCase(Locale.ENGLISH);
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
