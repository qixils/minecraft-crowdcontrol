package dev.qixils.crowdcontrol.common;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Manages the registry of commands (aka effects).
 *
 * @param <PLAYER> the player class used by your plugin
 * @param <PLUGIN> the plugin class using this registry
 * @param <COMMAND> the command class used by your plugin
 */
public abstract class AbstractCommandRegister<PLAYER, PLUGIN extends Plugin<PLAYER, ?>, COMMAND extends Command<PLAYER>> {
	protected final @NotNull PLUGIN plugin;
	protected final @NotNull Set<Class<? extends COMMAND>> registeredCommandClasses = new HashSet<>();
	protected final @NotNull Map<Class<? extends COMMAND>, COMMAND> singleCommandInstances = new HashMap<>();
	protected final @NotNull Map<String, COMMAND> registeredCommandMap = new HashMap<>();
	protected @MonotonicNonNull List<COMMAND> registeredCommands;

	protected AbstractCommandRegister(@NotNull PLUGIN plugin) {
		this.plugin = plugin;
	}

	protected abstract List<COMMAND> createCommands();

	public final List<COMMAND> getCommands() {
		if (registeredCommands != null)
			return registeredCommands;

		List<COMMAND> commands = createCommands();
		for (COMMAND command : commands) {
			registeredCommandMap.put(command.getEffectName().toLowerCase(Locale.ENGLISH), command);

			//noinspection unchecked
			Class<COMMAND> clazz = (Class<COMMAND>) command.getClass();
			if (registeredCommandClasses.contains(clazz))
				singleCommandInstances.remove(clazz);
			else
				singleCommandInstances.put(clazz, command);
			registeredCommandClasses.add(clazz);
		}

		return registeredCommands = commands;
	}

	protected abstract void onFirstRegistry();

	protected abstract void registerListener(COMMAND command);

	public final void register() {
		boolean firstRegistry = registeredCommands == null;
		for (COMMAND command : getCommands()) {
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
	public final <T extends COMMAND> T getCommand(@NotNull Class<T> tClass) throws IllegalArgumentException {
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
	public final COMMAND getCommandByName(@NotNull String name) throws IllegalArgumentException {
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
	public final <T extends COMMAND> T getCommandByName(@NotNull String name, @NotNull Class<T> expectedClass) throws IllegalArgumentException {
		COMMAND command = getCommandByName(name);
		if (!expectedClass.isInstance(command))
			throw new IllegalArgumentException("Expected command '" + name
					+ "' to an instance of " + expectedClass.getSimpleName()
					+ ", not " + command.getClass().getSimpleName());
		//noinspection unchecked
		return (T) command;
	}
}
