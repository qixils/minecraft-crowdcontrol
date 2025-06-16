package dev.qixils.crowdcontrol.plugin.paper;

import dev.qixils.crowdcontrol.common.command.AbstractCommandRegister;
import dev.qixils.crowdcontrol.common.command.Command;
import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.paper.commands.*;
import dev.qixils.crowdcontrol.plugin.paper.commands.executeorperish.DoOrDieCommand;
import dev.qixils.crowdcontrol.plugin.paper.utils.MaterialTag;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.CollectionUtil.initTo;

public class CommandRegister extends AbstractCommandRegister<Player, PaperCrowdControlPlugin> {
	private static final MaterialTag SET_BLOCKS = new MaterialTag(CommandConstants.SET_BLOCKS);
	private static final MaterialTag SET_FALLING_BLOCKS = new MaterialTag(CommandConstants.SET_FALLING_BLOCKS);
	private static final MaterialTag GIVE_TAKE_ITEMS = new MaterialTag(CommandConstants.GIVE_TAKE_ITEMS);

	protected CommandRegister(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void createCommands(List<Command<Player>> commands) {
		super.createCommands(commands);
		commands.addAll(this.<Command<Player>>initAll(
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
			() -> new FlowerCommand(plugin),
			() -> new FlingCommand(plugin),
			() -> new TorchCommand(plugin, true),
			() -> new TorchCommand(plugin, false),
			() -> new GravelCommand(plugin),
			() -> new DigCommand(plugin),
			() -> new ItemDamageCommand(plugin, true),
			() -> new ItemDamageCommand(plugin, false),
			() -> new RemoveEnchantsCommand(plugin),
			() -> new HatCommand(plugin),
			() -> new RespawnCommand(plugin),
			() -> new DropItemCommand(plugin),
			() -> new DeleteItemCommand(plugin),
			() -> new BucketClutchCommand(plugin),
			() -> MovementStatusCommand.disableJumping(plugin),
			() -> MovementStatusCommand.invertCamera(plugin),
			() -> MovementStatusCommand.invertControls(plugin),
			() -> FreezeCommand.feet(plugin),
			() -> FreezeCommand.camera(plugin),
			() -> FreezeCommand.skyCamera(plugin),
			() -> FreezeCommand.groundCamera(plugin),
			() -> new EntityChaosCommand(plugin),
			() -> new FlightCommand(plugin),
			() -> new KeepInventoryCommand(plugin, true),
			() -> new KeepInventoryCommand(plugin, false),
			() -> new ClearInventoryCommand(plugin),
			() -> new PlantTreeCommand(plugin),
			() -> new DoOrDieCommand(plugin),
			() -> new ClearWeatherCommand(plugin),
			() -> new RainyWeatherCommand(plugin),
			() -> new ThunderingWeatherCommand(plugin),
			() -> new StructureCommand(plugin),
			() -> new BiomeCommand(plugin),
			() -> new ExplodeCommand(plugin),
			() -> new SetTimeCommand(plugin, "time_day", DAY),
			() -> new SetTimeCommand(plugin, "time_night", NIGHT),
			() -> GravityCommand.zero(plugin),
			() -> GravityCommand.low(plugin),
			() -> GravityCommand.high(plugin),
			() -> GravityCommand.maximum(plugin),
			() -> new DeleteRandomItemCommand(plugin),
			() -> new UniteCommand(plugin),
			() -> new TickFreezeCommand(plugin),
			() -> TickRateCommand.doubleRate(plugin),
			() -> TickRateCommand.halfRate(plugin),
			() -> PlayerSizeCommand.increase(plugin),
			() -> PlayerSizeCommand.decrease(plugin),
			() -> EntitySizeCommand.increase(plugin),
			() -> EntitySizeCommand.decrease(plugin),
			() -> new RandomFallingBlockCommand(plugin),
			() -> new LavaCommand(plugin),
			() -> new LanguageCommand(plugin)
		));

		// entity commands
		for (EntityType entity : Registry.ENTITY_TYPE) {
			if (!isWhitelistedEntity(entity)) continue;
			initTo(commands, () -> new SummonEntityCommand(plugin, entity));
			initTo(commands, () -> new RemoveEntityCommand(plugin, entity));
		}

		// register difficulty commands
		for (Difficulty difficulty : Difficulty.values()) {
			initTo(commands, () -> new DifficultyCommand(plugin, difficulty));
		}

		// potions
		for (PotionEffectType potionEffectType : Registry.POTION_EFFECT_TYPE) {
			initTo(commands, () -> new PotionCommand(plugin, potionEffectType));
		}

		// block sets
		for (Material block : SET_BLOCKS) {
			initTo(commands, () -> new BlockCommand(plugin, block));
		}

		for (Material block : SET_FALLING_BLOCKS) {
			initTo(commands, () -> new FallingBlockCommand(plugin, block));
		}

		// enchantments
		for (Enchantment enchantment : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)) {
			initTo(commands, () -> new EnchantmentCommand(plugin, enchantment), e -> plugin.getSLF4JLogger().warn("Enchantment {} does not implement the Adventure/Paper API. Ignoring.", enchantment.getKey()));
		}

		// give/take items
		for (Material item : GIVE_TAKE_ITEMS) {
			initTo(commands, () -> new GiveItemCommand(plugin, item));
			initTo(commands, () -> new TakeItemCommand(plugin, item));
		}

		// gamemode commands
		for (GameMode gamemode : GameMode.values()) {
			if (gamemode == GameMode.SURVIVAL) continue;
			initTo(commands, () -> new GameModeCommand(plugin, gamemode, gamemode == GameMode.SPECTATOR ? 8L : 15L));
		}

//		for (Shader shader : Shader.values()) {
//			initTo(commands, () -> new ShaderCommand(plugin, shader));
//		}
	}

	@Override
	protected void registerListener(Command<Player> command) {
		if (command instanceof Listener listener)
			Bukkit.getPluginManager().registerEvents(listener, plugin.getPaperPlugin());
		else
			plugin.getSLF4JLogger().warn("Could not register listener for command {}", command.getEffectName());
	}

	@Override
	protected void onFirstRegistry() {
		PaperLoader paperPlugin = plugin.getPaperPlugin();
		Bukkit.getPluginManager().registerEvents(new KeepInventoryCommand.Manager(), paperPlugin);
		Bukkit.getPluginManager().registerEvents(new MovementStatusCommand.Manager(), paperPlugin);
		Bukkit.getPluginManager().registerEvents(new LootboxCommand.Manager(plugin), paperPlugin);
		Bukkit.getPluginManager().registerEvents(new FreezeCommand.Manager(plugin), paperPlugin);
		Bukkit.getPluginManager().registerEvents(new GameModeCommand.Manager(paperPlugin), paperPlugin);
		Bukkit.getPluginManager().registerEvents(new HealthModifierManager(), paperPlugin);
	}
}
