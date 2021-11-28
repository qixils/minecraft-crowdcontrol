package dev.qixils.crowdcontrol.plugin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import dev.qixils.crowdcontrol.exceptions.NoApplicableTarget;
import dev.qixils.crowdcontrol.plugin.commands.GamemodeCommand;
import dev.qixils.crowdcontrol.plugin.utils.TextBuilder;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Request.Target;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class PlayerMapper implements Listener {
    private static final Gson GSON = new Gson();
    private static final Component SUCCESS_MESSAGE =
            new TextBuilder("Successfully connected to your Twitch account.", NamedTextColor.GREEN)
                    .build();
    private static final Component FAILURE_MESSAGE =
            new TextBuilder("Your Twitch account could not be identified. Please ensure you have linked your Minecraft account in the Crowd Control app.", NamedTextColor.RED)
                    .build();
    private final CrowdControlPlugin plugin;
    private final Logger logger = Logger.getLogger("MC-CC-PlayerMapper");
    private final BiMap<Integer, UUID> twitchToUserMap = HashBiMap.create();
    private final Executor webRequestExecutor = Executors.newCachedThreadPool();

    @Contract(value = "_ -> param1", mutates = "param1")
    private @NotNull List<@NotNull Player> filter(@NotNull List<@Nullable Player> players) {
        players.removeIf(player -> player == null
                || !player.isValid()
                || player.isDead()
                || (player.getGameMode() == GameMode.SPECTATOR && !GamemodeCommand.isEffectActive(plugin, player))
        );
        // above statement removes null entries
        //noinspection NullableProblems
        return players;
    }

    @CheckReturnValue
    @NotNull
    CompletableFuture<@NotNull List<@NotNull Player>> getPlayers(final @NotNull Request request) {
        if (plugin.isGlobal(request))
            return CompletableFuture.completedFuture(filter(new ArrayList<>(Bukkit.getOnlinePlayers())));

        Target[] targets = request.getTargets();
        List<CompletableFuture<Player>> futures = new ArrayList<>(targets.length);
        for (Target target : targets)
            futures.add(playerFromTarget(target));

        CompletableFuture<List<Player>> playersFuture = new CompletableFuture<>();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenAccept($ -> {
            List<Player> players = new ArrayList<>(futures.size());
            for (CompletableFuture<Player> future : futures) {
                try {
                    players.add(future.get());
                } catch (CancellationException | ExecutionException | InterruptedException ignored) {
                }
            }

            filter(players);
            if (players.isEmpty())
                playersFuture.completeExceptionally(new NoApplicableTarget());
            else
                playersFuture.complete(players);
        });
        return playersFuture;
    }

    @NotNull
    @CheckReturnValue
    private CompletableFuture<@Nullable Player> playerFromTarget(@NotNull Target target) {
        if (twitchToUserMap.containsKey(target.getId())) {
            logger.finest("Using cached UUID for " + target.getId());
            Player player = Bukkit.getPlayer(twitchToUserMap.get(target.getId()));
            if (player == null) {
                final String warning = "Streamer %s (%s) is accepting effects but is offline".formatted(target.getName(), target.getId());
                logger.warning(warning);
            }
            return CompletableFuture.completedFuture(player);
        }

        return getJsonFromURL("https://api.crowdcontrol.live/minecraft/twitch/" + target.getId(), MinecraftIdResponse.class)
                .thenApply(response -> {
                    if (response == null) return null;
                    UUID uuid = response.getUUID();
                    if (uuid == null) return null;
                    twitchToUserMap.put(target.getId(), uuid);
                    return Bukkit.getPlayer(uuid);
                });
    }

    private <T> @NotNull CompletableFuture<@Nullable T> getJsonFromURL(@NotNull URL url, Class<T> tClass) {
        CompletableFuture<T> future = new CompletableFuture<>();
        webRequestExecutor.execute(() -> {
            try {
                URLConnection conn = url.openConnection();
                conn.connect();

                InputStreamReader input = new InputStreamReader(conn.getInputStream());
                T object = GSON.fromJson(input, tClass);
                input.close();

                if (object == null)
                    logger.fine("Couldn't read results from API (no parsable response)");

                future.complete(object);
            } catch (IOException ioExc) {
                logger.log(Level.FINE, "Couldn't fetch results from API", ioExc);
                future.complete(null);
            } catch (JsonParseException jsonExc) {
                logger.log(Level.SEVERE, "Couldn't read results from API", jsonExc);
                future.complete(null);
            }
        });
        return future;
    }

    private <T> @NotNull CompletableFuture<@Nullable T> getJsonFromURL(@NotNull String url, Class<T> tClass) {
        try {
            return getJsonFromURL(new URL(url), tClass);
        } catch (MalformedURLException exception) {
            logger.log(Level.SEVERE, "Failed to parse URL", exception);
            return CompletableFuture.completedFuture(null);
        }
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        String username = event.getPlayer().getName();
        UUID uuid = event.getPlayer().getUniqueId();
        if (twitchToUserMap.containsValue(uuid)) return;

        String uuidStr = uuid.toString().toLowerCase(Locale.ENGLISH).replace("-", "");
        getJsonFromURL("https://api.crowdcontrol.live/minecraft/uuid/" + uuidStr, TwitchIdResponse.class)
                .thenAccept(response -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (response == null || response.twitchID <= 0) {
                        if (plugin.isServer()) {
                            logger.warning("Could not identify " + username + "'s Twitch account (" + uuidStr + ")");
                            if (player != null)
                                player.sendMessage(FAILURE_MESSAGE);
                        }
                    } else {
                        twitchToUserMap.put(response.twitchID, uuid);
                        if (player != null)
                            player.sendMessage(SUCCESS_MESSAGE);
                    }
                });
    }

    private static final class MinecraftIdResponse {
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

    private static final class TwitchIdResponse {
        public int twitchID;
    }
}
