package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.fabric.commands.*;
import dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, FabricCrowdControlPlugin> {
	private boolean tagsRegistered = false;
	private Set<EntityType<?>> safeEntities;
	private MappedKeyedTag<Block> setBlocks;
	private MappedKeyedTag<Block> setFallingBlocks;
	private MappedKeyedTag<Item> giveTakeItems;

	public CommandRegister(FabricCrowdControlPlugin plugin) {
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
	protected void createCommands(List<Command<ServerPlayer>> commands) {
		super.createCommands(commands);
		registerTags();
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
				FreezeCommand.feet(plugin),
				FreezeCommand.camera(plugin),
				FreezeCommand.skyCamera(plugin),
				FreezeCommand.groundCamera(plugin),
				new FlowerCommand(plugin),
				new FlingCommand(plugin),
				new TorchCommand(plugin, true),
				new TorchCommand(plugin, false),
				new GravelCommand(plugin),
				new DigCommand(plugin),
				new TimeCommand(plugin),
				new ItemDurabilityCommand.Repair(plugin),
				new ItemDurabilityCommand.Damage(plugin),
				new RemoveEnchantsCommand(plugin),
				new HatCommand(plugin),
				new RespawnCommand(plugin),
				new DropItemCommand(plugin),
				new DeleteItemCommand(plugin),
				new BucketClutchCommand(plugin),
				new DisableJumpingCommand(plugin),
				new EntityChaosCommand(plugin),
				new FlightCommand(plugin),
				new KeepInventoryCommand(plugin, true),
				new KeepInventoryCommand(plugin, false),
				new ClearInventoryCommand(plugin),
				new PlantTreeCommand(plugin),
				new DoOrDieCommand(plugin),
				new ExplodeCommand(plugin),
				SetTimeCommand.day(plugin),
				SetTimeCommand.night(plugin),
				WeatherCommand.clear(plugin),
				WeatherCommand.downfall(plugin),
				WeatherCommand.storm(plugin),
				GravityCommand.zero(plugin),
				GravityCommand.low(plugin),
				GravityCommand.high(plugin),
				GravityCommand.maximum(plugin)

				// TODO location commands from paper
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
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		plugin.getEventManager().registerListeners(command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getEventManager().registerListeners(new GameModeCommand.Manager());
	}
}
