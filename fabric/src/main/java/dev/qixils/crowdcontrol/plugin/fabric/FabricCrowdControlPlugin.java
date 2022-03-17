package dev.qixils.crowdcontrol.plugin.fabric;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import dev.qixils.crowdcontrol.common.EntityMapper;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.mojmap.MojmapPlugin;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

@Getter
public final class FabricCrowdControlPlugin extends MojmapPlugin implements ModInitializer {
	public static boolean CLIENT_INITIALIZED = false;
	public static boolean CLIENT_AVAILABLE = false;
	private static final TextUtil EMPTY_TEXT_UTIL = new TextUtil(null);
	// platform stuff
	private final @NotNull TextUtil textUtil = EMPTY_TEXT_UTIL;
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
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTING.register(this::setServer);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> setServer(null));
		// TODO render join message
	}

	// boilerplate

	public @NotNull FabricServerAudiences adventure() {
		return (FabricServerAudiences) super.adventure();
	}

	@Override
	protected @NotNull AudienceProvider initAdventure(@NotNull MinecraftServer server) {
		return FabricServerAudiences.of(server);
	}
}
