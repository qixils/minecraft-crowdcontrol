package dev.qixils.crowdcontrol.plugin.mojmap;

import dev.qixils.crowdcontrol.common.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.Command;
import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.mojmap.commands.*;
import dev.qixils.crowdcontrol.plugin.mojmap.utils.TypedTag;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.qixils.crowdcontrol.common.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, MojmapPlugin<?>, Command<ServerPlayer>> {
	private boolean tagsRegistered = false;
	private Set<EntityType<?>> safeEntities;
	private MappedKeyedTag<Block> setBlocks;
	private MappedKeyedTag<Block> setFallingBlocks;
	private MappedKeyedTag<Item> giveTakeItems;

	public CommandRegister(MojmapPlugin<?> plugin) {
		super(plugin);
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			safeEntities = new HashSet<>(new TypedTag<>(CommandConstants.SAFE_ENTITIES, Registry.ENTITY_TYPE).getAll());
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, Registry.BLOCK);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, Registry.BLOCK);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, Registry.ITEM);
		}
	}

	@Override
	protected List<Command<ServerPlayer>> createCommands() {
		registerTags();
		List<Command<ServerPlayer>> commands = new ArrayList<>(Arrays.asList(
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
//				new ToastCommand(plugin),
//				new FreezeCommand(plugin),
//				new CameraLockCommand(plugin),
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
//				new TorchCommand(plugin, true),
//				new TorchCommand(plugin, false),
				new GravelCommand(plugin),
				new DigCommand(plugin),
				new TimeCommand(plugin),
				ItemDurabilityCommand.damage(plugin),
				ItemDurabilityCommand.repair(plugin),
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
//				new DisableJumpingCommand(plugin),
				new EntityChaosCommand(plugin),
//				new CameraLockToSkyCommand(plugin),
//				new CameraLockToGroundCommand(plugin),
//				new FlightCommand(plugin),
				new KeepInventoryCommand(plugin, true),
				new KeepInventoryCommand(plugin, false),
				new ClearInventoryCommand(plugin),
				new PlantTreeCommand(plugin),
//				new DoOrDieCommand(plugin),
				new ExplodeCommand(plugin),
				new SetTimeCommand(plugin, "Set Time to Day", "time_day", DAY),
				new SetTimeCommand(plugin, "Set Time to Night", "time_night", NIGHT),
				WeatherCommand.clear(plugin),
				WeatherCommand.downfall(plugin),
				WeatherCommand.storm(plugin)
		));

		// entity commands
		for (EntityType<?> entity : safeEntities) {
			commands.add(new SummonEntityCommand<>(plugin, entity));
			commands.add(new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values())
			commands.add(new DifficultyCommand(plugin, difficulty));

		// potions
		for (MobEffect potion : Registry.MOB_EFFECT)
			commands.add(new PotionCommand(plugin, potion));

		// block sets
		for (Block block : setBlocks)
			commands.add(new BlockCommand(plugin, block));

		for (Block block : setFallingBlocks)
			commands.add(new FallingBlockCommand(plugin, block));

		// enchantments
		for (Enchantment enchantment : Registry.ENCHANTMENT)
			commands.add(new EnchantmentCommand(plugin, enchantment));

		// give/take items
		for (Item item : giveTakeItems) {
			commands.add(new GiveItemCommand(plugin, item));
			commands.add(new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameType gameType : GameType.values()) {
			if (gameType == GameType.SURVIVAL) continue;
			commands.add(new GameModeCommand(plugin, gameType,
					gameType == GameType.SPECTATOR ? 8L : 15L));
		}

		return commands;
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		plugin.getEventManager().registerListeners(command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getEventManager().registerListeners(new GameModeCommand.Manager());
		plugin.getEventManager().registerListeners(new KeepInventoryCommand.Manager());
	}
}
