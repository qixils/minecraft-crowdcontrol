package io.github.lexikiq.crowdcontrol;

import io.github.lexikiq.crowdcontrol.commands.*;
import org.bukkit.Difficulty;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class RegisterCommands {
    public static final Set<EntityType> SAFE_ENTITIES = Set.of(
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

    public static void register(CrowdControl plugin) {
        // register normal commands
        Set<ChatCommand> commands = new HashSet<>(Set.of(
                new VeinCommand(plugin),
                new SoundCommand(plugin),
                new ChargedCreeperCommand(plugin),
                new FireCommand(plugin),
                new GiveItem(plugin),
                new TakeItem(plugin),
                new SwapCommand(plugin),
                new DinnerboneCommand(plugin)
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

        // actually register the commands
        for (ChatCommand cmd : commands) {
            String name = cmd.getCommand().toLowerCase(java.util.Locale.ENGLISH);
            try {
                plugin.registerCommand(name, cmd);
            } catch (AlreadyRegisteredException e) {
                plugin.getLogger().warning("Tried to register Twitch command '"+name+"' but it was already registered.");
            }
        }
    }
}
