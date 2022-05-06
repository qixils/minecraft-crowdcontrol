package dev.qixils.crowdcontrol.plugin.fabric;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.plugin.fabric.client.FabricPlatformClient;
import dev.qixils.crowdcontrol.plugin.fabric.utils.FabricWrappedAudienceProvider;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.qixils.crowdcontrol.exceptions.ExceptionUtil.validateNotNullElseGet;

@Getter
public final class FabricCrowdControlPlugin extends MojmapPlugin<FabricServerAudiences> implements ModInitializer {
	public static boolean CLIENT_INITIALIZED = false;
	public static boolean CLIENT_AVAILABLE = false;
	@Accessors(fluent = true)
	private final EntityMapper<CommandSourceStack> commandSenderMapper = new CommandSourceStackMapper(this);
	private final FabricServerCommandManager<CommandSourceStack> commandManager
			= FabricServerCommandManager.createNative(AsynchronousCommandExecutionCoordinator.<CommandSourceStack>newBuilder()
				.withAsynchronousParsing()
				.withExecutor(getAsyncExecutor())
				.build()
	);

	public FabricCrowdControlPlugin() {
	}

	@Override
	public boolean isClientAvailable(@Nullable List<ServerPlayer> possiblePlayers, @NotNull Request request) {
		if (!CLIENT_INITIALIZED)
			return false;
		final List<ServerPlayer> players = validateNotNullElseGet(possiblePlayers, () -> getPlayers(request));
		if (players.size() != 1)
			return false;
		return FabricPlatformClient.get().player()
				.map(player -> player.getUUID().equals(players.get(0).getUUID()))
				.orElse(false);
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(this::setServer);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> setServer(null));
		// TODO render join message
	}

	// boilerplate

	@Override
	protected @NotNull FabricWrappedAudienceProvider initAdventure(@NotNull MinecraftServer server) {
		return new FabricWrappedAudienceProvider(FabricServerAudiences.of(server));
	}
}
