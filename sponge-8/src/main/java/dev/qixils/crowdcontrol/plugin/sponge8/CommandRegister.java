package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.*;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.Arrays;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, SpongeCrowdControlPlugin> {
	private boolean tagsRegistered = false;
	private MappedKeyedTag<BlockType> setBlocks;
	private MappedKeyedTag<BlockType> setFallingBlocks;
	private MappedKeyedTag<ItemType> giveTakeItems;

	public CommandRegister(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, plugin, RegistryTypes.BLOCK_TYPE);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, plugin, RegistryTypes.BLOCK_TYPE);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, plugin, RegistryTypes.ITEM_TYPE);
		}
	}

	@Override
	protected void createCommands(List<Command<ServerPlayer>> commands) {
		super.createCommands(commands);
		registerTags();
		// register normal commands
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
				// end: deprecated effects
				new FlingCommand(plugin),
				new TorchCommand(plugin, true),
				new TorchCommand(plugin, false),
				new GravelCommand(plugin),
				new DigCommand(plugin),
				new ItemRepairCommand(plugin),
				new ItemDamageCommand(plugin),
				new RemoveEnchantsCommand(plugin),
				new HatCommand(plugin),
				// TODO: fails to get bed location and defaults to 0,0 -- new RespawnCommand(plugin),
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
		plugin.getGame().registry(RegistryTypes.ENTITY_TYPE).stream().forEach(entity -> {
			commands.add(new SummonEntityCommand<>(plugin, entity));
			commands.add(new RemoveEntityCommand<>(plugin, entity));
		});

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
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), new KeepInventoryCommand.Manager());
		plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), new GameModeCommand.Manager());
		plugin.getGame().eventManager().registerListeners(plugin.getPluginContainer(), new HealthModifierManager());
	}
}
