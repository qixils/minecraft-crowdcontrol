package dev.qixils.crowdcontrol.plugin;

import com.google.common.collect.ImmutableSet;
import dev.qixils.crowdcontrol.plugin.commands.*;
import dev.qixils.crowdcontrol.plugin.commands.executeorperish.DoOrDieCommand;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RegisterCommands {
    public static final Set<EntityType> SAFE_ENTITIES = ImmutableSet.of(
            EntityType.CREEPER,
            EntityType.SKELETON,
            EntityType.ZOMBIE,
            EntityType.ZOGLIN,
            EntityType.BAT,
            EntityType.BEE,
            EntityType.BLAZE,
            EntityType.BOAT,
            EntityType.CAT,
            EntityType.CAVE_SPIDER,
            EntityType.COD,
            EntityType.COW,
            EntityType.CHICKEN,
            EntityType.DOLPHIN,
            EntityType.DONKEY,
            EntityType.DROWNED,
            EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.FOX,
            EntityType.GHAST,
            EntityType.GIANT,
            EntityType.GUARDIAN,
            EntityType.HOGLIN,
            EntityType.HORSE,
            EntityType.HUSK,
            EntityType.LIGHTNING,
            EntityType.IRON_GOLEM,
            EntityType.ILLUSIONER,
            EntityType.LLAMA,
            EntityType.MAGMA_CUBE,
            EntityType.MINECART,
            EntityType.MINECART_CHEST,
            EntityType.MINECART_FURNACE,
            EntityType.MINECART_HOPPER,
            EntityType.MINECART_TNT,
            EntityType.PRIMED_TNT,
            EntityType.MULE,
            EntityType.MUSHROOM_COW,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PARROT,
            EntityType.PHANTOM,
            EntityType.PIG,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.POLAR_BEAR,
            EntityType.PUFFERFISH,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.WOLF,
            EntityType.WITHER_SKELETON,
            EntityType.WANDERING_TRADER,
            EntityType.WITCH,
            EntityType.VINDICATOR,
            EntityType.VILLAGER,
            EntityType.VEX,
            EntityType.TURTLE,
            EntityType.TROPICAL_FISH,
            EntityType.TRADER_LLAMA,
            EntityType.STRIDER,
            EntityType.STRAY,
            EntityType.SQUID,
            EntityType.SPIDER,
            EntityType.SNOWMAN,
            EntityType.SLIME,
            EntityType.SILVERFISH,
            EntityType.SKELETON_HORSE,
            EntityType.SHULKER,
            EntityType.SHEEP,
            EntityType.SALMON,
            EntityType.RAVAGER,
            EntityType.RABBIT,
            EntityType.ARMOR_STAND,
            EntityType.AXOLOTL,
            EntityType.GLOW_SQUID,
            EntityType.GOAT,
            EntityType.WITHER,
            EntityType.ENDER_DRAGON
    );

    public static final Set<Material> SET_BLOCKS = ImmutableSet.of(
            Material.TNT,
            Material.FIRE,
            Material.COBWEB,
            Material.REDSTONE_TORCH,
            Material.WITHER_ROSE,
            Material.LIGHTNING_ROD,
            Material.BEDROCK
    );

    public static final Set<Material> SET_FALLING_BLOCKS = ImmutableSet.of(
            Material.ANVIL,
            Material.SAND,
            Material.RED_SAND,
            Material.GRAVEL
    );

    public static final Set<Material> GIVE_TAKE_ITEMS = ImmutableSet.of(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE,
            Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.ENDER_EYE,
            Material.END_PORTAL_FRAME,
            Material.ELYTRA
    );

    public static List<Command> getCommands(CrowdControlPlugin plugin) {
        // register normal commands
        List<Command> commands = new ArrayList<>(Arrays.asList(
                new VeinCommand(plugin),
                new SoundCommand(plugin),
                new ChargedCreeperCommand(plugin),
                new SwapCommand(plugin),
                new DinnerboneCommand(plugin),
                new ClutterCommand(plugin),
                new LootboxCommand(plugin),
                new TeleportCommand(plugin),
                new ToastCommand(plugin),
                new FreezeCommand(plugin),
                new CameraLockCommand(plugin),
                new FlowerCommand(plugin),
                new MoveCommand(plugin, 0, 1, 0, "Up"),
                new MoveCommand(plugin, 0, -2, 0, "Down"),
                new MoveCommand(plugin, 2, 0.2, 0, "xplus", "East"),
                new MoveCommand(plugin, -2, 0.2, 0, "xminus", "West"),
                new MoveCommand(plugin, 0, 0.2, 2, "zplus", "South"),
                new MoveCommand(plugin, 0, 0.2, -2, "zminus", "North"),
                new TorchCommand(plugin, true),
                new TorchCommand(plugin, false),
                new GravelCommand(plugin),
                new DigCommand(plugin),
                new TimeCommand(plugin),
                new ItemDamageCommand(plugin, true),
                new ItemDamageCommand(plugin, false),
                new RemoveEnchantsCommand(plugin),
                new HatCommand(plugin),
                new RespawnCommand(plugin),
                new DropItemCommand(plugin),
                new DeleteItemCommand(plugin),
                new BucketClutchCommand(plugin),
                new DamageCommand(plugin, "kill", "Kill Players", Integer.MAX_VALUE),
                new DamageCommand(plugin, "damage_1", "Damage Players (1 Heart)", 2f),
                new DamageCommand(plugin, "heal_1", "Heal Players (1 Heart)", -2f),
                new DamageCommand(plugin, "full_heal", "Heal Players", -Integer.MAX_VALUE),
                new HalfHealthCommand(plugin),
                new FeedCommand(plugin, "feed", "Feed Players", 40),
                new FeedCommand(plugin, "feed_1", "Feed Players (1 Bar)", 2),
                new FeedCommand(plugin, "starve", "Starve Players", -Integer.MAX_VALUE),
                new FeedCommand(plugin, "starve_1", "Remove One Hunger Bar", -2),
                new ResetExpProgressCommand(plugin),
                new ExperienceCommand(plugin, "xp_plus1", "Give One XP Level", 1),
                new ExperienceCommand(plugin, "xp_sub1", "Take One XP Level", -1),
                new MaxHealthCommand(plugin, -1),
                new MaxHealthCommand(plugin, 1),
                new DisableJumpingCommand(plugin),
                new EntityChaosCommand(plugin),
                new CameraLockToSkyCommand(plugin),
                new CameraLockToGroundCommand(plugin),
                new FlightCommand(plugin),
                new KeepInventoryCommand(plugin, true),
                new KeepInventoryCommand(plugin, false),
                new ClearInventoryCommand(plugin),
                new PlantTreeCommand(plugin),
                new DoOrDieCommand(plugin)
        ));

        // register action bar updater
        Bukkit.getPluginManager().registerEvents(new KeepInventoryCommand.Manager(), plugin);

        // entity commands
        for (EntityType entity : SAFE_ENTITIES) {
            commands.add(new SummonEntityCommand(plugin, entity));
            commands.add(new RemoveEntityCommand(plugin, entity));
        }

        // register difficulty commands
        for (Difficulty difficulty : Difficulty.values()) {
            commands.add(new DifficultyCommand(plugin, difficulty));
        }

        // potions
        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            commands.add(new PotionCommand(plugin, potionEffectType));
        }

        // block sets
        for (Material SET_BLOCK : SET_BLOCKS) {
            commands.add(new BlockCommand(plugin, SET_BLOCK));
        }

        for (Material block : SET_FALLING_BLOCKS) {
            commands.add(new FallingBlockCommand(plugin, block));
        }

        // weather commands
        for (WeatherType weatherType : WeatherType.values()) {
            commands.add(new WeatherCommand(plugin, weatherType));
        }

        // enchantments
        for (Enchantment enchantment : Enchantment.values()) {
            commands.add(new EnchantmentCommand(plugin, enchantment));
        }

        // give/take items
        for (Material item : GIVE_TAKE_ITEMS) {
            commands.add(new GiveItemCommand(plugin, item));
            commands.add(new TakeItemCommand(plugin, item));
        }

        // gamemode commands
        for (GameMode gamemode : GameMode.values()) {
            if (gamemode == GameMode.SURVIVAL) continue;
            commands.add(new GamemodeCommand(plugin, gamemode,
                    gamemode == GameMode.SPECTATOR ? 8L : 15L)); // duration (in seconds)
        }

        return commands;
    }

    public static List<Command> register(CrowdControlPlugin plugin) {
        List<Command> commands = register(plugin, getCommands(plugin));
        for (Command command : commands) {
            if (command instanceof Listener listener)
                Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
        return commands;
    }

    public static List<Command> register(CrowdControlPlugin plugin, List<Command> commands) {
        for (Command cmd : commands) {
            String name = cmd.getEffectName().toLowerCase(java.util.Locale.ENGLISH);
            plugin.registerCommand(name, cmd);
        }

        return commands;
    }

    public static void writeCommands(CrowdControlPlugin plugin, List<Command> commands) {
        try {
            FileWriter fileWriter = new FileWriter("crowdcontrol_commands.txt");
            for (Command command : commands)
                fileWriter.write("        new Effect(\"" + command.getDisplayName() + "\", \"" + command.getEffectName().toLowerCase(Locale.ENGLISH) + "\"),\n");
            fileWriter.close();
        } catch (IOException e) {
            if (plugin != null)
                plugin.getLogger().warning("Failed to write commands to file.");
            e.printStackTrace();
        }
    }
}
