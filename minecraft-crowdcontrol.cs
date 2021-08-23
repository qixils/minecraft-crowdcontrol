using System;
using System.Collections.Generic;
using CrowdControl.Common;
using CrowdControl.Games.Packs;
using ConnectorType = CrowdControl.Common.ConnectorType;

public class Minecraft : SimpleTCPPack
{
    public override string Host => "127.0.0.1";

    public override ushort Port => 58431;

    public Minecraft(IPlayer player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler) : base(player, responseHandler, statusUpdateHandler) { }

    public override Game Game => new Game(53, "Minecraft", "minecraft", "PC", ConnectorType.SimpleTCPConnector);

    public override List<Effect> Effects => new List<Effect>
    {
        // miscellaneous
        new Effect("Spawn Ore Vein", "vein"),
        new Effect("Spooky Sound Effect", "sound-effect"),
        new Effect("Swap Locations", "swap"), // swaps the locations of every online player
        new Effect("Dinnerbone", "dinnerbone"), // flips nearby mobs upside-down
        new Effect("Clutter Inventories", "clutter"), // shuffles around a couple items in everyone's inventory
        new Effect("Open Lootbox", "lootbox"), // gives a completely random item with varying enchants and modifiers
        new Effect("Eat Chorus Fruit", "chorus-fruit"), // teleports the player to a random nearby block as if they ate a Chorus Fruit
        new Effect("Render Toasts", "toast"), // displays a bunch of "Recipe Unlocked" notifications in the top right
        new Effect("Freeze", "freeze"), // locks everyone in place for 7 seconds (camera rotation allowed)
        new Effect("Camera Lock", "camera-lock"), // locks everyone's camera in place for 7 seconds (movement allowed)
        new Effect("Place Flowers", "flowers"), // places a bunch of flowers nearby as if a Bonemeal item was used
        new Effect("Place Torches", "lit"), // places torches on every valid nearby block
        new Effect("Remove Torches", "dim"), // removes nearby torches
        new Effect("Replace Area With Gravel", "gravel-hell"), // replaces nearby stone blocks with gravel
        new Effect("Dig Hole", "dig"), // creates a small hole underneath every player
        new Effect("Zip Time", "zip"), // adds a minute to the in-game day/night cycle
        new Effect("Name Item", "name-item"), // names a held item after the viewer
        new Effect("Repair Item", "repair-item"), // sets the durability of an item to 100%
        new Effect("Damage Item", "damage-item"), // sets the durability of an item to 50%
        new Effect("Put Item on Head", "hat"), // swaps the held item and the player's head item
        new Effect("Respawn Players", "respawn"),
        new Effect("Drop Held Item", "drop-item"),
        new Effect("Delete Held Item", "delete-item"),
        new Effect("Water Bucket Clutch", "bucket-clutch"), // teleports players 30 blocks up and gives them a water bucket
        new Effect("Kill Players", "kill"),
        new Effect("Damage Players (1 Heart)", "damage-1"),
        new Effect("Heal Players (1 Heart)", "heal-1"),
        new Effect("Heal Players", "full-heal"),
        // TODO: give/take XP
        // TODO: give/take food
        // TODO: increase/decrease max health
        // TODO: disable jumping for ~10 seconds
        // teleports players by a few blocks in the specified direction
        new Effect("Move Up", "up"),
        new Effect("Move Down", "down"),
        new Effect("Move X+", "x+"),
        new Effect("Move X-", "x-"),
        new Effect("Move Z+", "z+"),
        new Effect("Move Z-", "z-"),
        // summons a mob around each player
        new Effect("Summon Charged Creeper", "entity-charged-creeper"),
        new Effect("Summon Creeper", "entity-creeper"),
        new Effect("Summon Skeleton", "entity-skeleton"),
        new Effect("Summon Zombie", "entity-zombie"),
        new Effect("Summon Zoglin", "entity-zoglin"),
        new Effect("Summon Bat", "entity-bat"),
        new Effect("Summon Bee", "entity-bee"),
        new Effect("Summon Blaze", "entity-blaze"),
        new Effect("Summon Boat", "entity-boat"),
        new Effect("Summon Cat", "entity-cat"),
        new Effect("Summon Cave Spider", "entity-cave_spider"),
        new Effect("Summon Cod", "entity-cod"),
        new Effect("Summon Cow", "entity-cow"),
        new Effect("Summon Chicken", "entity-chicken"),
        new Effect("Summon Dolphin", "entity-dolphin"),
        new Effect("Summon Donkey", "entity-donkey"),
        new Effect("Summon Drowned", "entity-drowned"),
        new Effect("Summon Elder Guardian", "entity-elder_guardian"),
        new Effect("Summon Enderman", "entity-enderman"),
        new Effect("Summon Endermite", "entity-endermite"),
        new Effect("Summon Evoker", "entity-evoker"),
        new Effect("Summon Fox", "entity-fox"),
        new Effect("Summon Ghast", "entity-ghast"),
        new Effect("Summon Giant", "entity-giant"),
        new Effect("Summon Guardian", "entity-guardian"),
        new Effect("Summon Hoglin", "entity-hoglin"),
        new Effect("Summon Horse", "entity-horse"),
        new Effect("Summon Husk", "entity-husk"),
        new Effect("Summon Lightning Bolt", "entity-lightning"),
        new Effect("Summon Iron Golem", "entity-iron_golem"),
        new Effect("Summon Illusioner", "entity-illusioner"),
        new Effect("Summon Llama", "entity-llama"),
        new Effect("Summon Magma Cube", "entity-magma_cube"),
        new Effect("Summon Minecart", "entity-minecart"),
        new Effect("Summon Minecart with Chest", "entity-minecart_chest"),
        new Effect("Summon Minecart with Furnace", "entity-minecart_furnace"),
        new Effect("Summon Minecart with Hopper", "entity-minecart_hopper"),
        new Effect("Summon Minecart with TNT", "entity-minecart_tnt"),
        new Effect("Summon Primed TNT", "entity-primed_tnt"),
        new Effect("Summon Mule", "entity-mule"),
        new Effect("Summon Mooshroom", "entity-mushroom_cow"),
        new Effect("Summon Ocelot", "entity-ocelot"),
        new Effect("Summon Panda", "entity-panda"),
        new Effect("Summon Parrot", "entity-parrot"),
        new Effect("Summon Phantom", "entity-phantom"),
        new Effect("Summon Pig", "entity-pig"),
        new Effect("Summon Piglin", "entity-piglin"),
        new Effect("Summon Piglin Brute", "entity-piglin_brute"),
        new Effect("Summon Pillager", "entity-pillager"),
        new Effect("Summon Polar Bear", "entity-polar_bear"),
        new Effect("Summon Pufferfish", "entity-pufferfish"),
        new Effect("Summon Zombified Piglin", "entity-zombified_piglin"),
        new Effect("Summon Zombie Horse", "entity-zombie_horse"),
        new Effect("Summon Zombie Villager", "entity-zombie_villager"),
        new Effect("Summon Wolf", "entity-wolf"),
        new Effect("Summon Wither Skeleton", "entity-wither_skeleton"),
        new Effect("Summon Wandering Trader", "entity-wandering_trader"),
        new Effect("Summon Witch", "entity-witch"),
        new Effect("Summon Vindicator", "entity-vindicator"),
        new Effect("Summon Villager", "entity-villager"),
        new Effect("Summon Vex", "entity-vex"),
        new Effect("Summon Turtle", "entity-turtle"),
        new Effect("Summon Tropical Fish", "entity-tropical_fish"),
        new Effect("Summon Trader Llama", "entity-trader_llama"),
        new Effect("Summon Strider", "entity-strider"),
        new Effect("Summon Stray", "entity-stray"),
        new Effect("Summon Squid", "entity-squid"),
        new Effect("Summon Spider", "entity-spider"),
        new Effect("Summon Snow Golem", "entity-snowman"),
        new Effect("Summon Slime", "entity-slime"),
        new Effect("Summon Silverfish", "entity-silverfish"),
        new Effect("Summon Skeleton Horse", "entity-skeleton_horse"),
        new Effect("Summon Shulker", "entity-shulker"),
        new Effect("Summon Sheep", "entity-sheep"),
        new Effect("Summon Salmon", "entity-salmon"),
        new Effect("Summon Ravager", "entity-ravager"),
        new Effect("Summon Rabbit", "entity-rabbit"),
        new Effect("Summon Armor Stand", "entity-armor_stand"),
        // sets the server difficulty (affects how much damage mobs deal)
        new Effect("Set Difficulty: Peaceful", "difficulty-peaceful"),
        new Effect("Set Difficulty: Easy", "difficulty-easy"),
        new Effect("Set Difficulty: Normal", "difficulty-normal"),
        new Effect("Set Difficulty: Hard", "difficulty-hard"),
        // applies potion effects to every player
        new Effect("Apply Speed Potion Effect", "potion-speed"),
        new Effect("Apply Slowness Potion Effect", "potion-slowness"),
        new Effect("Apply Haste Potion Effect", "potion-haste"),
        new Effect("Apply Mining Fatigue Potion Effect", "potion-mining_fatigue"),
        new Effect("Apply Strength Potion Effect", "potion-strength"),
        new Effect("Apply Healing Potion Effect", "potion-healing"),
        new Effect("Apply Harming Potion Effect", "potion-harming"),
        new Effect("Apply Jump Boost Potion Effect", "potion-jump_boost"),
        new Effect("Apply Nausea Potion Effect", "potion-nausea"),
        new Effect("Apply Regeneration Potion Effect", "potion-regeneration"),
        new Effect("Apply Resistance Potion Effect", "potion-resistance"),
        new Effect("Apply Fire Resistance Potion Effect", "potion-fire_resistance"),
        new Effect("Apply Water Breathing Potion Effect", "potion-water_breathing"),
        new Effect("Apply Invisibility Potion Effect", "potion-invisibility"),
        new Effect("Apply Blindness Potion Effect", "potion-blindness"),
        new Effect("Apply Night Vision Potion Effect", "potion-night_vision"),
        new Effect("Apply Hunger Potion Effect", "potion-hunger"),
        new Effect("Apply Weakness Potion Effect", "potion-weakness"),
        new Effect("Apply Poison Potion Effect", "potion-poison"),
        new Effect("Apply Wither Potion Effect", "potion-wither"),
        new Effect("Apply Health Boost Potion Effect", "potion-health_boost"),
        new Effect("Apply Absorption Potion Effect", "potion-absorption"),
        new Effect("Apply Saturation Potion Effect", "potion-saturation"),
        new Effect("Apply Glowing Potion Effect", "potion-glowing"),
        new Effect("Apply Levitation Potion Effect", "potion-levitation"),
        new Effect("Apply Luck Potion Effect", "potion-luck"),
        new Effect("Apply Bad Luck Potion Effect", "potion-bad_luck"),
        new Effect("Apply Slow Falling Potion Effect", "potion-slow_falling"),
        new Effect("Apply Conduit Power Potion Effect", "potion-conduit_power"),
        new Effect("Apply Dolphins Grace Potion Effect", "potion-dolphins_grace"),
        new Effect("Apply Bad Omen Potion Effect", "potion-bad_omen"),
        new Effect("Apply Hero Of The Village Potion Effect", "potion-hero_of_the_village"),
        // places a block at everyone's feet
        new Effect("Place TNT Block", "block-tnt"),
        new Effect("Place Fire Block", "block-fire"),
        new Effect("Place Cobweb Block", "block-cobweb"),
        new Effect("Place Redstone Torch Block", "block-redstone_torch"),
        new Effect("Place Wither Rose Block", "block-wither_rose"),
        // places a block several blocks above everyone's head
        new Effect("Falling Anvil Block", "falling-block-anvil"),
        new Effect("Falling Sand Block", "falling-block-sand"),
        new Effect("Falling Red Sand Block", "falling-block-red_sand"),
        new Effect("Falling Gravel Block", "falling-block-gravel"),
        // sets the server weather
        new Effect("Set Weather to Downfall", "downfall"),
        new Effect("Set Weather to Clear", "clear"),
        // apply enchants
        new Effect("Remove Enchants", "remove-enchants"), // removes all enchants from the held item
        new Effect("Apply Fire Protection IV", "enchant-fire_protection"),
        new Effect("Apply Sharpness V", "enchant-sharpness"),
        new Effect("Apply Flame", "enchant-flame"),
        new Effect("Apply Soul Speed III", "enchant-soul_speed"),
        new Effect("Apply Aqua Affinity", "enchant-aqua_affinity"),
        new Effect("Apply Punch II", "enchant-punch"),
        new Effect("Apply Loyalty III", "enchant-loyalty"),
        new Effect("Apply Depth Strider III", "enchant-depth_strider"),
        new Effect("Apply Curse of Vanishing", "enchant-curse_of_vanishing"),
        new Effect("Apply Unbreaking III", "enchant-unbreaking"),
        new Effect("Apply Knockback II", "enchant-knockback"),
        new Effect("Apply Luck of the Sea III", "enchant-luck_of_the_sea"),
        new Effect("Apply Curse of Binding", "enchant-curse_of_binding"),
        new Effect("Apply Fortune III", "enchant-fortune"),
        new Effect("Apply Protection IV", "enchant-protection"),
        new Effect("Apply Efficiency V", "enchant-efficiency"),
        new Effect("Apply Mending", "enchant-mending"),
        new Effect("Apply Frost Walker II", "enchant-frost_walker"),
        new Effect("Apply Lure III", "enchant-lure"),
        new Effect("Apply Looting III", "enchant-looting"),
        new Effect("Apply Piercing IV", "enchant-piercing"),
        new Effect("Apply Blast Protection IV", "enchant-blast_protection"),
        new Effect("Apply Smite V", "enchant-smite"),
        new Effect("Apply Multishot", "enchant-multishot"),
        new Effect("Apply Fire Aspect II", "enchant-fire_aspect"),
        new Effect("Apply Channeling", "enchant-channeling"),
        new Effect("Apply Sweeping Edge III", "enchant-sweeping_edge"),
        new Effect("Apply Thorns III", "enchant-thorns"),
        new Effect("Apply Bane of Arthropods V", "enchant-bane_of_arthropods"),
        new Effect("Apply Respiration III", "enchant-respiration"),
        new Effect("Apply Riptide III", "enchant-riptide"),
        new Effect("Apply Silk Touch", "enchant-silk_touch"),
        new Effect("Apply Quick Charge III", "enchant-quick_charge"),
        new Effect("Apply Projectile Protection IV", "enchant-projectile_protection"),
        new Effect("Apply Impaling V", "enchant-impaling"),
        new Effect("Apply Feather Falling IV", "enchant-feather_falling"),
        new Effect("Apply Power V", "enchant-power"),
        new Effect("Apply Infinity", "enchant-infinity"),
        // gives/takes 1 item
        new Effect("Give Wooden Pickaxe", "give-wooden_pickaxe"),
        new Effect("Take Wooden Pickaxe", "take-wooden_pickaxe"),
        new Effect("Give Stone Pickaxe", "give-stone_pickaxe"),
        new Effect("Take Stone Pickaxe", "take-stone_pickaxe"),
        new Effect("Give Golden Pickaxe", "give-golden_pickaxe"),
        new Effect("Take Golden Pickaxe", "take-golden_pickaxe"),
        new Effect("Give Iron Pickaxe", "give-iron_pickaxe"),
        new Effect("Take Iron Pickaxe", "take-iron_pickaxe"),
        new Effect("Give Diamond Pickaxe", "give-diamond_pickaxe"),
        new Effect("Take Diamond Pickaxe", "take-diamond_pickaxe"),
        new Effect("Give Netherite Pickaxe", "give-netherite_pickaxe"),
        new Effect("Take Netherite Pickaxe", "take-netherite_pickaxe"),
        new Effect("Give Golden Apple", "give-golden_apple"),
        new Effect("Take Golden Apple", "take-golden_apple"),
        new Effect("Give Enchanted Golden Apple", "give-enchanted_golden_apple"),
        new Effect("Take Enchanted Golden Apple", "take-enchanted_golden_apple"),
    };
}