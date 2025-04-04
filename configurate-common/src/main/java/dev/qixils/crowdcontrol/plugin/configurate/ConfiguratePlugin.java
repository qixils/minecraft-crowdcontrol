package dev.qixils.crowdcontrol.plugin.configurate;

import dev.qixils.crowdcontrol.common.HideNames;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.SoftLockConfig;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static dev.qixils.crowdcontrol.common.SoftLockConfig.*;

public abstract class ConfiguratePlugin<P, S> extends Plugin<P, S> {

	protected @NotNull Path dataFolder = Paths.get("config", "CrowdControlData");

	public ConfiguratePlugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
		super(playerClass, commandSenderClass);
	}

	protected abstract ConfigurationLoader<?> getConfigLoader() throws IllegalStateException;

	public void loadConfig() {
		ConfigurationNode config;
		try {
			config = getConfigLoader().load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		// soft-lock observer
		softLockConfig = new SoftLockConfig(
			config.node("soft-lock-observer.period").getInt(DEF_PERIOD),
			config.node("soft-lock-observer.deaths").getInt(DEF_DEATHS),
			config.node("soft-lock-observer.search-horizontal").getInt(DEF_SEARCH_HORIZ),
			config.node("soft-lock-observer.search-vertical").getInt(DEF_SEARCH_VERT)
		);

		// hosts
		TypeToken<Set<String>> hostToken = new TypeToken<Set<String>>() {};
		try {
			hosts = Collections.unmodifiableSet(config.node("hosts").get(hostToken, new HashSet<>(hosts)));
		} catch (SerializationException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}

		// limit config
		boolean hostsBypass = config.node("limits", "hosts-bypass").getBoolean(limitConfig.hostsBypass());
		TypeToken<Map<String, Integer>> limitToken = new TypeToken<Map<String, Integer>>() {};
		try {
			Map<String, Integer> itemLimits = config.node("limits", "items").get(limitToken, limitConfig.itemLimits());
			Map<String, Integer> entityLimits = config.node("limits", "entities").get(limitToken, limitConfig.entityLimits());
			limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);
		} catch (SerializationException e) {
			getSLF4JLogger().warn("Could not parse limits config", e);
		}

		// misc
		global = config.node("global").getBoolean(global);
		announce = config.node("announce").getBoolean(announce);
		hideNames = HideNames.fromConfigCode(config.node("hide-names").getString(hideNames.getConfigCode()));
	}

	public void saveConfig() {
		try {
			// TODO: add comments
			ConfigurationNode config = getConfigLoader().createNode();
			TypeToken<Set<String>> hostToken = new TypeToken<Set<String>>() {};
			TypeToken<Map<String, Integer>> limitToken = new TypeToken<Map<String, Integer>>() {};
			config.node("hosts").set(hostToken, new HashSet<>(hosts));
			config.node("limits", "hosts-bypass").set(limitConfig.hostsBypass());
			config.node("limits", "items").set(limitToken, limitConfig.itemLimits());
			config.node("limits", "entities").set(limitToken, limitConfig.entityLimits());
			config.node("global").set(global);
			config.node("announce").set(announce);
			config.node("hide-names").set(hideNames.getConfigCode());
			getConfigLoader().save(config);
		} catch (ConfigurateException e) {
			throw new RuntimeException("Could not save plugin config", e);
		}
	}

	protected @NotNull Path fixConfigDirectory(@NotNull Path configDirectory) {
		if (configDirectory.getFileName().toString().equals("crowdcontrol.conf"))
			configDirectory = configDirectory.getParent();
		return configDirectory;
	}

	/**
	 * Creates a config loader given the directory in which plugin config files are stored.
	 *
	 * @param configDirectory path in which plugin config files are stored
	 * @return the loader for a config file
	 * @throws IllegalStateException copying the default config file failed
	 */
	@CheckReturnValue
	protected HoconConfigurationLoader createConfigLoader(@NotNull Path configDirectory) throws IllegalStateException {
		configDirectory = fixConfigDirectory(configDirectory);
		dataFolder = configDirectory.resolve("CrowdControlData");

		if (!Files.exists(configDirectory)) {
			try {
				Files.createDirectories(configDirectory);
			} catch (Exception e) {
				throw new IllegalStateException("Could not create config directory", e);
			}
		}

		// move old config
		Path configPath = configDirectory.resolve("crowdcontrol.conf");
		Path oldConfigPath = configDirectory.resolve("crowd-control.conf");
		if (Files.exists(oldConfigPath)) {
			try {
				Files.move(oldConfigPath, configPath);
			} catch (Exception e) {
				getSLF4JLogger().warn("Could not move old config file to new location", e);
			}
		}

		if (!Files.exists(configPath)) {
			// read the default config
			InputStream inputStream = getInputStream("assets/crowdcontrol/default.conf");
			if (inputStream == null)
				throw new IllegalStateException("Could not find default config file");
			// copy the default config to the config path
			try {
				Files.copy(inputStream, configPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not copy default config file to " + configPath, e);
			}
		}

		return HoconConfigurationLoader.builder().path(configPath).build();
	}

	@Override
	public @NotNull Path getDataFolder() {
		return dataFolder;
	}
}
