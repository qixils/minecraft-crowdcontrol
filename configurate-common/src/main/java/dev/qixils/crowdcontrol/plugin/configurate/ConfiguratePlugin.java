package dev.qixils.crowdcontrol.plugin.configurate;

import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.exceptions.ExceptionUtil;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class ConfiguratePlugin<P, S> extends dev.qixils.crowdcontrol.common.AbstractPlugin<P, S> {

	public ConfiguratePlugin(@NotNull Class<P> playerClass, @NotNull Class<S> commandSenderClass) {
		super(playerClass, commandSenderClass);
	}

	protected abstract ConfigurationLoader<?> getConfigLoader() throws IllegalStateException;

	@Override
	public @Nullable String getPassword() {
		if (!isServer()) return null;
		if (crowdControl != null)
			return crowdControl.getPassword();
		if (manualPassword != null)
			return manualPassword;
		try {
			return getConfigLoader().load().node("password").getString();
		} catch (IOException e) {
			getSLF4JLogger().warn("Could not load config", e);
			return null;
		}
	}

	@Override
	public void initCrowdControl() {
		ConfigurationNode config;
		try {
			config = getConfigLoader().load();
		} catch (IOException e) {
			throw new RuntimeException("Could not load plugin config", e);
		}

		try {
			hosts = Collections.unmodifiableCollection(ExceptionUtil.validateNotNullElseGet(
					config.node("hosts").getList(String.class),
					Collections::emptyList
			));
		} catch (SerializationException e) {
			throw new RuntimeException("Could not parse 'hosts' config variable", e);
		}

		// limit config
		boolean hostsBypass = config.node("limits", "hosts-bypass").getBoolean(true);
		TypeToken<Map<String, Integer>> typeToken = new TypeToken<Map<String, Integer>>() {};
		try {
			Map<String, Integer> itemLimits = config.node("limits", "items").get(typeToken);
			Map<String, Integer> entityLimits = config.node("limits", "entities").get(typeToken);
			limitConfig = new LimitConfig(hostsBypass, itemLimits, entityLimits);
		} catch (SerializationException e) {
			getSLF4JLogger().warn("Could not parse limits config", e);
		}

		global = config.node("global").getBoolean(false);
		announce = config.node("announce").getBoolean(true);
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ROOT));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}
		isServer = !config.node("legacy").getBoolean(false);
		int port = config.node("port").getInt(DEFAULT_PORT);
		if (isServer) {
			getSLF4JLogger().info("Running Crowd Control in server mode");
			String password;
			if (manualPassword != null)
				password = manualPassword;
			else {
				password = config.node("password").getString("crowdcontrol");
				if (password == null || password.isEmpty()) {
					getSLF4JLogger().error("No password has been set in the plugin's config file. Please set one by editing config/crowd-control.conf or set a temporary password using the /password command.");
					return;
				}
			}
			crowdControl = CrowdControl.server().port(port).password(password).build();
		} else {
			getSLF4JLogger().info("Running Crowd Control in legacy client mode");
			String ip = config.node("ip").getString("127.0.0.1");
			if (ip == null || ip.isEmpty()) {
				getSLF4JLogger().error("No IP address has been set in the plugin's config file. Please set one by editing config/crowd-control.conf");
				return;
			}
			crowdControl = CrowdControl.client().port(port).ip(ip).build();
		}

		commandRegister().register();
		postInitCrowdControl(crowdControl);
	}

	/**
	 * Creates a config loader given the directory in which plugin config files are stored.
	 *
	 * @param configDirectory path in which plugin config files are stored
	 * @return the loader for a config file
	 * @throws IllegalStateException copying the default config file failed
	 */
	@CheckReturnValue
	protected HoconConfigurationLoader createConfigLoader(@NotNull File configDirectory) throws IllegalStateException {
		if (!configDirectory.exists()) {
			//noinspection ResultOfMethodCallIgnored
			configDirectory.mkdir();
		}

		File configFile = new File(configDirectory, "crowd-control.conf");
		Path configPath = configFile.toPath();
		if (!configFile.exists()) {
			// read the default config
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("default.conf");
			if (inputStream == null)
				throw new IllegalStateException("Could not find default config file; please report to qixils");
			// copy the default config to the config path
			try {
				Files.copy(inputStream, configPath);
			} catch (IOException e) {
				throw new IllegalStateException("Could not copy default config file to " + configPath, e);
			}
		}

		return HoconConfigurationLoader.builder().file(configFile).build();
	}
}
