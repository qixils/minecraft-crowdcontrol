package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
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

import java.util.Arrays;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<Player, PaperCrowdControlPlugin> {
	private static final MaterialTag SET_BLOCKS = new MaterialTag(CommandConstants.SET_BLOCKS);
	private static final MaterialTag SET_FALLING_BLOCKS = new MaterialTag(CommandConstants.SET_FALLING_BLOCKS);
	private static final MaterialTag GIVE_TAKE_ITEMS = new MaterialTag(CommandConstants.GIVE_TAKE_ITEMS);

	protected CommandRegister(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void createCommands(List<Command<Player>> commands) {
		super.createCommands(commands);
		commands.addAll(Arrays.asList(
				new VeinCommand(plugin),
				new SoundCommand(plugin),
				new ChargedCreeperCommand(plugin),
				new SwapCommand(plugin),
				new DinnerboneCommand(plugin),
				new ClutterCommand(plugin),
				new LootboxCommand(plugin, 0),
				new LootboxCommand(plugin, 5),
				new LootboxCommand(plugin, 10),
				new TeleportCommand(plugin),
				new ToastCommand(plugin),
				new FreezeCommand(plugin),
				new CameraLockCommand(plugin),
				new FlowerCommand(plugin),
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
				new SetTimeCommand(plugin, "time_day", DAY),
				new SetTimeCommand(plugin, "time_night", NIGHT),
				GravityCommand.zero(plugin),
				GravityCommand.low(plugin),
				GravityCommand.high(plugin),
				GravityCommand.maximum(plugin),
				new DeleteRandomItemCommand(plugin)
		));

		// entity commands
		for (EntityType entity : EntityType.values()) {
			if (entity == EntityType.UNKNOWN) continue;
			// TODO: skip experimental entities (blocked by Paper#8950(Issue)/Paper#8952(PR))
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
		for (Material block : SET_BLOCKS) {
			commands.add(new BlockCommand(plugin, block));
		}

		for (Material block : SET_FALLING_BLOCKS) {
			commands.add(new FallingBlockCommand(plugin, block));
		}

		// enchantments
		for (Enchantment enchantment : Enchantment.values()) {
			try {
				commands.add(new EnchantmentCommand(plugin, enchantment));
			} catch (AbstractMethodError ignored) {
				plugin.getSLF4JLogger().warn("Enchantment " + enchantment.getKey() + " does not implement the Adventure/Paper API. Ignoring.");
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
			commands.add(new GameModeCommand(plugin, gamemode,
					gamemode == GameMode.SPECTATOR ? 8L : 15L)); // duration (in seconds)
		}
	}

	@Override
	protected void registerListener(Command<Player> command) {
		Bukkit.getPluginManager().registerEvents((Listener) command, plugin);
	}

	@Override
	protected void onFirstRegistry() {
		Bukkit.getPluginManager().registerEvents(new KeepInventoryCommand.Manager(), plugin);
		Bukkit.getPluginManager().registerEvents(new GameModeCommand.Manager(plugin), plugin);
		Bukkit.getPluginManager().registerEvents(new HealthModifierManager(), plugin);
	}
}
