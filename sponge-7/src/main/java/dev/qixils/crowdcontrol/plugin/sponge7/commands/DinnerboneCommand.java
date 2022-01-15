package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.Command;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.data.entity.OriginalDisplayNameData;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_NAME;
import static dev.qixils.crowdcontrol.common.CommandConstants.DINNERBONE_RADIUS;
import static dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin.VIEWER_SPAWNED;

@Getter
public class DinnerboneCommand extends Command {
	private final String effectName = "dinnerbone";
	private final String displayName = "Flip Mobs Upside-Down";

	public DinnerboneCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public @NotNull CompletableFuture<Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		CompletableFuture<Boolean> successFuture = new CompletableFuture<>();
		sync(() -> {
			Set<Entity> entities = new HashSet<>();
			for (Player player : players) {
				List<Entity> toAdd = new ArrayList<>(player.getWorld().getNearbyEntities(player.getPosition(), DINNERBONE_RADIUS));
				toAdd.removeIf(entity -> entity.getType().equals(EntityTypes.PLAYER));
				entities.addAll(toAdd);
			}
			successFuture.complete(!entities.isEmpty());
			entities.forEach(entity -> {
				Text oldName = entity.get(OriginalDisplayNameData.class).map(data -> data.originalDisplayName().get()).orElse(Text.EMPTY);
				Text currentName = entity.getOrElse(Keys.DISPLAY_NAME, Text.EMPTY);
				if (currentName.equals(Text.of(DINNERBONE_NAME))) {
					entity.offer(Keys.DISPLAY_NAME, oldName);
					entity.remove(OriginalDisplayNameData.class);
					if (entity.getOrElse(VIEWER_SPAWNED, false))
						entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
				} else {
					entity.offer(new OriginalDisplayNameData(currentName));
					entity.offer(Keys.DISPLAY_NAME, Text.of(DINNERBONE_NAME));
					entity.offer(Keys.CUSTOM_NAME_VISIBLE, false);
				}
			});
		});
		return successFuture.thenApply(success -> success
				? request.buildResponse().type(ResultType.SUCCESS)
				: request.buildResponse().type(ResultType.RETRY).message("No nearby entities"));
	}
}
