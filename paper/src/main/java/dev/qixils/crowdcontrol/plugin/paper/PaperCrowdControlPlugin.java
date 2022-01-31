package dev.qixils.crowdcontrol.plugin.paper;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import dev.qixils.crowdcontrol.CrowdControl;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.TextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class PaperCrowdControlPlugin extends JavaPlugin implements Listener, Plugin<Player, CommandSender> {
	private static final Map<String, Boolean> VALID_SOUNDS = new HashMap<>();
	public static final PersistentDataType<Byte, Boolean> BOOLEAN_TYPE = new BooleanDataType();
	public static final PersistentDataType<String, Component> COMPONENT_TYPE = new ComponentDataType();
	@Getter
	private final PaperPlayerMapper playerMapper = new PaperPlayerMapper(this);
	@SuppressWarnings("deprecation") // ComponentFlattenerProvider has not been implemented yet
	@Getter
	private final TextUtil textUtil = new TextUtil(Bukkit.getUnsafe().componentFlattener());
	@Getter
	private final Class<Player> playerClass = Player.class;
	@Getter
	private final Class<CommandSender> commandSenderClass = CommandSender.class;
	FileConfiguration config = getConfig();
	// actual stuff
	String manualPassword = null; // set via /password
	@Getter
	CrowdControl crowdControl = null;
	List<Command> commands;
	@Getter
	private PaperCommandManager<CommandSender> commandManager;
	@Getter
	private boolean isServer = true;
	@Getter
	private boolean global = false;
	@Getter
	private Collection<String> hosts = Collections.emptyList();
	private boolean announce = true;

	@Override
	public void onLoad() {
		saveDefaultConfig();
		// init sound validator
		CommandConstants.SOUND_VALIDATOR = key -> {
			String asString = key.value();
			Boolean value = VALID_SOUNDS.get(asString);
			if (value != null)
				return value;

			try {
				Sound.valueOf(asString.toUpperCase(Locale.ENGLISH).replace('.', '_'));
				VALID_SOUNDS.put(asString, true);
				return true;
			} catch (IllegalArgumentException e) {
				VALID_SOUNDS.put(asString, false);
				return false;
			}
		};
	}

	public void initCrowdControl() {
		reloadConfig();
		config = getConfig();
		int port = config.getInt("port", DEFAULT_PORT);

		if (!config.getBoolean("legacy", false)) {
			isServer = true;
			String password = Objects.requireNonNullElseGet(manualPassword, () -> config.getString("password", ""));
			if (!password.isBlank()) {
				getLogger().info("Running Crowd Control in server mode");
				crowdControl = CrowdControl.server().port(port).password(password).build();
			} else {
				getLogger().severe("No password has been set in the plugin's config file. Please set one by editing plugins/CrowdControl/config.yml or set a temporary password using the /password command.");
				return;
			}
		} else {
			isServer = false;
			String ip = config.getString("ip", "127.0.0.1");
			if (ip.isBlank())
				throw new IllegalStateException("IP address is blank. Please fix this in the plugins/CrowdControl/config.yml file.");
			getLogger().info("Running Crowd Control in client mode");
			crowdControl = CrowdControl.client().port(port).ip(ip).build();
		}

		if (commands == null)
			commands = RegisterCommands.register(this);
		else
			RegisterCommands.register(this, commands);
	}

	@Override
	public void updateCrowdControl(@Nullable CrowdControl crowdControl) {
		this.crowdControl = crowdControl;
	}

	@SneakyThrows
	@Override
	public void onEnable() {
		global = config.getBoolean("global", false);
		announce = config.getBoolean("announce", true);
		hosts = Collections.unmodifiableCollection(config.getStringList("hosts"));
		if (!hosts.isEmpty()) {
			Set<String> loweredHosts = new HashSet<>(hosts.size());
			for (String host : hosts)
				loweredHosts.add(host.toLowerCase(Locale.ENGLISH));
			hosts = Collections.unmodifiableSet(loweredHosts);
		}

		initCrowdControl();

		Bukkit.getPluginManager().registerEvents(this, this);

		try {
			commandManager = new PaperCommandManager<>(this,
					AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder()
							.withAsynchronousParsing().build(),
					Function.identity(),
					Function.identity()
			);
			commandManager.registerBrigadier();
			commandManager.registerAsynchronousCompletions();
			registerChatCommands();
		} catch (Exception exception) {
			throw new IllegalStateException("The command manager was unable to load. Please ensure you are using the latest version of Paper.", exception);
		}
	}

	@Override
	public @NotNull Logger getSLF4JLogger() {
		return super.getSLF4JLogger();
	}

	@Override
	public void onDisable() {
		if (crowdControl != null) {
			crowdControl.shutdown("Plugin is unloading (server may be shutting down)");
			crowdControl = null;
		}
		commands = null;
	}

	public boolean announceEffects() {
		return announce;
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getAllPlayers() {
		return playerMapper.getAllPlayers();
	}

	@CheckReturnValue
	@NotNull
	public List<@NotNull Player> getPlayers(final @NotNull Request request) {
		return playerMapper.getPlayers(request);
	}

	@Override
	public void registerCommand(@NotNull String name, dev.qixils.crowdcontrol.common.@NotNull Command<Player> command) {
		name = name.toLowerCase(Locale.ENGLISH);
		crowdControl.registerHandler(name, command::executeAndNotify);
		getLogger().fine("Registered CC command '" + name + "'");
	}

	@Override
	public @Nullable String getPassword() {
		if (!isServer()) return null;
		if (crowdControl != null)
			return crowdControl.getPassword(); // should be non-null because isServer is true
		if (manualPassword != null)
			return manualPassword;
		return config.getString("password");
	}

	@Override
	public void setPassword(@NotNull String password) throws IllegalArgumentException, IllegalStateException {
		if (!isServer())
			throw new IllegalStateException("Not running in server mode");
		manualPassword = password;
	}

	@Override
	public boolean isAdmin(@NotNull CommandSender commandSource) {
		return commandSource.hasPermission(ADMIN_PERMISSION) || commandSource.isOp();
	}

	@Override
	public @NotNull Optional<UUID> getUUID(@NotNull CommandSender entity) {
		if (entity instanceof Entity trueEntity)
			return Optional.of(trueEntity.getUniqueId());
		return Plugin.super.getUUID(entity);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		onPlayerJoin(event.getPlayer());
	}

	// boilerplate stuff for the data container storage

	private static final class BooleanDataType implements PersistentDataType<Byte, Boolean> {
		private static final byte TRUE = 1;
		private static final byte FALSE = 0;

		@NotNull
		public Class<Byte> getPrimitiveType() {
			return Byte.class;
		}

		@NotNull
		public Class<Boolean> getComplexType() {
			return Boolean.class;
		}

		@NotNull
		public Byte toPrimitive(@NotNull Boolean complex, @NotNull PersistentDataAdapterContext context) {
			return complex ? TRUE : FALSE;
		}

		@NotNull
		public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull PersistentDataAdapterContext context) {
			return primitive != FALSE;
		}
	}

	private static final class ComponentDataType implements PersistentDataType<String, Component> {
		private final GsonComponentSerializer serializer = GsonComponentSerializer.gson();

		@Override
		public @NotNull Class<String> getPrimitiveType() {
			return String.class;
		}

		@Override
		public @NotNull Class<Component> getComplexType() {
			return Component.class;
		}

		@Override
		public @NotNull String toPrimitive(@NotNull Component complex, @NotNull PersistentDataAdapterContext context) {
			return serializer.serialize(complex);
		}

		@Override
		public @NotNull Component fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
			return serializer.deserialize(primitive);
		}
	}
}
