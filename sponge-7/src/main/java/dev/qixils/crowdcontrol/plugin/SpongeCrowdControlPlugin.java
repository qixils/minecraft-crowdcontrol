package dev.qixils.crowdcontrol.plugin;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.sponge7.SpongeCommandManager;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.AbstractPlugin;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.plugin.utils.Sponge7TextUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.spongeapi.SpongeAudiences;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.asset.AssetId;
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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

@Plugin(
		id = "crowd-control",
		name = "Crowd Control",
		version = "${project.version}",
		description = "Allows viewers to interact with your Minecraft world",
		url = "https://github.com/qixils/minecraft-crowdcontrol",
		authors = {"qixils"}
)
@Getter
public class SpongeCrowdControlPlugin extends AbstractPlugin<Player, CommandSource> {
	private final TextUtil textUtil = new Sponge7TextUtil();
	private final SpongePlayerMapper playerMapper = new SpongePlayerMapper(this);
	private SpongeCommandManager<CommandSource> commandManager;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private List<Command> commands;
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
	@AssetId("default.conf")
	private Asset defaultConfig;
	@Inject
	@DefaultConfig(sharedRoot = true)
	private Path configPath;
	@Inject
	private SpongeAudiences audiences;

	public SpongeCrowdControlPlugin() {
		super(Player.class, CommandSource.class);
	}

	@Override
	public Audience asAudience(@NotNull CommandSource source) {
		if (source instanceof Player)
			return audiences.player((Player) source);
		return audiences.receiver(source);
	}

	@Override
	public boolean isAdmin(@NotNull CommandSource commandSource) {
		return commandSource.hasPermission(ADMIN_PERMISSION); // TODO: operator check
	}

	@Override
	public @Nullable String getPassword() {
		if (!isServer()) return null;
		if (crowdControl != null)
			return crowdControl.getPassword();
		if (manualPassword != null)
			return manualPassword;
		try {
			return configLoader.load().getNode("password").getString();
		} catch (IOException e) {
			logger.warn("Could not load config", e);
			return null;
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void initCrowdControl() {
		ConfigurationNode config;
		try {
			config = configLoader.load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		try {
			hosts = Collections.unmodifiableCollection(config.getNode("hosts").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}

		global = config.getNode("global").getBoolean(false);
		announce = config.getNode("announce").getBoolean(true);
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}
		isServer = !config.getNode("legacy").getBoolean(false);
		if (isServer) {
			getLogger().info("Running Crowd Control in server mode");
			String password;
			if (manualPassword != null)
				password = manualPassword;
			else {
				password = config.getNode("password").getString();
				if (password == null || password.isEmpty()) {
					logger.error("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command."); // TODO: update config file path
					return;
				}
			}
			crowdControl = CrowdControl.server().port(PORT).password(password).build();
		} else {
			getLogger().info("Running Crowd Control in legacy client mode");
			String ip = config.getNode("ip").getString();
			if (ip == null || ip.isEmpty()) {
				logger.error("No IP address has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml"); // TODO config path
				return;
			}
			crowdControl = CrowdControl.client().port(PORT).ip(ip).build();
		}

		if (commands == null)
			commands = RegisterCommands.register(this);
		else
			RegisterCommands.register(this, commands);
	}

	@SneakyThrows(IOException.class)
	@Listener
	public void onServerStart(GameStartedServerEvent event) {
		defaultConfig.copyToFile(configPath, false, true);
		configLoader = HoconConfigurationLoader.builder()
				.setPath(configPath)
				.build();
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
		if (crowdControl != null) {
			crowdControl.shutdown("Minecraft server is shutting down");
			crowdControl = null;
		}
		commands = null;
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return logger;
	}
}
