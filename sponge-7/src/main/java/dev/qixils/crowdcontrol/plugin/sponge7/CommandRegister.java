package dev.qixils.crowdcontrol.plugin.sponge7;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.MappedKeyedTag;
import dev.qixils.crowdcontrol.plugin.sponge7.commands.*;
import dev.qixils.crowdcontrol.plugin.sponge7.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.TypedTag;
import net.kyori.adventure.key.Key;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
		commands.addAll(this.<Command<Player>>initAll(
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
			// TODO: broken -- new RespawnCommand(plugin),
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
			// manual implementation of enchanted gapple commands
			() -> new GiveItemCommand(plugin, ItemStack.builder()
				.itemType(ItemTypes.GOLDEN_APPLE)
				.quantity(1)
				.add(Keys.GOLDEN_APPLE_TYPE, GoldenApples.ENCHANTED_GOLDEN_APPLE)
				.build(),
				"give_enchanted_golden_apple",
				Component.translatable("cc.item.enchanted_golden_apple.name")
			),
			() -> new TakeItemCommand(plugin, ItemTypes.GOLDEN_APPLE, GoldenApples.ENCHANTED_GOLDEN_APPLE),
			() -> GravityCommand.zero(plugin),
			() -> GravityCommand.low(plugin),
			() -> GravityCommand.high(plugin),
			() -> GravityCommand.maximum(plugin),
			() -> new DeleteRandomItemCommand(plugin)
		));

		// entity commands
		Map<String, EntityType> minecraftEntities = new HashMap<>();
		Map<String, EntityType> moddedEntities = new HashMap<>();
		for (EntityType entity : new HashSet<>(plugin.getRegistry().getAllOf(EntityType.class))) {
			Key key = SpongeTextUtil.asKey(entity);
			if (key == null) continue;
			Map<String, EntityType> entities = key.namespace().equals(Key.MINECRAFT_NAMESPACE) ? minecraftEntities : moddedEntities;
			String id = SpongeTextUtil.csIdOf(entity);
			entities.put(id, entity);
		}
		for (Map.Entry<String, EntityType> entry : moddedEntities.entrySet()) {
			if (minecraftEntities.containsKey(entry.getKey())) continue;
			minecraftEntities.put(entry.getKey(), entry.getValue());
		}
		for (EntityType entity : minecraftEntities.values()) {
			initTo(commands, () -> new SummonEntityCommand(plugin, entity));
			initTo(commands, () -> new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : plugin.getRegistry().getAllOf(Difficulty.class)) {
			initTo(commands, () -> new DifficultyCommand(plugin, difficulty));
		}

		// potions
		for (PotionEffectType potionEffectType : plugin.getRegistry().getAllOf(PotionEffectType.class)) {
			initTo(commands, () -> new PotionCommand(plugin, potionEffectType));
		}

		// block sets
		for (BlockType block : setBlocks) {
			initTo(commands, () -> new BlockCommand(plugin, block));
		}
		// cobweb is named differently in 1.12.2 & I'm not refactoring KeyedTag to support fallbacks
		initTo(commands, () -> new BlockCommand(plugin, BlockTypes.WEB, "block_cobweb"));

		for (BlockType block : setFallingBlocks) {
			initTo(commands, () -> new FallingBlockCommand(plugin, block));
		}

		// weather commands
		for (Weather weather : plugin.getRegistry().getAllOf(Weather.class)) {
			initTo(commands, () -> new WeatherCommand(plugin, weather));
		}

		// enchantments
		for (EnchantmentType enchantment : plugin.getRegistry().getAllOf(EnchantmentType.class)) {
			initTo(commands, () -> new EnchantmentCommand(plugin, enchantment));
		}

		// give/take items
		for (ItemType item : giveTakeItems) {
			initTo(commands, () -> new GiveItemCommand(plugin, item));
			initTo(commands, () -> new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameMode gamemode : plugin.getRegistry().getAllOf(GameMode.class)) {
			if (gamemode.equals(GameModes.SURVIVAL))
				continue;
			initTo(commands, () -> new GameModeCommand(plugin, gamemode, gamemode.equals(GameModes.SPECTATOR) ? 8L : 15L));
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
