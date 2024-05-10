package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.commands.*;
import dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.plugin.fabric.Command.csIdOf;

public class CommandRegister extends AbstractCommandRegister<ServerPlayer, FabricCrowdControlPlugin> {
	private boolean tagsRegistered = false;
	private MappedKeyedTag<Block> setBlocks;
	private MappedKeyedTag<Block> setFallingBlocks;
	private MappedKeyedTag<Item> giveTakeItems;

	public CommandRegister(FabricCrowdControlPlugin plugin) {
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
			() -> new ShaderCommand(plugin, "bumpy", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "green", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "ntsc", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "desaturate", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "flip", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "invert", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "blobs2", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "pencil", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "sobel", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "cc_wobble", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "bits", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "spider", new SemVer(3, 3, 0)),
			() -> new ShaderCommand(plugin, "phosphor", new SemVer(3, 3, 0)),
			() -> new DeleteRandomItemCommand(plugin),
			() -> new UniteCommand(plugin),
			() -> TickRateCommand.doubleRate(plugin),
			() -> TickRateCommand.halfRate(plugin),
			() -> new TickFreezeCommand(plugin)
		));

		// entity commands
		Map<String, EntityType<?>> minecraftEntities = new HashMap<>();
		Map<String, EntityType<?>> moddedEntities = new HashMap<>();
		for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
			String id = csIdOf(entry.getKey().location());
			if (!CommandConstants.ENTITIES.contains(id)) continue;
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

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values())
			initTo(commands, () -> new DifficultyCommand(plugin, difficulty));

		// potions
		BuiltInRegistries.MOB_EFFECT.holders()
			.forEach(potion -> initTo(commands, () -> new PotionCommand(plugin, potion)));

		// block sets
		for (Block block : setBlocks)
			initTo(commands, () -> new BlockCommand(plugin, block));

		for (Block block : setFallingBlocks)
			initTo(commands, () -> new FallingBlockCommand(plugin, block));

		// enchantments
		for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT)
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
	}

	@Override
	protected void registerListener(Command<ServerPlayer> command) {
		plugin.getEventManager().registerListeners(command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getEventManager().registerListeners(new GameModeCommand.Manager());
		plugin.getEventManager().registerListeners(new MovementStatusCommand.Manager());
		plugin.getEventManager().registerListeners(new HealthModifierManager());
	}
}
