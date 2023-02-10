package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge7.commands.*;
import dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.TypedTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.weather.Weather;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.DAY;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.NIGHT;

public class CommandRegister extends AbstractCommandRegister<Player, SpongeCrowdControlPlugin> {
	private boolean tagsRegistered = false;
	private MappedKeyedTag<BlockType> setBlocks;
	private MappedKeyedTag<BlockType> setFallingBlocks;
	private MappedKeyedTag<ItemType> giveTakeItems;

	public CommandRegister(@NotNull SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	private void registerTags() {
		if (!tagsRegistered) {
			tagsRegistered = true;
			setBlocks = new TypedTag<>(CommandConstants.SET_BLOCKS, plugin.getRegistry(), BlockType.class);
			setFallingBlocks = new TypedTag<>(CommandConstants.SET_FALLING_BLOCKS, plugin.getRegistry(), BlockType.class);
			giveTakeItems = new TypedTag<>(CommandConstants.GIVE_TAKE_ITEMS, plugin.getRegistry(), ItemType.class);
		}
	}

	@Override
	protected void createCommands(List<Command<Player>> commands) {
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
				new FlingCommand(plugin),
				new TorchCommand(plugin, true),
				new TorchCommand(plugin, false),
				new GravelCommand(plugin),
				new DigCommand(plugin),
				new TimeCommand(plugin),
				new ItemRepairCommand(plugin),
				new ItemDamageCommand(plugin),
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
				new ExplodeCommand(plugin),
				new SetTimeCommand(plugin, "time_day", DAY),
				new SetTimeCommand(plugin, "time_night", NIGHT),
				// manual implementation of enchanted gapple commands
				new GiveItemCommand(plugin, ItemStack.builder()
						.itemType(ItemTypes.GOLDEN_APPLE)
						.quantity(1)
						.add(Keys.GOLDEN_APPLE_TYPE, GoldenApples.ENCHANTED_GOLDEN_APPLE)
						.build(),
						"give_enchanted_golden_apple",
						Component.translatable("cc.item.enchanted_golden_apple.name")
				),
				new TakeItemCommand(plugin, ItemTypes.GOLDEN_APPLE, GoldenApples.ENCHANTED_GOLDEN_APPLE),
				GravityCommand.zero(plugin),
				GravityCommand.low(plugin),
				GravityCommand.high(plugin),
				GravityCommand.maximum(plugin)
		));

		// entity commands
		for (EntityType entity : new HashSet<>(plugin.getRegistry().getAllOf(EntityType.class))) { // for whatever reason there are some duplicated entities
			commands.add(new SummonEntityCommand(plugin, entity));
			commands.add(new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : plugin.getRegistry().getAllOf(Difficulty.class)) {
			commands.add(new DifficultyCommand(plugin, difficulty));
		}

		// potions
		for (PotionEffectType potionEffectType : plugin.getRegistry().getAllOf(PotionEffectType.class)) {
			commands.add(new PotionCommand(plugin, potionEffectType));
		}

		// block sets
		for (BlockType block : setBlocks) {
			commands.add(new BlockCommand(plugin, block));
		}
		// cobweb is named differently in 1.12.2 & I'm not refactoring KeyedTag to support fallbacks
		commands.add(new BlockCommand(plugin, BlockTypes.WEB, "block_cobweb"));

		for (BlockType block : setFallingBlocks) {
			commands.add(new FallingBlockCommand(plugin, block));
		}

		// weather commands
		for (Weather weather : plugin.getRegistry().getAllOf(Weather.class)) {
			commands.add(new WeatherCommand(plugin, weather));
		}

		// enchantments
		for (EnchantmentType enchantment : plugin.getRegistry().getAllOf(EnchantmentType.class)) {
			commands.add(new EnchantmentCommand(plugin, enchantment));
		}

		// give/take items
		for (ItemType item : giveTakeItems) {
			commands.add(new GiveItemCommand(plugin, item));
			commands.add(new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameMode gamemode : plugin.getRegistry().getAllOf(GameMode.class)) {
			if (gamemode.equals(GameModes.SURVIVAL))
				continue;
			commands.add(new GameModeCommand(plugin, gamemode,
					gamemode.equals(GameModes.SPECTATOR) ? 8L : 15L)); // duration (in seconds)
		}
	}

	@Override
	protected void registerListener(Command<Player> command) {
		plugin.getGame().getEventManager().registerListeners(plugin, command);
	}

	@Override
	protected void onFirstRegistry() {
		plugin.getGame().getEventManager().registerListeners(plugin, new KeepInventoryCommand.Manager());
		plugin.getGame().getEventManager().registerListeners(plugin, new GameModeCommand.Manager());
		plugin.getGame().getEventManager().registerListeners(plugin, new HealthModifierManager());
	}
}
