package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.AbstractPlayerManager;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.AllArgsConstructor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class FabricPlayerManager extends AbstractPlayerManager<ServerPlayerEntity> {
	private final FabricCrowdControlPlugin plugin;

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getPlayers(@NotNull Request request) {
		// TODO
	}

	@Override
	public @NotNull List<@NotNull ServerPlayerEntity> getAllPlayers() {
		return plugin.getServer().getPlayerManager().getPlayerList();
	}
}
