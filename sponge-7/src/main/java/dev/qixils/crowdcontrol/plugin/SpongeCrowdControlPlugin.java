package dev.qixils.crowdcontrol.plugin;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge7.SpongeCommandManager;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

@Plugin(
		id = "minecraft-crowd-control",
		name = "Minecraft Crowd Control",
		version = "${project.version}",
		description = "Allows viewers to interact with your Minecraft world",
		url = "https://github.com/qixils/minecraft-crowdcontrol",
		authors = {"qixils"}
)
@Getter
public class SpongeCrowdControlPlugin implements dev.qixils.crowdcontrol.common.Plugin<Player, CommandSource> {
	private final TextUtil textUtil = new Sponge7TextUtil();
	private final SpongePlayerMapper playerMapper = new SpongePlayerMapper(this);
	private final Class<Player> playerClass = Player.class;
	private final Class<CommandSource> commandSenderClass = CommandSource.class;
	// injected variables
	@Inject
	private Logger logger;
	@Inject
	private PluginContainer pluginContainer;
	@Inject
	@SynchronousExecutor
	private SpongeExecutorService syncExecutor;
	@Inject
	@AsynchronousExecutor
	private SpongeExecutorService asyncExecutor;
	@Inject
	private Game game;
	@Inject
	@DefaultConfig() // TODO: set this up
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	// "actual" variables
	private CrowdControl crowdControl;
	private SpongeCommandManager<CommandSource> commandManager;
	private boolean global;
	@Accessors(fluent = true)
	private boolean announceEffects;
	private Collection<String> hosts = Collections.emptySet();
	private boolean isServer;
	private String manualPassword;

	@SuppressWarnings("UnstableApiUsage")
	@SneakyThrows
	@Override
	public void initCrowdControl() {
		ConfigurationNode config = configLoader.load();
		global = config.getNode("global").getBoolean(false);
		announceEffects = config.getNode("announce").getBoolean(true);
		hosts = Collections.unmodifiableCollection(config.getNode("hosts").getList(TypeToken.of(String.class)));
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}
		isServer = !config.getNode("legacy").getBoolean(false);
		if (isServer) {
			String password;
			if (manualPassword != null)
				password = manualPassword;
			else {
				password = config.getNode("password").getString();
				if (password == null) {

					return;
				}
			}
		} else {
			String ip = config.getNode("ip").getString("localhost");
			if (ip.isEmpty())
				throw new IllegalStateException("IP address is blank. Please fix this in the plugin's config file.");
		}
		// TODO
	}

	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		initCrowdControl();
		commandManager = new SpongeCommandManager<>(
				pluginContainer,
				AsynchronousCommandExecutionCoordinator.<CommandSource>newBuilder()
						.withAsynchronousParsing().withExecutor(asyncExecutor).build(),
				Function.identity(),
				Function.identity()
		);
	}

	@Listener
	public void onServerStop(GameStoppingServerEvent event) {

	}

	@Override
	public boolean isAdmin(@NotNull CommandSource commandSource) {
		return commandSource.hasPermission(ADMIN_PERMISSION); // TODO: operator check
	}
}
