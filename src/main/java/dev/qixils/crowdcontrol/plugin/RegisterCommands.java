package dev.qixils.crowdcontrol.plugin;

import com.google.common.collect.ImmutableSet;
import dev.qixils.crowdcontrol.plugin.commands.*;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
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
            EntityType.ARMOR_STAND
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
                new MoveCommand(plugin, 0, 1, 0, "Up"),
                new MoveCommand(plugin, 0, -1, 0, "Down"),
                new MoveCommand(plugin, 1, 0, 0, "X+"),
                new MoveCommand(plugin, -1, 0, 0, "X-"),
                new MoveCommand(plugin, 0, 0, 1, "Z+"),
                new MoveCommand(plugin, 0, 0, -1, "Z-"),
                new TorchCommand(plugin, true),
                new TorchCommand(plugin, false),
                new GravelCommand(plugin),
                new DigCommand(plugin),
                new TimeCommand(plugin),
                new NameCommand(plugin),
                new ItemDamageCommand(plugin, true),
                new ItemDamageCommand(plugin, false)
        ));

        SAFE_ENTITIES.forEach(entity -> commands.add(new SummonEntityCommand(plugin, entity)));

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
