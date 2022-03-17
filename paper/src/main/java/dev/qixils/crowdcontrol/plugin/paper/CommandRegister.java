package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.paper.commands.*;
import dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.MaterialTag;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<Player, PaperCrowdControlPlugin, Command> {
	@SuppressWarnings("deprecation") // Bukkit is dumb
	private static final MappedKeyedTag<EntityType> SAFE_ENTITIES =
			new MappedKeyedTag<>(CommandConstants.SAFE_ENTITIES, key -> EntityType.fromName(key.value()));
	private static final MaterialTag SET_BLOCKS = new MaterialTag(CommandConstants.SET_BLOCKS);
	private static final MaterialTag SET_FALLING_BLOCKS = new MaterialTag(CommandConstants.SET_FALLING_BLOCKS);
	private static final MaterialTag GIVE_TAKE_ITEMS = new MaterialTag(CommandConstants.GIVE_TAKE_ITEMS);

	protected CommandRegister(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected List<Command> createCommands() {
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
				new ItemDamageCommand(plugin, true),
				new ItemDamageCommand(plugin, false),
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
				new ClearWeatherCommand(plugin),
				new RainyWeatherCommand(plugin),
				new ThunderingWeatherCommand(plugin),
				new StructureCommand(plugin),
				new BiomeCommand(plugin),
				new ExplodeCommand(plugin),
				new SetTimeCommand(plugin, "Set Time to Day", "time_day", DAY),
				new SetTimeCommand(plugin, "Set Time to Night", "time_night", NIGHT)
		));

		// entity commands
		for (EntityType entity : SAFE_ENTITIES) {
			commands.add(new SummonEntityCommand(plugin, entity));
			commands.add(new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values()) {
			commands.add(new DifficultyCommand(plugin, difficulty));
		}

		// potions
		for (PotionEffectType potionEffectType : PotionEffectType.values()) {
			commands.add(new PotionCommand(plugin, potionEffectType));
		}

		// block sets
		for (Material SET_BLOCK : SET_BLOCKS) {
			commands.add(new BlockCommand(plugin, SET_BLOCK));
		}

		for (Material block : SET_FALLING_BLOCKS) {
			commands.add(new FallingBlockCommand(plugin, block));
		}

		// enchantments
		for (Enchantment enchantment : Enchantment.values()) {
			try {
				commands.add(new EnchantmentCommand(plugin, enchantment));
			} catch (AbstractMethodError ignored) {
				// ignore enchants that do not implement the Adventure/Paper API
			}
		}

		// give/take items
		for (Material item : GIVE_TAKE_ITEMS) {
			commands.add(new GiveItemCommand(plugin, item));
			commands.add(new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameMode gamemode : GameMode.values()) {
			if (gamemode == GameMode.SURVIVAL) continue;
			commands.add(new GamemodeCommand(plugin, gamemode,
					gamemode == GameMode.SPECTATOR ? 8L : 15L)); // duration (in seconds)
		}

		return commands;
	}

	@Override
	protected void registerListener(Command command) {
		Bukkit.getPluginManager().registerEvents((Listener) command, plugin);
	}

	@Override
	protected void onFirstRegistry() {
		Bukkit.getPluginManager().registerEvents(new KeepInventoryCommand.Manager(), plugin);
		Bukkit.getPluginManager().registerEvents(new GamemodeCommand.Manager(plugin), plugin);
	}
}
