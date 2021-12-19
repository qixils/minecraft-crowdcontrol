package dev.qixils.crowdcontrol.plugin;

import dev.qixils.crowdcontrol.common.util.CommonTags;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.commands.ChargedCreeperCommand;
import dev.qixils.crowdcontrol.plugin.commands.RemoveEntityCommand;
import dev.qixils.crowdcontrol.plugin.commands.SummonEntityCommand;
import dev.qixils.crowdcontrol.plugin.utils.TypedTag;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.ItemType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommandRegister {
	private final SpongeCrowdControlPlugin plugin;
	private boolean tagsRegistered = false;
	private MappedKeyedTag<EntityType> safeEntities;
	private MappedKeyedTag<ItemType> setBlocks;
	private MappedKeyedTag<ItemType> setFallingBlocks;
	private MappedKeyedTag<ItemType> giveTakeItems;
	private List<Command> registeredCommands;

	public CommandRegister(SpongeCrowdControlPlugin plugin) {
		this.plugin = plugin;
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			safeEntities = new TypedTag<>(CommonTags.SAFE_ENTITIES, plugin, EntityType.class);
			setBlocks = new TypedTag<>(CommonTags.SET_BLOCKS, plugin, ItemType.class);
			setFallingBlocks = new TypedTag<>(CommonTags.SET_FALLING_BLOCKS, plugin, ItemType.class);
			giveTakeItems = new TypedTag<>(CommonTags.GIVE_TAKE_ITEMS, plugin, ItemType.class);
		}
	}

	public List<Command> getCommands() {
		if (registeredCommands != null)
			return registeredCommands;

		registerTags();

		// register normal commands
		List<Command> commands = new ArrayList<>(Arrays.asList(
//				new VeinCommand(plugin),
//				new SoundCommand(plugin),
				new ChargedCreeperCommand(plugin)
//				new SwapCommand(plugin),
//				new DinnerboneCommand(plugin),
//				new ClutterCommand(plugin),
//				new LootboxCommand(plugin),
//				new TeleportCommand(plugin),
//				new ToastCommand(plugin),
//				new FreezeCommand(plugin),
//				new CameraLockCommand(plugin),
//				new FlowerCommand(plugin),
//				new MoveCommand(plugin, 0, 1, 0, "Up"),
//				new MoveCommand(plugin, 0, -2, 0, "Down"),
//				new MoveCommand(plugin, 2, 0.2, 0, "xplus", "East"),
//				new MoveCommand(plugin, -2, 0.2, 0, "xminus", "West"),
//				new MoveCommand(plugin, 0, 0.2, 2, "zplus", "South"),
//				new MoveCommand(plugin, 0, 0.2, -2, "zminus", "North"),
//				new TorchCommand(plugin, true),
//				new TorchCommand(plugin, false),
//				new GravelCommand(plugin),
//				new DigCommand(plugin),
//				new TimeCommand(plugin),
//				new ItemDamageCommand(plugin, true),
//				new ItemDamageCommand(plugin, false),
//				new RemoveEnchantsCommand(plugin),
//				new HatCommand(plugin),
//				new RespawnCommand(plugin),
//				new DropItemCommand(plugin),
//				new DeleteItemCommand(plugin),
//				new BucketClutchCommand(plugin),
//				new DamageCommand(plugin, "kill", "Kill Players", Integer.MAX_VALUE),
//				new DamageCommand(plugin, "damage_1", "Damage Players (1 Heart)", 2f),
//				new DamageCommand(plugin, "heal_1", "Heal Players (1 Heart)", -2f),
//				new DamageCommand(plugin, "full_heal", "Heal Players", -Integer.MAX_VALUE),
//				new HalfHealthCommand(plugin),
//				new FeedCommand(plugin, "feed", "Feed Players", 40),
//				new FeedCommand(plugin, "feed_1", "Feed Players (1 Bar)", 2),
//				new FeedCommand(plugin, "starve", "Starve Players", -Integer.MAX_VALUE),
//				new FeedCommand(plugin, "starve_1", "Remove One Hunger Bar", -2),
//				new ResetExpProgressCommand(plugin),
//				new ExperienceCommand(plugin, "xp_plus1", "Give One XP Level", 1),
//				new ExperienceCommand(plugin, "xp_sub1", "Take One XP Level", -1),
//				new MaxHealthCommand(plugin, -1),
//				new MaxHealthCommand(plugin, 1),
//				new DisableJumpingCommand(plugin),
//				new EntityChaosCommand(plugin),
//				new CameraLockToSkyCommand(plugin),
//				new CameraLockToGroundCommand(plugin),
//				new FlightCommand(plugin),
//				new KeepInventoryCommand(plugin, true),
//				new KeepInventoryCommand(plugin, false),
//				new ClearInventoryCommand(plugin),
//				new PlantTreeCommand(plugin),
//				new DoOrDieCommand(plugin)
		));

		// register keep inventory event handler
//		Bukkit.getPluginManager().registerEvents(new KeepInventoryCommand.Manager(), plugin);

		// entity commands
		for (EntityType entity : safeEntities) {
			commands.add(new SummonEntityCommand(plugin, entity));
			commands.add(new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
//		for (Difficulty difficulty : Difficulty.values()) {
//			commands.add(new DifficultyCommand(plugin, difficulty));
//		}

		// potions
//		for (PotionEffectType potionEffectType : PotionEffectType.values()) {
//			commands.add(new PotionCommand(plugin, potionEffectType));
//		}

		// block sets
//		for (Material SET_BLOCK : SET_BLOCKS) {
//			commands.add(new BlockCommand(plugin, SET_BLOCK));
//		}
//
//		for (Material block : SET_FALLING_BLOCKS) {
//			commands.add(new FallingBlockCommand(plugin, block));
//		}

		// weather commands
//		for (WeatherType weatherType : WeatherType.values()) {
//			commands.add(new WeatherCommand(plugin, weatherType));
//		}

		// enchantments
//		for (Enchantment enchantment : Enchantment.values()) {
//			commands.add(new EnchantmentCommand(plugin, enchantment));
//		}

		// give/take items
//		for (Material item : GIVE_TAKE_ITEMS) {
//			commands.add(new GiveItemCommand(plugin, item));
//			commands.add(new TakeItemCommand(plugin, item));
//		}

		// gamemode commands
//		for (GameMode gamemode : GameMode.values()) {
//			if (gamemode == GameMode.SURVIVAL) continue;
//			commands.add(new GamemodeCommand(plugin, gamemode,
//					gamemode == GameMode.SPECTATOR ? 8L : 15L)); // duration (in seconds)
//		}

		registeredCommands = commands;
		return registeredCommands;
	}

	public void register() {
		boolean firstRegistry = registeredCommands == null;
		for (Command command : getCommands()) {
			String name = command.getEffectName().toLowerCase(Locale.ENGLISH);
			plugin.registerCommand(name, command);

			if (firstRegistry && command.isEventListener())
				plugin.getGame().getEventManager().registerListeners(plugin, command);
		}
	}

	public void writeCommands(List<Command> commands) {
		try {
			FileWriter fileWriter = new FileWriter("crowdcontrol_commands.txt");
			for (Command command : commands)
				fileWriter.write("        new Effect(\"" + command.getDisplayName() + "\", \"" + command.getEffectName().toLowerCase(Locale.ENGLISH) + "\"),\n");
			fileWriter.close();
		} catch (IOException e) {
			if (plugin != null)
				plugin.getLogger().warn("Failed to write commands to file.");
			e.printStackTrace();
		}
	}
}
