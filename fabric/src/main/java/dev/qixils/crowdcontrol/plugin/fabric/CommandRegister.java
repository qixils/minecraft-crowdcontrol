package dev.qixils.crowdcontrol.plugin.fabric;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.plugin.fabric.commands.*;
import dev.qixils.crowdcontrol.plugin.fabric.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.fabric.utils.TypedTag;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.Arrays;
import java.util.List;

public class CommandRegister extends AbstractCommandRegister<ServerPlayerEntity, FabricCrowdControlPlugin> {
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
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, Registries.BLOCK);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, Registries.BLOCK);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, Registries.ITEM);
		}
	}

	@Override
	protected void createCommands(List<Command<ServerPlayerEntity>> commands) {
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
				MovementStatusCommand.disableJumping(plugin),
				MovementStatusCommand.invertControls(plugin),
				MovementStatusCommand.invertCamera(plugin),
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
				GravityCommand.maximum(plugin),
				new BiomeCommand(plugin),
				new StructureCommand(plugin),
				new ShaderCommand(plugin, "bumpy", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "green", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "ntsc", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "desaturate", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "flip", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "invert", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "blobs2", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "pencil", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "sobel", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "cc_wobble", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "bits", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "spider", new SemVer(3, 3, 0)),
				new ShaderCommand(plugin, "phosphor", new SemVer(3, 3, 0)),
				new DeleteRandomItemCommand(plugin)
		));

		// entity commands
		for (EntityType<?> entity : Registries.ENTITY_TYPE) {
			commands.add(new SummonEntityCommand<>(plugin, entity));
			commands.add(new RemoveEntityCommand<>(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values())
			commands.add(new DifficultyCommand(plugin, difficulty));

		// potions
		for (StatusEffect potion : Registries.STATUS_EFFECT)
			commands.add(new PotionCommand(plugin, potion));

		// block sets
		for (Block block : setBlocks)
			commands.add(new BlockCommand(plugin, block));

		for (Block block : setFallingBlocks)
			commands.add(new FallingBlockCommand(plugin, block));

		// enchantments
		for (Enchantment enchantment : Registries.ENCHANTMENT)
			commands.add(new EnchantmentCommand(plugin, enchantment));

		// give/take items
		for (Item item : giveTakeItems) {
			commands.add(new GiveItemCommand(plugin, item));
			commands.add(new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameMode gameType : GameMode.values()) {
			if (gameType == GameMode.SURVIVAL) continue;
			commands.add(new GameModeCommand(plugin, gameType, gameType == GameMode.SPECTATOR ? 8L : 15L));
		}
	}

	@Override
	protected void registerListener(Command<ServerPlayerEntity> command) {
		plugin.getEventManager().registerListeners(command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getEventManager().registerListeners(new GameModeCommand.Manager());
		plugin.getEventManager().registerListeners(new MovementStatusCommand.Manager());
		plugin.getEventManager().registerListeners(new HealthModifierManager());
	}
}
