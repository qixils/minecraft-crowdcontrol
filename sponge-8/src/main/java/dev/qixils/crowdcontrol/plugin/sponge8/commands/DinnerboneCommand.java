package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.Command;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_RADIUS;
import static dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin.VIEWER_SPAWNED;

@Getter
public class DinnerboneCommand extends Command {
	private final String effectName = "dinnerbone";
	private final String displayName = "Flip Mobs Upside-Down";

	public DinnerboneCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			Set<Entity> entities = new HashSet<>();
			for (Player player : players) {
				List<Entity> toAdd = new ArrayList<>(player.world().nearbyEntities(player.position(), DINNERBONE_RADIUS));
				toAdd.removeIf(entity -> entity.type().equals(EntityTypes.PLAYER.get()));
				entities.addAll(toAdd);
			}
			successFuture.complete(!entities.isEmpty());
			GsonComponentSerializer serializer = plugin.getSerializer();
			entities.forEach(entity -> {
				Component oldName = entity.get(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME)
						.map(serializer::deserialize)
						.orElseGet(Component::empty);
				Component currentName = entity.getOrElse(Keys.CUSTOM_NAME, Component.empty());
				if (currentName.equals(Component.text(DINNERBONE_NAME))) {
					entity.offer(Keys.CUSTOM_NAME, oldName);
					entity.remove(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME);
					if (entity.getOrElse(VIEWER_SPAWNED, false))
						entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, true);
				} else {
					entity.offer(SpongeCrowdControlPlugin.ORIGINAL_DISPLAY_NAME, serializer.serialize(currentName));
					entity.offer(Keys.CUSTOM_NAME, Component.text(DINNERBONE_NAME));
					entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, false);
				}
			});
		});
		return successFuture.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
	}
}
