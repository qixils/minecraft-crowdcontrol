package dev.qixils.crowdcontrol.plugin.sponge8;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.*;
import dev.qixils.crowdcontrol.plugin.sponge8.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.TypedTag;
import net.kyori.adventure.key.Key;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

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
		commands.addAll(this.<Command<ServerPlayer>>initAll(
			() -> new VeinCommand(plugin),
			() -> new SoundCommand(plugin),
			() -> new ChargedCreeperCommand(plugin),
			() -> new SwapCommand(plugin),
			() -> new DinnerboneCommand(plugin),
			() -> new ClutterCommand(plugin),
			() -> new LootboxCommand(plugin, 0),
			() -> new LootboxCommand(plugin, 5),
			() -> new LootboxCommand(plugin, 10),
			() -> new TeleportCommand(plugin),
			() -> new ToastCommand(plugin),
			() -> new FreezeCommand(plugin),
			() -> new CameraLockCommand(plugin),
			() -> new FlowerCommand(plugin),
			() -> new FlingCommand(plugin),
			() -> new TorchCommand(plugin, true),
			() -> new TorchCommand(plugin, false),
			() -> new GravelCommand(plugin),
			() -> new DigCommand(plugin),
			() -> new ItemRepairCommand(plugin),
			() -> new ItemDamageCommand(plugin),
			() -> new RemoveEnchantsCommand(plugin),
			() -> new HatCommand(plugin),
			// TODO: fails to get bed location and defaults to 0,0 -- new RespawnCommand(plugin),
			() -> new DropItemCommand(plugin),
			() -> new DeleteItemCommand(plugin),
			() -> new BucketClutchCommand(plugin),
			() -> new DisableJumpingCommand(plugin),
			() -> new EntityChaosCommand(plugin),
			() -> new CameraLockToSkyCommand(plugin),
			() -> new CameraLockToGroundCommand(plugin),
			() -> new FlightCommand(plugin),
			() -> new KeepInventoryCommand(plugin, true),
			() -> new KeepInventoryCommand(plugin, false),
			() -> new ClearInventoryCommand(plugin),
			() -> new PlantTreeCommand(plugin),
			() -> new DoOrDieCommand(plugin),
			() -> new ExplodeCommand(plugin),
			() -> new SetTimeCommand(plugin, "time_day", DAY),
			() -> new SetTimeCommand(plugin, "time_night", NIGHT),
			() -> GravityCommand.zero(plugin),
			() -> GravityCommand.low(plugin),
			() -> GravityCommand.high(plugin),
			() -> GravityCommand.maximum(plugin),
			() -> new DeleteRandomItemCommand(plugin),
			() -> new UniteCommand(plugin)
		));

		// entity commands
		Map<String, EntityType<?>> minecraftEntities = new HashMap<>();
		Map<String, EntityType<?>> moddedEntities = new HashMap<>();
		plugin.getGame().registry(RegistryTypes.ENTITY_TYPE).streamEntries().forEach(entry -> {
			if (!isWhitelistedEntity(entry.key())) return;
			String id = CommandConstants.csIdOf(entry.key());
			Map<String, EntityType<?>> entities = entry.key().namespace().equals(Key.MINECRAFT_NAMESPACE) ? minecraftEntities : moddedEntities;
			entities.put(id, entry.value());
		});
		for (Map.Entry<String, EntityType<?>> entry : moddedEntities.entrySet()) {
			if (minecraftEntities.containsKey(entry.getKey())) continue;
			minecraftEntities.put(entry.getKey(), entry.getValue());
		}
		for (EntityType<?> entity : minecraftEntities.values()) {
			initTo(commands, () -> new SummonEntityCommand<>(plugin, entity));
			initTo(commands, () -> new RemoveEntityCommand<>(plugin, entity));
		}

		// register difficulty commands
		plugin.getGame().registry(RegistryTypes.DIFFICULTY).stream().forEach(
				difficulty -> initTo(commands, () -> new DifficultyCommand(plugin, difficulty)));

		// potions
		plugin.getGame().registry(RegistryTypes.POTION_EFFECT_TYPE).stream().forEach(
				potionEffectType -> initTo(commands, () -> new PotionCommand(plugin, potionEffectType)));

		// block sets
		for (BlockType block : setBlocks) {
			initTo(commands, () -> new BlockCommand(plugin, block));
		}

		for (BlockType block : setFallingBlocks) {
			initTo(commands, () -> new FallingBlockCommand(plugin, block));
		}

		// weather commands
		plugin.getGame().registry(RegistryTypes.WEATHER_TYPE).stream().forEach(
				weather -> initTo(commands, () -> new WeatherCommand(plugin, weather)));

		// enchantments
		plugin.getGame().registry(RegistryTypes.ENCHANTMENT_TYPE).stream().forEach(
				enchantmentType -> initTo(commands, () -> new EnchantmentCommand(plugin, enchantmentType)));

		// give/take items
		for (ItemType item : giveTakeItems) {
			initTo(commands, () -> new GiveItemCommand(plugin, item));
			initTo(commands, () -> new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		plugin.getGame().registry(RegistryTypes.GAME_MODE).stream()
				.filter(gamemode -> !gamemode.equals(GameModes.SURVIVAL.get()))
				.forEach(gamemode -> initTo(commands, () -> new GameModeCommand(plugin, gamemode,
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
