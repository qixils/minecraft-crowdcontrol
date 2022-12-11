package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.command.impl.HalfHealthCommand;
import dev.qixils.crowdcontrol.common.command.impl.food.FoodAddCommand;
import dev.qixils.crowdcontrol.common.command.impl.food.FoodSubCommand;
import dev.qixils.crowdcontrol.common.command.impl.food.FullFeedCommand;
import dev.qixils.crowdcontrol.common.command.impl.food.FullStarveCommand;
import dev.qixils.crowdcontrol.common.command.impl.health.DamageCommand;
import dev.qixils.crowdcontrol.common.command.impl.health.FullHealCommand;
import dev.qixils.crowdcontrol.common.command.impl.health.HealCommand;
import dev.qixils.crowdcontrol.common.command.impl.health.KillCommand;
import dev.qixils.crowdcontrol.common.command.impl.maxhealth.MaxHealthAddCommand;
import dev.qixils.crowdcontrol.common.command.impl.maxhealth.MaxHealthCommand;
import dev.qixils.crowdcontrol.common.command.impl.maxhealth.MaxHealthSubCommand;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages the registry of commands (aka effects).
 *
 * @param <PLAYER> the player class used by your plugin
 * @param <PLUGIN> the plugin class using this registry
 */
public abstract class AbstractCommandRegister<PLAYER, PLUGIN extends Plugin<PLAYER, ?>> {
	protected final @NotNull PLUGIN plugin;
	protected final @NotNull Set<Class<? extends Command<PLAYER>>> registeredCommandClasses = new HashSet<>();
	protected final @NotNull Map<Class<? extends Command<PLAYER>>, Command<PLAYER>> singleCommandInstances = new HashMap<>();
	protected final @NotNull Map<String, Command<PLAYER>> registeredCommandMap = new HashMap<>();
	protected @MonotonicNonNull List<Command<PLAYER>> registeredCommands;

	protected AbstractCommandRegister(@NotNull PLUGIN plugin) {
		this.plugin = plugin;
	}

	protected void createCommands(List<Command<PLAYER>> commands) {
		commands.addAll(Arrays.asList(
				new HalfHealthCommand<>(plugin),
				new MaxHealthSubCommand<>(plugin),
				new MaxHealthAddCommand<>(plugin),
				new MaxHealthCommand<>(plugin, 4), // used in hype trains only
				new KillCommand<>(plugin),
				new DamageCommand<>(plugin),
				new FullHealCommand<>(plugin),
				new HealCommand<>(plugin), // TODO: this might be broken on fabric
				new FoodAddCommand<>(plugin),
				new FoodSubCommand<>(plugin),
				new FullFeedCommand<>(plugin),
				new FullStarveCommand<>(plugin)
		));
	}

	public final List<Command<PLAYER>> getCommands() {
		if (registeredCommands != null)
			return registeredCommands;

		List<Command<PLAYER>> commands = new ArrayList<>();
		createCommands(commands);
		for (Command<PLAYER> command : commands) {
			registeredCommandMap.put(command.getEffectName().toLowerCase(Locale.ENGLISH), command);

			//noinspection unchecked
			Class<Command<PLAYER>> clazz = (Class<Command<PLAYER>>) command.getClass();
			if (registeredCommandClasses.contains(clazz))
				singleCommandInstances.remove(clazz);
			else
				singleCommandInstances.put(clazz, command);
			registeredCommandClasses.add(clazz);
		}

		return registeredCommands = commands;
	}

	protected abstract void onFirstRegistry();

	protected abstract void registerListener(Command<PLAYER> command);

	public final void register() {
		boolean firstRegistry = registeredCommands == null;
		for (Command<PLAYER> command : getCommands()) {
			String name = command.getEffectName().toLowerCase(Locale.ENGLISH);
			plugin.registerCommand(name, command);

			if (firstRegistry && command.isEventListener()) {
				registerListener(command);
			}
		}
		if (firstRegistry)
			onFirstRegistry();
	}

	/**
	 * Gets an instance of a registered command.
	 * Only commands that register a sole instance can be returned by this method.
	 *
	 * @param tClass class of the desired command
	 * @param <T>    type of the desired command
	 * @return the command instance
	 * @throws IllegalArgumentException the requested command has not been registered or has been
	 *                                  registered several times
	 */
	@NotNull
	public final <T extends Command<PLAYER>> T getCommand(@NotNull Class<T> tClass) throws IllegalArgumentException {
		if (!singleCommandInstances.containsKey(tClass))
			throw new IllegalArgumentException("Requested class " + tClass.getName()
					+ " is invalid. Please ensure that only one instance of this command is registered.");
		//noinspection unchecked
		return (T) singleCommandInstances.get(tClass);
	}

	/**
	 * Fetches a command by the given effect name.
	 *
	 * @param name effect name of a command
	 * @return the requested command
	 * @throws IllegalArgumentException the requested command does not exist
	 */
	@NotNull
	public final Command<PLAYER> getCommandByName(@NotNull String name) throws IllegalArgumentException {
		name = name.toLowerCase(Locale.ENGLISH);
		if (!registeredCommandMap.containsKey(name))
			throw new IllegalArgumentException("Could not find a command by the name of " + name);
		return registeredCommandMap.get(name);
	}

	/**
	 * Fetches a command by the given effect name and expected command type.
	 *
	 * @param name          effect name of a command
	 * @param expectedClass class of the expected command type
	 * @param <T>           expected command type
	 * @return the requested command
	 * @throws IllegalArgumentException the requested command does not exist or does not match the
	 *                                  expected class
	 */
	@NotNull
	public final <T extends Command<PLAYER>> T getCommandByName(@NotNull String name, @NotNull Class<T> expectedClass) throws IllegalArgumentException {
		Command<PLAYER> command = getCommandByName(name);
		if (!expectedClass.isInstance(command))
			throw new IllegalArgumentException("Expected command '" + name
					+ "' to an instance of " + expectedClass.getSimpleName()
					+ ", not " + command.getClass().getSimpleName());
		//noinspection unchecked
		return (T) command;
	}
}
