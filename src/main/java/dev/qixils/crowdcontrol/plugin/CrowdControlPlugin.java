package dev.qixils.crowdcontrol.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.TimedEffect;
import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import me.lucko.commodore.CommodoreProvider;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CrowdControlPlugin extends JavaPlugin {
    // actual stuff
    CrowdControl crowdControl = null;
    List<Command> commands;
    final FileConfiguration config = getConfig();
    public static final TextColor USER_COLOR = TextColor.color(0x9f44db);
    public static final TextColor CMD_COLOR = TextColor.color(0xb15be3);
    private static final int port = 58431;
    static boolean global = false; // I don't like making this static, but it's an easy fix
    private List<String> hosts = Collections.emptyList();
    private boolean announce = true;

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    void initCrowdControl() {
        String password = config.getString("password", "");
        String ip = config.getString("ip", "127.0.0.1");
        global = config.getBoolean("global", false);
        hosts = config.getStringList("hosts");
        announce = config.getBoolean("announce", true);

        if (!password.isBlank()) {
            getLogger().info("Running Crowd Control in server mode");
            crowdControl = CrowdControl.server().port(port).password(password).build();
        } else if (!ip.isBlank()) {
            getLogger().info("Running Crowd Control in client mode");
            crowdControl = CrowdControl.client().port(port).ip(ip).build();
        } else {
            throw new IllegalStateException("Config file is improperly configured; please ensure you have entered a valid IP address or password.");
        }

        if (commands == null)
            commands = RegisterCommands.register(this);
        else
            RegisterCommands.register(this, commands);
    }

    @Override
    public void onEnable() {
        initCrowdControl();

        BukkitCrowdControlCommand.register(
                this,
                CommodoreProvider.getCommodore(this),
                Objects.requireNonNull(getCommand("crowdcontrol"), "plugin.yml is improperly configured; cannot find crowdcontrol command")
        );
    }

    @Override
    public void onDisable() {
        if (crowdControl == null) return;
        crowdControl.shutdown();
        crowdControl = null;
        commands = null;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public boolean announceEffects() {
        return announce;
    }

    @CheckReturnValue
    public static boolean isGlobal(@NotNull Request request) {
        return global || request.isGlobal();
    }

    @CheckReturnValue
    @NotNull
    private static List<@NotNull Player> getPlayers() {
        return Bukkit.getServer().getOnlinePlayers().stream()
                .filter(player -> !player.isDead() && player.getGameMode() != GameMode.SPECTATOR)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @CheckReturnValue
    @NotNull
    public static CompletableFuture<@NotNull List<@NotNull Player>> getPlayers(final @NotNull Request request) {
        if (isGlobal(request)) {
            return CompletableFuture.completedFuture(getPlayers());
        } else {
            Target[] targets = request.getTargets();
            List<CompletableFuture<Player>> futures = new ArrayList<>(targets.length);
            for (Target target : targets)
                futures.add(playerFromTarget(target));

            CompletableFuture<List<Player>> playersFuture = new CompletableFuture<>();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenAccept($ -> {
                List<Player> players = new ArrayList<>(futures.size());
                for (CompletableFuture<Player> future : futures) {
                    try {
                        Player player = future.get();
                        if (player != null)
                            players.add(player);
                    } catch (CancellationException | ExecutionException | InterruptedException ignored) {}
                }

                if (players.isEmpty())
                    playersFuture.completeExceptionally(new NoApplicableTarget());
                else
                    playersFuture.complete(players);
            });
            return playersFuture;
        }
    }

    private static final Logger TARGET_LOGGER = Logger.getLogger("MC-CC-TargetMapper");
    private static final Map<Integer, UUID> TWITCH_TO_USER_MAP = new HashMap<>();
    private static final Executor TARGET_EXECUTOR = Executors.newCachedThreadPool();
    private static final Gson GSON = new Gson();

    @NotNull
    @CheckReturnValue
    public static CompletableFuture<@Nullable Player> playerFromTarget(@NotNull Target target) {
        if (TWITCH_TO_USER_MAP.containsKey(target.getId())) {
            TARGET_LOGGER.finer("Using cached UUID for " + target.getId());
            Player player = Bukkit.getPlayer(TWITCH_TO_USER_MAP.get(target.getId()));
            if (player == null) {
                final String warning = "Streamer %s (%s) is accepting effects but is offline".formatted(target.getName(), target.getId());
                TARGET_LOGGER.warning(warning);
            }
            return CompletableFuture.completedFuture(player);
        }

        try {
            CompletableFuture<Player> future = new CompletableFuture<>();
            URL url = new URL("https://api.crowdcontrol.live/minecraft/twitch/" + target.getId());
            TARGET_EXECUTOR.execute(() -> {
                try {
                    URLConnection conn = url.openConnection();
                    conn.connect();
                    InputStreamReader input = new InputStreamReader(conn.getInputStream());
                    UUID uuid = GSON.fromJson(input, APIResponse.class).getUUID();
                    input.close();
                    if (uuid != null) {
                        TWITCH_TO_USER_MAP.put(target.getId(), uuid);
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null || player.isDead() || !player.isValid() || (player.getGameMode() == GameMode.SPECTATOR && !TimedEffect.isActive("gamemode", target)))
                            future.complete(null);
                        else
                            future.complete(player);
                    } else {
                        TARGET_LOGGER.fine("Couldn't read results from API (no parsable response)");
                        future.complete(null);
                    }
                } catch (IOException ioExc) {
                    TARGET_LOGGER.log(Level.FINE, "Couldn't fetch results from API", ioExc);
                    future.complete(null);
                } catch (JsonParseException jsonExc) {
                    TARGET_LOGGER.log(Level.WARNING, "Couldn't read results from API", jsonExc);
                    future.complete(null);
                }
            });
            return future;
        } catch (MalformedURLException exc) {
            TARGET_LOGGER.log(Level.SEVERE, "Failed to parse URL", exc);
            return CompletableFuture.completedFuture(null);
        }
    }

    private static final class APIResponse {
        @SerializedName("minecraftID")
        private String uuid;

        @Nullable
        public UUID getUUID() {
            if (uuid == null) return null;
            // convert to standard UUID format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < uuid.length(); i++) {
                sb.append(uuid.charAt(i));
                if (i == 7 || i == 11 || i == 15 || i == 19)
                    sb.append('-');
            }
            return UUID.fromString(sb.toString());
        }
    }

    public void registerCommand(@NotNull String name, @NotNull Command command) {
        name = name.toLowerCase(Locale.ENGLISH);
        crowdControl.registerHandler(name, command::executeAndNotify);
        getLogger().fine("Registered CC command '"+name+"'");
    }

    @Nullable
    public CrowdControl getCrowdControl() {
        return crowdControl;
    }
}
