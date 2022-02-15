package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.*;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.CommandConstants.NIGHT;

public class CommandRegister {
	private final SpongeCrowdControlPlugin plugin;
	private final Set<Class<? extends Command>> registeredCommandClasses = new HashSet<>();
	private final Map<Class<? extends Command>, Command> singleCommandInstances = new HashMap<>();
	private boolean tagsRegistered = false;
	private Set<EntityType<?>> safeEntities;
	private MappedKeyedTag<BlockType> setBlocks;
	private MappedKeyedTag<BlockType> setFallingBlocks;
	private MappedKeyedTag<ItemType> giveTakeItems;
	private List<Command> registeredCommands;

	public CommandRegister(SpongeCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			safeEntities = new HashSet<>(new TypedTag<>(CommandConstants.SAFE_ENTITIES, plugin, RegistryTypes.ENTITY_TYPE).getAll());
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, plugin, RegistryTypes.BLOCK_TYPE);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, plugin, RegistryTypes.BLOCK_TYPE);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, plugin, RegistryTypes.ITEM_TYPE);
		}
	}

	public List<Command> getCommands() {
		if (registeredCommands != null)
			return registeredCommands;

		registerTags();

		// register normal commands
		List<Command> commands = new ArrayList<>(Arrays.asList(
				new VeinCommand(plugin),
				new SoundCommand(plugin),
				new ChargedCreeperCommand(plugin),
				new SwapCommand(plugin),
				new DinnerboneCommand(plugin),
				new ClutterCommand(plugin),
				new LootboxCommand(plugin, "Open Lootbox", 0),
				new LootboxCommand(plugin, "Open Lucky Lootbox", 5),
				new LootboxCommand(plugin, "Open Very Lucky Lootbox", 10),
				new TeleportCommand(plugin),
				new ToastCommand(plugin),
				new FreezeCommand(plugin),
				new CameraLockCommand(plugin),
				new FlowerCommand(plugin),
				new MoveCommand(plugin, 0, 1, 0, "Up"),
				new MoveCommand(plugin, 0, -2, 0, "Down"),
				// begin: deprecated effects
				new MoveCommand(plugin, 2, 0.2, 0, "xplus", "East"),
				new MoveCommand(plugin, -2, 0.2, 0, "xminus", "West"),
				new MoveCommand(plugin, 0, 0.2, 2, "zplus", "South"),
				new MoveCommand(plugin, 0, 0.2, -2, "zminus", "North"),
				// end: deprecated effects
				new FlingCommand(plugin),
				new TorchCommand(plugin, true),
				new TorchCommand(plugin, false),
				new GravelCommand(plugin),
				new DigCommand(plugin),
				new TimeCommand(plugin),
				new ItemRepairCommand(plugin),
				new ItemDamageCommand(plugin),
				new RemoveEnchantsCommand(plugin),
				new HatCommand(plugin),
				new RespawnCommand(plugin),
				new DropItemCommand(plugin),
				new DeleteItemCommand(plugin),
				new BucketClutchCommand(plugin),
				new DamageCommand(plugin, "kill", "Kill Players", Integer.MAX_VALUE),
				new DamageCommand(plugin, "damage_1", "Damage Players (1 Heart)", 2f),
				new DamageCommand(plugin, "heal_1", "Heal Players (1 Heart)", -2f),
				new DamageCommand(plugin, "full_heal", "Heal Players", Integer.MIN_VALUE),
				new HalfHealthCommand(plugin),
				new FeedCommand(plugin, "feed", "Feed Players", 40),
				new FeedCommand(plugin, "feed_1", "Feed Players (1 Bar)", 2),
				new FeedCommand(plugin, "starve", "Starve Players", Integer.MIN_VALUE),
				new FeedCommand(plugin, "starve_1", "Remove One Hunger Bar", -2),
				new ResetExpProgressCommand(plugin),
				new ExperienceCommand(plugin, "xp_plus1", "Give One XP Level", 1),
				new ExperienceCommand(plugin, "xp_sub1", "Take One XP Level", -1),
				new MaxHealthCommand(plugin, -1),
				new MaxHealthCommand(plugin, 1),
				new MaxHealthCommand(plugin, 4), // used in hype trains only
				new DisableJumpingCommand(plugin),
				new EntityChaosCommand(plugin),
				new CameraLockToSkyCommand(plugin),
				new CameraLockToGroundCommand(plugin),
				new FlightCommand(plugin),
				new KeepInventoryCommand(plugin, true),
				new KeepInventoryCommand(plugin, false),
				new ClearInventoryCommand(plugin),
				new PlantTreeCommand(plugin),
				new DoOrDieCommand(plugin),
				new ExplodeCommand(plugin),
				new SetTimeCommand(plugin, "Set Time to Day", "time_day", DAY),
				new SetTimeCommand(plugin, "Set Time to Night", "time_night", NIGHT)
		));

		// entity commands
		for (EntityType<?> entity : safeEntities) {
			commands.add(new SummonEntityCommand(plugin, entity));
			commands.add(new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		plugin.getGame().registry(RegistryTypes.DIFFICULTY).stream().forEach(
				difficulty -> commands.add(new DifficultyCommand(plugin, difficulty)));

		// potions
		plugin.getGame().registry(RegistryTypes.POTION_EFFECT_TYPE).stream().forEach(
				potionEffectType -> commands.add(new PotionCommand(plugin, potionEffectType)));

		// block sets
		for (BlockType block : setBlocks) {
			commands.add(new BlockCommand(plugin, block));
		}

		for (BlockType block : setFallingBlocks) {
			commands.add(new FallingBlockCommand(plugin, block));
		}

		// weather commands
		plugin.getGame().registry(RegistryTypes.WEATHER_TYPE).stream().forEach(
				weather -> commands.add(new WeatherCommand(plugin, weather)));

		// enchantments
		plugin.getGame().registry(RegistryTypes.ENCHANTMENT_TYPE).stream().forEach(
				enchantmentType -> commands.add(new EnchantmentCommand(plugin, enchantmentType)));

		// give/take items
		for (ItemType item : giveTakeItems) {
			commands.add(new GiveItemCommand(plugin, item));
			commands.add(new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		plugin.getGame().registry(RegistryTypes.GAME_MODE).stream()
				.filter(gamemode -> !gamemode.equals(GameModes.SURVIVAL.get()))
				.forEach(gamemode -> commands.add(new GameModeCommand(plugin, gamemode,
						gamemode.equals(GameModes.SPECTATOR.get()) ? 8L : 15L)));

		for (Command command : commands) {
			Class<? extends Command> clazz = command.getClass();
			if (registeredCommandClasses.contains(clazz))
				singleCommandInstances.remove(clazz);
			else
				singleCommandInstances.put(clazz, command);
			registeredCommandClasses.add(clazz);
		}

		return registeredCommands = commands;
	}

	public void register() {
		boolean firstRegistry = registeredCommands == null;
		for (Command command : getCommands()) {
			String name = command.getEffectName().toLowerCase(Locale.ENGLISH);
			plugin.registerCommand(name, command);

			if (firstRegistry && command.isEventListener())
				plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), command);
		}
		if (firstRegistry) {
			plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), new KeepInventoryCommand.Manager());
			plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), new GameModeCommand.Manager());
		}
	}

	/**
	 * Gets an instance of a registered command.
	 * Only commands that register a sole instance can be returned by this method.
	 *
	 * @param tClass class of the desired command
	 * @param <T>    type of the desired command
	 * @return the command instance
	 * @throws IllegalArgumentException the request command has not been registered or has been
	 *                                  registered several times
	 */
	@NotNull
	public <T> T getCommand(@NotNull Class<T> tClass) throws IllegalArgumentException {
		if (!singleCommandInstances.containsKey(tClass))
			throw new IllegalArgumentException("Requested class " + tClass.getName()
					+ " is invalid. Please ensure that only one instance of this command is registered.");
		//noinspection unchecked
		return (T) singleCommandInstances.get(tClass);
	}
}
