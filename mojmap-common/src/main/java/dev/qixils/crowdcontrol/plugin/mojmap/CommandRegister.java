package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.Command;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.ClearInventoryCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.DeleteItemCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.DifficultyCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.DigCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.DropItemCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.FlingCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.KeepInventoryCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.MoveCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.SetTimeCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.SoundCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.SwapCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.TeleportCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.qixils.crowdcontrol.common.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, MojmapPlugin, Command<ServerPlayer>> {
	public CommandRegister(MojmapPlugin plugin) {
		super(plugin);
	}

	@Override
	protected List<Command<ServerPlayer>> createCommands() {
		List<Command<ServerPlayer>> commands = new ArrayList<>(Arrays.asList(
//				new VeinCommand(plugin),
				new SoundCommand(plugin),
//				new ChargedCreeperCommand(plugin),
				new SwapCommand(plugin),
//				new DinnerboneCommand(plugin),
//				new ClutterCommand(plugin),
//				new LootboxCommand(plugin, "Open Lootbox", 0),
//				new LootboxCommand(plugin, "Open Lucky Lootbox", 5),
//				new LootboxCommand(plugin, "Open Very Lucky Lootbox", 10),
				new TeleportCommand(plugin),
//				new ToastCommand(plugin),
//				new FreezeCommand(plugin),
//				new CameraLockCommand(plugin),
//				new FlowerCommand(plugin),
				new MoveCommand(plugin, 0, 1, 0, "Up"),
				new MoveCommand(plugin, 0, -2, 0, "Down"),
				// begin: deprecated effects
				new MoveCommand(plugin, 2, 0.2, 0, "xplus", "East"),
				new MoveCommand(plugin, -2, 0.2, 0, "xminus", "West"),
				new MoveCommand(plugin, 0, 0.2, 2, "zplus", "South"),
				new MoveCommand(plugin, 0, 0.2, -2, "zminus", "North"),
				// end: deprecated effects
				new FlingCommand(plugin),
//				new TorchCommand(plugin, true),
//				new TorchCommand(plugin, false),
//				new GravelCommand(plugin),
				new DigCommand(plugin),
//				new TimeCommand(plugin),
//				new ItemRepairCommand(plugin),
//				new ItemDamageCommand(plugin),
//				new RemoveEnchantsCommand(plugin),
//				new HatCommand(plugin),
//				new RespawnCommand(plugin),
				new DropItemCommand(plugin),
				new DeleteItemCommand(plugin),
//				new BucketClutchCommand(plugin),
//				new DamageCommand(plugin, "kill", "Kill Players", Integer.MAX_VALUE),
//				new DamageCommand(plugin, "damage_1", "Damage Players (1 Heart)", 2f),
//				new DamageCommand(plugin, "heal_1", "Heal Players (1 Heart)", -2f),
//				new DamageCommand(plugin, "full_heal", "Heal Players", Integer.MIN_VALUE),
//				new HalfHealthCommand(plugin),
//				new FeedCommand(plugin, "feed", "Feed Players", 40),
//				new FeedCommand(plugin, "feed_1", "Feed Players (1 Bar)", 2),
//				new FeedCommand(plugin, "starve", "Starve Players", Integer.MIN_VALUE),
//				new FeedCommand(plugin, "starve_1", "Remove One Hunger Bar", -2),
//				new ResetExpProgressCommand(plugin),
//				new ExperienceCommand(plugin, "xp_plus1", "Give One XP Level", 1),
//				new ExperienceCommand(plugin, "xp_sub1", "Take One XP Level", -1),
//				new MaxHealthCommand(plugin, -1),
//				new MaxHealthCommand(plugin, 1),
//				new MaxHealthCommand(plugin, 4), // used in hype trains only
//				new DisableJumpingCommand(plugin),
//				new EntityChaosCommand(plugin),
//				new CameraLockToSkyCommand(plugin),
//				new CameraLockToGroundCommand(plugin),
//				new FlightCommand(plugin),
				new KeepInventoryCommand(plugin, true),
				new KeepInventoryCommand(plugin, false),
				new ClearInventoryCommand(plugin),
//				new PlantTreeCommand(plugin),
//				new DoOrDieCommand(plugin),
//				new ExplodeCommand(plugin),
				new SetTimeCommand(plugin, "Set Time to Day", "time_day", DAY),
				new SetTimeCommand(plugin, "Set Time to Night", "time_night", NIGHT)
		));

		// entity commands
//		for (EntityType<?> entity : safeEntities) {
//			commands.add(new SummonEntityCommand(plugin, entity));
//			commands.add(new RemoveEntityCommand(plugin, entity));
//		}

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values()) {
			commands.add(new DifficultyCommand(plugin, difficulty));
		}

		// potions
//		plugin.getGame().registry(RegistryTypes.POTION_EFFECT_TYPE).stream().forEach(
//				potionEffectType -> commands.add(new PotionCommand(plugin, potionEffectType)));

		// block sets
//		for (BlockType block : setBlocks) {
//			commands.add(new BlockCommand(plugin, block));
//		}

//		for (BlockType block : setFallingBlocks) {
//			commands.add(new FallingBlockCommand(plugin, block));
//		}

		// weather commands
//		plugin.getGame().registry(RegistryTypes.WEATHER_TYPE).stream().forEach(
//				weather -> commands.add(new WeatherCommand(plugin, weather)));

		// enchantments
//		plugin.getGame().registry(RegistryTypes.ENCHANTMENT_TYPE).stream().forEach(
//				enchantmentType -> commands.add(new EnchantmentCommand(plugin, enchantmentType)));

		// give/take items
//		for (ItemType item : giveTakeItems) {
//			commands.add(new GiveItemCommand(plugin, item));
//			commands.add(new TakeItemCommand(plugin, item));
//		}

		// gamemode commands
//		plugin.getGame().registry(RegistryTypes.GAME_MODE).stream()
//				.filter(gamemode -> !gamemode.equals(GameModes.SURVIVAL.get()))
//				.forEach(gamemode -> commands.add(new GameModeCommand(plugin, gamemode,
//						gamemode.equals(GameModes.SPECTATOR.get()) ? 8L : 15L)));

		return commands;
	}

	@Override
	protected void onFirstRegistry() {
		// TODO register KeepInventoryCommand.Manager and GameModeCommand.Manager
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		// TODO
	}
}
