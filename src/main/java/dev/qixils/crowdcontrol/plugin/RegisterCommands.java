package dev.qixils.crowdcontrol.plugin;

import com.google.common.collect.ImmutableSet;
import dev.qixils.crowdcontrol.plugin.commands.BlockCommand;
import dev.qixils.crowdcontrol.plugin.commands.BucketClutchCommand;
import dev.qixils.crowdcontrol.plugin.commands.CameraLockCommand;
import dev.qixils.crowdcontrol.plugin.commands.ChargedCreeperCommand;
import dev.qixils.crowdcontrol.plugin.commands.ClutterCommand;
import dev.qixils.crowdcontrol.plugin.commands.DamageCommand;
import dev.qixils.crowdcontrol.plugin.commands.DeleteItemCommand;
import dev.qixils.crowdcontrol.plugin.commands.DifficultyCommand;
import dev.qixils.crowdcontrol.plugin.commands.DigCommand;
import dev.qixils.crowdcontrol.plugin.commands.DinnerboneCommand;
import dev.qixils.crowdcontrol.plugin.commands.DisableJumpingCommand;
import dev.qixils.crowdcontrol.plugin.commands.DropItemCommand;
import dev.qixils.crowdcontrol.plugin.commands.EnchantmentCommand;
import dev.qixils.crowdcontrol.plugin.commands.ExperienceCommand;
import dev.qixils.crowdcontrol.plugin.commands.FallingBlockCommand;
import dev.qixils.crowdcontrol.plugin.commands.FeedCommand;
import dev.qixils.crowdcontrol.plugin.commands.FlowerCommand;
import dev.qixils.crowdcontrol.plugin.commands.FreezeCommand;
import dev.qixils.crowdcontrol.plugin.commands.GiveItemCommand;
import dev.qixils.crowdcontrol.plugin.commands.GravelCommand;
import dev.qixils.crowdcontrol.plugin.commands.HalfHealthCommand;
import dev.qixils.crowdcontrol.plugin.commands.HatCommand;
import dev.qixils.crowdcontrol.plugin.commands.ItemDamageCommand;
import dev.qixils.crowdcontrol.plugin.commands.LootboxCommand;
import dev.qixils.crowdcontrol.plugin.commands.MaxHealthCommand;
import dev.qixils.crowdcontrol.plugin.commands.MoveCommand;
import dev.qixils.crowdcontrol.plugin.commands.PotionCommand;
import dev.qixils.crowdcontrol.plugin.commands.RemoveEnchantsCommand;
import dev.qixils.crowdcontrol.plugin.commands.RemoveEntityCommand;
import dev.qixils.crowdcontrol.plugin.commands.ResetExpProgressCommand;
import dev.qixils.crowdcontrol.plugin.commands.RespawnCommand;
import dev.qixils.crowdcontrol.plugin.commands.SoundCommand;
import dev.qixils.crowdcontrol.plugin.commands.SummonEntityCommand;
import dev.qixils.crowdcontrol.plugin.commands.SwapCommand;
import dev.qixils.crowdcontrol.plugin.commands.TakeItemCommand;
import dev.qixils.crowdcontrol.plugin.commands.TeleportCommand;
import dev.qixils.crowdcontrol.plugin.commands.TimeCommand;
import dev.qixils.crowdcontrol.plugin.commands.ToastCommand;
import dev.qixils.crowdcontrol.plugin.commands.TorchCommand;
import dev.qixils.crowdcontrol.plugin.commands.VeinCommand;
import dev.qixils.crowdcontrol.plugin.commands.WeatherCommand;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
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
            EntityType.GOAT
    );

    public static final Set<Material> SET_BLOCKS = ImmutableSet.of(
            Material.TNT,
            Material.FIRE,
            Material.COBWEB,
            Material.REDSTONE_TORCH,
            Material.WITHER_ROSE
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
            Material.ENCHANTED_GOLDEN_APPLE
    );

    public static List<Command> register(CrowdControlPlugin plugin) {
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
                new MoveCommand(plugin, 0, 2, 0, "Up"),
                new MoveCommand(plugin, 0, -2, 0, "Down"),
                new MoveCommand(plugin, 2, 0, 0, "xplus", "X+"),
                new MoveCommand(plugin, -2, 0, 0, "xminus", "X-"),
                new MoveCommand(plugin, 0, 0, 2, "zplus", "Z+"),
                new MoveCommand(plugin, 0, 0, -2, "zminus", "Z-"),
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
                new DisableJumpingCommand(plugin)
        ));

        SAFE_ENTITIES.forEach(entity -> {
            commands.add(new SummonEntityCommand(plugin, entity));
            commands.add(new RemoveEntityCommand(plugin, entity));
        });

        // register difficulty commands
        for (Difficulty difficulty : Difficulty.values()) {
            commands.add(new DifficultyCommand(plugin, difficulty));
        }

        // potions
        for (PotionEffectType potionEffectType : PotionEffectType.values()) {
            commands.add(new PotionCommand(plugin, potionEffectType));
        }

        // block sets
        SET_BLOCKS.forEach(block -> commands.add(new BlockCommand(plugin, block)));
        SET_FALLING_BLOCKS.forEach(block -> commands.add(new FallingBlockCommand(plugin, block)));

        // weather commands
        for (WeatherType weatherType : WeatherType.values()) {
            commands.add(new WeatherCommand(plugin, weatherType));
        }

        // enchantments
        for (Enchantment enchantment : Enchantment.values()) {
            commands.add(new EnchantmentCommand(plugin, enchantment));
        }

        GIVE_TAKE_ITEMS.forEach(item -> {
            commands.add(new GiveItemCommand(plugin, item));
            commands.add(new TakeItemCommand(plugin, item));
        });

        // actually register the commands
        for (Command cmd : commands) {
            String name = cmd.getEffectName().toLowerCase(java.util.Locale.ENGLISH);
            plugin.registerCommand(name, cmd);
            if (cmd instanceof Listener listener)
                Bukkit.getPluginManager().registerEvents(listener, plugin);
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
            plugin.getLogger().warning("Failed to write commands to file.");
            e.printStackTrace();
        }
    }
}
