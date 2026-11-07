package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.custom.CustomCommandData;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.fabric.commands.*;
import dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
	public boolean isReady() {
		return plugin.getServer() != null && plugin.getServer().overworld() != null;
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
			() -> new BiomeCommand(plugin),
			() -> new StructureCommand(plugin),
			() -> new DeleteRandomItemCommand(plugin),
			() -> new UniteCommand(plugin),
			() -> new RandomFallingBlockCommand(plugin),
			() -> new LavaCommand(plugin),
			() -> new WaterCommand(plugin),
			() -> new DrainCommand(plugin)
		));

		// entity commands
		// boats are a single entity type on this version; the wood type is randomized when spawned
		EntityType<Boat>[] boats = new EntityType[] {
			EntityType.BOAT
		};
		EntityType<ChestBoat>[] chestBoats = new EntityType[] {
			EntityType.CHEST_BOAT
		};
		// TODO: could bring back the fill-in-vanilla-holes thing to have some better defaults for some modded mobs?
		//  but there will probably be overlap and make it weird idk
		for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
			try {
				if (!entry.getValue().isEnabled(plugin.server().overworld().enabledFeatures())) continue;
				if (Stream.concat(Arrays.stream(boats), Arrays.stream(chestBoats)).anyMatch(boat -> boat == entry.getValue())) continue;

				boolean allowed = isWhitelistedEntity(FabricAudiences.toAdventure(entry.getKey().location())) || entry.getValue().create(plugin.server().overworld()) instanceof Mob;
				if (!allowed) continue;
				initTo(commands, () -> new SummonEntityCommand<>(plugin, entry.getValue()));

				if (entry.getValue().equals(EntityType.LIGHTNING_BOLT)) continue;
				if (entry.getValue().equals(EntityType.TNT)) continue;
				initTo(commands, () -> new RemoveEntityCommand<>(plugin, entry.getValue()));
			} catch (Throwable e) {
				plugin.getSLF4JLogger().warn("Failed to check if entity is allowed; ignoring", e);
			}
		}

		// boat effects
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
		BuiltInRegistries.MOB_EFFECT.forEach(potion -> {
			if (potion == MobEffects.LUCK) return; // unused
			if (potion == MobEffects.UNLUCK) return; // unused
			// if (potion.is(MobEffects.JUMP)) return; // disabled in favor of gravity -- which doesnt exist in this version
			if (potion == MobEffects.HUNGER) return; // disabled in favor of take food
			if (potion == MobEffects.SATURATION) return; // disabled in favor of give food
			if (potion == MobEffects.HARM) return; // disabled in favor of take health
			if (potion == MobEffects.HEAL) return; // disabled in favor of give health
			if (potion == MobEffects.WITHER) return; // we tend to avoid effects that can kill
			initTo(commands, () -> new PotionCommand(plugin, potion));
		});

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

//		for (Shader shader : Shader.values()) {
//			initTo(commands, () -> new ShaderCommand(plugin, shader));
//		}

		if (plugin.getCustomEffectsConfig().effects() != null) {
			for (CustomCommandData data : plugin.getCustomEffectsConfig().effects()) {
				initTo(commands, () -> new CustomCommandsCommand(plugin, data));
			}
		}
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
