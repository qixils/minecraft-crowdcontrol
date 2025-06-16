package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.fabric.commands.*;
import dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.isWhitelistedEntity;
import static dev.qixils.crowdcontrol.common.util.CollectionUtil.initTo;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, ModdedCrowdControlPlugin> {
	private boolean tagsRegistered = false;
	private MappedKeyedTag<Block> setBlocks;
	private MappedKeyedTag<Block> setFallingBlocks;
	private MappedKeyedTag<Item> giveTakeItems;

	public CommandRegister(ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, BuiltInRegistries.BLOCK);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, BuiltInRegistries.BLOCK);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, BuiltInRegistries.ITEM);
		}
	}

	@Override
	protected void createCommands(List<Command<ServerPlayer>> commands) {
		super.createCommands(commands);
		registerTags();
		commands.addAll(this.<Command<ServerPlayer>>initAll(
			() -> new VeinCommand(plugin),
			() -> new SoundCommand(plugin),
			() -> new ChargedCreeperCommand(plugin),
			() -> new ChickenJockeyCommand(plugin),
			() -> new SmallAntCommand(plugin),
			() -> new SwapCommand(plugin),
			() -> new DinnerboneCommand(plugin),
			() -> new ClutterCommand(plugin),
			() -> new LootboxCommand(plugin, 0),
			() -> new LootboxCommand(plugin, 5),
			() -> new LootboxCommand(plugin, 10),
			() -> new TeleportCommand(plugin),
			() -> new ToastCommand(plugin),
			() -> FreezeCommand.feet(plugin),
			() -> FreezeCommand.camera(plugin),
			() -> FreezeCommand.skyCamera(plugin),
			() -> FreezeCommand.groundCamera(plugin),
			() -> new FlowerCommand(plugin),
			() -> new FlingCommand(plugin),
			() -> new TorchCommand(plugin, true),
			() -> new TorchCommand(plugin, false),
			() -> new GravelCommand(plugin),
			() -> new DigCommand(plugin),
			() -> new ItemDurabilityCommand.Repair(plugin),
			() -> new ItemDurabilityCommand.Damage(plugin),
			() -> new RemoveEnchantsCommand(plugin),
			() -> new HatCommand(plugin),
			() -> new RespawnCommand(plugin),
			() -> new DropItemCommand(plugin),
			() -> new DeleteItemCommand(plugin),
			() -> new BucketClutchCommand(plugin),
			() -> MovementStatusCommand.disableJumping(plugin),
			() -> MovementStatusCommand.invertControls(plugin),
			() -> MovementStatusCommand.invertCamera(plugin),
			() -> new EntityChaosCommand(plugin),
			() -> new FlightCommand(plugin),
			() -> new KeepInventoryCommand(plugin, true),
			() -> new KeepInventoryCommand(plugin, false),
			() -> new ClearInventoryCommand(plugin),
			() -> new PlantTreeCommand(plugin),
			() -> new DoOrDieCommand(plugin),
			() -> new ExplodeCommand(plugin),
			() -> SetTimeCommand.day(plugin),
			() -> SetTimeCommand.night(plugin),
			() -> WeatherCommand.clear(plugin),
			() -> WeatherCommand.downfall(plugin),
			() -> WeatherCommand.storm(plugin),
			() -> GravityCommand.zero(plugin),
			() -> GravityCommand.low(plugin),
			() -> GravityCommand.high(plugin),
			() -> GravityCommand.maximum(plugin),
			() -> new BiomeCommand(plugin),
			() -> new StructureCommand(plugin),
			() -> new DeleteRandomItemCommand(plugin),
			() -> new UniteCommand(plugin),
			() -> TickRateCommand.doubleRate(plugin),
			() -> TickRateCommand.halfRate(plugin),
			() -> new TickFreezeCommand(plugin),
			() -> PlayerSizeCommand.increase(plugin),
			() -> PlayerSizeCommand.decrease(plugin),
			() -> EntitySizeCommand.increase(plugin),
			() -> EntitySizeCommand.decrease(plugin),
			() -> new RandomFallingBlockCommand(plugin),
			() -> new LavaCommand(plugin),
			() -> new LanguageCommand(plugin)
		));

		// entity commands
		Map<String, EntityType<?>> minecraftEntities = new HashMap<>();
		Map<String, EntityType<?>> moddedEntities = new HashMap<>();
		for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
			if (!isWhitelistedEntity(entry.getKey())) continue;
			String id = csIdOf(entry.getKey());
			Map<String, EntityType<?>> entities = entry.getKey().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? minecraftEntities : moddedEntities;
			entities.put(id, entry.getValue());
		}
		for (Map.Entry<String, EntityType<?>> entry : moddedEntities.entrySet()) {
			if (minecraftEntities.containsKey(entry.getKey())) continue;
			minecraftEntities.put(entry.getKey(), entry.getValue());
		}
		for (EntityType<?> entity : minecraftEntities.values()) {
			initTo(commands, () -> new SummonEntityCommand<>(plugin, entity));
			initTo(commands, () -> new RemoveEntityCommand<>(plugin, entity));
		}

		// misc grouped summons
		EntityType<AbstractBoat>[] boats = new EntityType[] {
			EntityType.OAK_BOAT,
			EntityType.BIRCH_BOAT,
			EntityType.ACACIA_BOAT,
			EntityType.CHERRY_BOAT,
			EntityType.DARK_OAK_BOAT,
			EntityType.JUNGLE_BOAT,
			EntityType.MANGROVE_BOAT,
			EntityType.PALE_OAK_BOAT,
			EntityType.SPRUCE_BOAT,
			EntityType.BAMBOO_RAFT
		};
		EntityType<AbstractChestBoat>[] chestBoats = new EntityType[] {
			EntityType.OAK_CHEST_BOAT,
			EntityType.BIRCH_CHEST_BOAT,
			EntityType.ACACIA_CHEST_BOAT,
			EntityType.CHERRY_CHEST_BOAT,
			EntityType.DARK_OAK_CHEST_BOAT,
			EntityType.JUNGLE_CHEST_BOAT,
			EntityType.MANGROVE_CHEST_BOAT,
			EntityType.PALE_OAK_CHEST_BOAT,
			EntityType.SPRUCE_CHEST_BOAT,
			EntityType.BAMBOO_CHEST_RAFT
		};
		initTo(commands, () -> new SummonEntityCommand<>(
			plugin,
			"entity_boat",
			Component.translatable("cc.effect.summon_entity.name", Component.translatable("cc.effect.summon_entity.boat")),
			boats[0],
			Arrays.copyOfRange(boats, 1, boats.length)
		));
		initTo(commands, () -> new SummonEntityCommand<>(
			plugin,
			"entity_chest_boat",
			Component.translatable("cc.effect.summon_entity.name", Component.translatable("cc.effect.summon_entity.chest_boat")),
			chestBoats[0],
			Arrays.copyOfRange(chestBoats, 1, chestBoats.length)
		));
		initTo(commands, () -> new RemoveEntityCommand<>(
			plugin,
			"remove_entity_boat",
			Component.translatable("cc.effect.remove_entity.name", Component.translatable("cc.effect.summon_entity.boat")),
			boats[0],
			Arrays.copyOfRange(boats, 1, boats.length)
		));
		initTo(commands, () -> new RemoveEntityCommand<>(
			plugin,
			"remove_entity_chest_boat",
			Component.translatable("cc.effect.remove_entity.name", Component.translatable("cc.effect.summon_entity.chest_boat")),
			chestBoats[0],
			Arrays.copyOfRange(chestBoats, 1, chestBoats.length)
		));

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values())
			initTo(commands, () -> new DifficultyCommand(plugin, difficulty));

		// potions
		BuiltInRegistries.MOB_EFFECT.listElements()
			.forEach(potion -> initTo(commands, () -> new PotionCommand(plugin, potion)));

		// block sets
		for (Block block : setBlocks)
			initTo(commands, () -> new BlockCommand(plugin, block));

		for (Block block : setFallingBlocks)
			initTo(commands, () -> new FallingBlockCommand(plugin, block));

		// enchantments
		for (Holder<Enchantment> enchantment : plugin.registryHolders(Registries.ENCHANTMENT, null))
			initTo(commands, () -> new EnchantmentCommand(plugin, enchantment));

		// give/take items
		for (Item item : giveTakeItems) {
			initTo(commands, () -> new GiveItemCommand(plugin, item));
			initTo(commands, () -> new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameType gameType : GameType.values()) {
			if (gameType == GameType.SURVIVAL) continue;
			initTo(commands, () -> new GameModeCommand(plugin, gameType, gameType == GameType.SPECTATOR ? 8L : 15L));
		}

//		for (Shader shader : Shader.values()) {
//			initTo(commands, () -> new ShaderCommand(plugin, shader));
//		}
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		plugin.getEventManager().registerListeners(command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getEventManager().registerListeners(new GameModeCommand.Manager());
		plugin.getEventManager().registerListeners(new HealthModifierManager());
		plugin.getEventManager().registerListeners(new FreezeCommand.Manager(plugin));
	}
}
