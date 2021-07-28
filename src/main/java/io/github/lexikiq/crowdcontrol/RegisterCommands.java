package io.github.lexikiq.crowdcontrol;

import com.google.common.collect.ImmutableSet;
import io.github.lexikiq.crowdcontrol.commands.*;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
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

    public static void register(CrowdControlPlugin plugin) {
        // register normal commands
        Set<ChatCommand> commands = new HashSet<>(Set.of(
                new VeinCommand(plugin),
                new SoundCommand(plugin),
                new ChargedCreeperCommand(plugin),
                new GiveItem(plugin),
                new TakeItem(plugin),
                new SwapCommand(plugin),
                new DinnerboneCommand(plugin),
                new ClutterCommand(plugin),
                new LootboxCommand(plugin),
                new TeleportCommand(plugin),
                new ToastCommand(plugin),
                new FreezeCommand(plugin),
                new FlowerCommand(plugin),
                new ParticleCommand(plugin),
                new MoveCommand(plugin, 0, 1, 0, "up"),
                new MoveCommand(plugin, 0, -1, 0, "down"),
                new MoveCommand(plugin, 1, 0, 0, "x+"),
                new MoveCommand(plugin, -1, 0, 0, "x-"),
                new MoveCommand(plugin, 0, 0, 1, "z+"),
                new MoveCommand(plugin, 0, 0, -1, "z-"),
                new TorchCommand(plugin, true),
                new TorchCommand(plugin, false),
                new GravelCommand(plugin),
                new DigCommand(plugin),
                new TimeCommand(plugin),
                new NameCommand(plugin)
        ));

        // register entity commands
        for (EntityType entity : SAFE_ENTITIES) {
            commands.add(new SummonEntityCommand(plugin, entity));
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
        for (Material material : SET_BLOCKS) {
            commands.add(new BlockCommand(plugin, material));
        }
        for (Material material : SET_FALLING_BLOCKS) {
            commands.add(new FallingBlockCommand(plugin, material));
        }

        // wwwwwwww
        for (WeatherType weatherType : WeatherType.values()) {
            commands.add(new WeatherCommand(plugin, weatherType));
        }

        for (Enchantment enchantment : Enchantment.values()) {
            commands.add(new EnchantmentCommand(plugin, enchantment));
        }

        // generate template commands.txt
//        FileWriter fileWriter = null;
//        try {
//            fileWriter = new FileWriter("crowdcontrol_commands.txt");
//        } catch (IOException e) {
//            plugin.getLogger().warning("Failed to write commands to file.");
//            e.printStackTrace();
//        }

        // actually register the commands
        for (ChatCommand cmd : commands) {
            String name = cmd.getCommand().toLowerCase(java.util.Locale.ENGLISH);
            try {
                plugin.registerCommand(name, cmd);
//                if (fileWriter != null) {
//                    try {
//                        fileWriter.write("!" + name + "\n");
//                    } catch (IOException e) {
//                        plugin.getLogger().warning("Failed to save command "+name+".");
//                        e.printStackTrace();
//                    }
//                }
            } catch (AlreadyRegisteredException e) {
                plugin.getLogger().warning("Tried to register Twitch command '"+name+"' but it was already registered.");
            }
        }

        // groan
//        if (fileWriter != null) {
//            try {
//                fileWriter.close();
//            } catch (IOException e) {
//                plugin.getLogger().warning("Failed to close file???");
//                e.printStackTrace();
//            }
//        }
    }
}
