using System;
using System.Collections.Generic;
using CrowdControl.Common;

namespace CrowdControl.Games.Packs
{
    public class MinecraftServer : SimpleTCPPack
    {
        public override string Host => "127.0.0.1";

        public override ushort Port => 58431;

        public MinecraftServer(IPlayer player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler) : base(player, responseHandler, statusUpdateHandler) { }

        public override Game Game => new Game(108, "Minecraft (Server)", "MinecraftServer", "PC", ConnectorType.SimpleTCPConnector);

        public override List<Effect> Effects => new List<Effect>
        {
            // miscellaneous
            new Effect("Miscellaneous", "miscellaneous", ItemKind.Folder),
            new Effect("+1 Max Health", "max_health_plus1", "miscellaneous"),
            new Effect("-1 Max Health", "max_health_sub1", "miscellaneous"),
            new Effect("Camera Lock (7s)", "camera_lock", "miscellaneous"), // locks everyone's camera in place for 7 seconds (movement allowed)
            new Effect("Camera Lock To Ground (7s)", "camera_lock_to_ground", "miscellaneous"), // locks everyone's cameras to face the ground
            new Effect("Camera Lock To Sky (7s)", "camera_lock_to_sky", "miscellaneous"), // locks everyone's cameras to face the sky
            new Effect("Clutter Inventory", "clutter", "miscellaneous"), // shuffles around a couple items in everyone's inventory
            new Effect("Damage Item", "damage_item", "miscellaneous"), // sets the durability of an item to 50%
            new Effect("Damage Player (1 Heart)", "damage_1", "miscellaneous"),
            new Effect("Delete Held Item", "delete_item", "miscellaneous"),
            new Effect("Dig Hole", "dig", "miscellaneous") {Description = "Digs a small hole underneath the streamer"},
            new Effect("Dinnerbone", "dinnerbone", "miscellaneous") {Description = "Flips nearby mobs upside-down"},
            new Effect("Disable Jumping (10s)", "disable_jumping", "miscellaneous"),
            new Effect("Do-or-Die", "do_or_die", "miscellaneous") {Description = "Gives the streamer a task to complete within 30 seconds or else they die"},
            new Effect("Drop Held Item", "drop_item", "miscellaneous"),
            new Effect("Eat Chorus Fruit", "chorus_fruit", "miscellaneous") {Description = "Teleports the player to a random nearby block as if they ate a Chorus Fruit"},
            new Effect("Feed Player", "feed", "miscellaneous"),
            new Effect("Feed Player (1 Bar)", "feed_1", "miscellaneous"),
            new Effect("Freeze (7s)", "freeze", "miscellaneous"), // locks everyone in place for 7 seconds (camera rotation allowed)
            new Effect("Give One XP Level", "xp_plus1", "miscellaneous"),
            new Effect("Heal Player", "full_heal", "miscellaneous"),
            new Effect("Heal Player (1 Heart)", "heal_1", "miscellaneous"),
            new Effect("Halve Health", "half_health", "miscellaneous"), // sets player's health to 50% of what they currently have
            new Effect("Kill Player", "kill", "miscellaneous"),
            new Effect("Open Lootbox", "lootbox", "miscellaneous") {Description = "Gifts a completely random item with varying enchants and modifiers"},
            new Effect("Place Flowers", "flowers", "miscellaneous"), // places a bunch of flowers nearby as if a bone meal item was used
            new Effect("Place Torches", "lit", "miscellaneous"), // places torches on every valid nearby block
            new Effect("Plant Tree", "plant_tree", "miscellaneous"), // places a tree on the player
            new Effect("Put Held Item on Head", "hat", "miscellaneous"), // swaps the held item and the player's head item
            new Effect("Remove One Hunger Bar", "starve_1", "miscellaneous"),
            new Effect("Remove Torches", "dim", "miscellaneous"), // removes nearby torches
            new Effect("Render Toasts", "toast", "miscellaneous") {Description = "Plays an obnoxious animation and an obnoxious sound"},
            new Effect("Repair Item", "repair_item", "miscellaneous"), // sets the durability of a damaged item to 100%
            new Effect("Replace Area With Gravel", "gravel_hell", "miscellaneous") {Description = "Replaces nearby stone-like blocks with gravel"},
            new Effect("Reset Experience Progress", "reset_exp_progress", "miscellaneous") {Description = "Clears the streamer's progress towards their next XP level"},
            new Effect("Respawn Player", "respawn", "miscellaneous") {Description = "Sends the streamer to their spawn point"},
            new Effect("Spawn Ore Veins", "vein", "miscellaneous") {Description = "Places random ore veins (ore lava) near the streamer"},
            new Effect("Spooky Sound Effect", "sfx", "miscellaneous") {Description = "Plays a random spooky sound effect"},
            new Effect("Starve Player", "starve", "miscellaneous"), // makes everyone hungry
            new Effect("Swap Locations", "swap", "miscellaneous") {Description = "Swaps the locations of all players participating in a multiplayer Crowd Control session"},
            new Effect("Take One XP Level", "xp_sub1", "miscellaneous"),
            new Effect("Teleport All Entities To Players", "entity_chaos", "miscellaneous"),
            new Effect("Water Bucket Clutch", "bucket_clutch", "miscellaneous") {Description = "Teleports players 30 blocks up and gives them a water bucket"},
            new Effect("Zip Time", "zip", "miscellaneous") {Description = "Adds several minutes to the in-game time"},
            // inventory commands
            new Effect("Inventory", "inventory", ItemKind.Folder),
            new Effect("Clear Inventory", "clear_inventory", "inventory"),
            new Effect("Disable Keep Inventory", "keep_inventory_off", "inventory") {Description = "Disallows the streamer from keeping their inventory upon death"},
            new Effect("Enable Keep Inventory", "keep_inventory_on", "inventory") {Description = "Allows the streamer to keep their inventory upon death"},
            // set gamemode for 30 seconds
            new Effect("Change Gamemode", "change_gamemode", ItemKind.Folder),
            new Effect("Adventure Mode (15s)", "adventure_mode", "change_gamemode") {Description = "Temporarily sets the streamer to Adventure mode, rendering them unable to place or break blocks"},
            new Effect("Creative Mode (15s)", "creative_mode", "change_gamemode") {Description = "Temporarily sets the streamer to Creative mode, allowing them to fly and spawn in items"},
            new Effect("Spectator Mode (8s)", "spectator_mode", "change_gamemode") {Description = "Temporarily sets the streamer to Spectator mode, turning them into a ghost that can fly through blocks"},
            new Effect("Allow Flight (15s)", "flight", "change_gamemode") {Description = "Temporarily allows the streamer to fly"},
            // teleports players by a few blocks in the specified direction
            new Effect("Fling Players", "teleportation", ItemKind.Folder),
            new Effect("Fling Up", "up", "teleportation"),
            new Effect("Fling Down", "down", "teleportation"),
            new Effect("Fling East", "xplus", "teleportation"),
            new Effect("Fling West", "xminus", "teleportation"),
            new Effect("Fling South", "zplus", "teleportation"),
            new Effect("Fling North", "zminus", "teleportation"),
            // summons a mob around each player
            new Effect("Summon Entity", "summon_entity", ItemKind.Folder),
            new Effect("Armor Stand", "entity_armor_stand", "summon_entity"),
            new Effect("Axolotl", "entity_axolotl", "summon_entity"),
            new Effect("Bat", "entity_bat", "summon_entity"),
            new Effect("Bee", "entity_bee", "summon_entity"),
            new Effect("Blaze", "entity_blaze", "summon_entity"),
            new Effect("Boat", "entity_boat", "summon_entity"),
            new Effect("Cat", "entity_cat", "summon_entity"),
            new Effect("Cave Spider", "entity_cave_spider", "summon_entity"),
            new Effect("Charged Creeper", "entity_charged_creeper", "summon_entity"),
            new Effect("Chicken", "entity_chicken", "summon_entity"),
            new Effect("Cod", "entity_cod", "summon_entity"),
            new Effect("Cow", "entity_cow", "summon_entity"),
            new Effect("Creeper", "entity_creeper", "summon_entity"),
            new Effect("Dolphin", "entity_dolphin", "summon_entity"),
            new Effect("Donkey", "entity_donkey", "summon_entity"),
            new Effect("Drowned", "entity_drowned", "summon_entity"),
            new Effect("Elder Guardian", "entity_elder_guardian", "summon_entity"),
            new Effect("Ender Dragon", "entity_ender_dragon", "summon_entity"),
            new Effect("Enderman", "entity_enderman", "summon_entity"),
            new Effect("Endermite", "entity_endermite", "summon_entity"),
            new Effect("Evoker", "entity_evoker", "summon_entity"),
            new Effect("Fox", "entity_fox", "summon_entity"),
            new Effect("Ghast", "entity_ghast", "summon_entity"),
            new Effect("Giant", "entity_giant", "summon_entity"),
            new Effect("Glow Squid", "entity_glow_squid", "summon_entity"),
            new Effect("Goat", "entity_goat", "summon_entity"),
            new Effect("Guardian", "entity_guardian", "summon_entity"),
            new Effect("Hoglin", "entity_hoglin", "summon_entity"),
            new Effect("Horse", "entity_horse", "summon_entity"),
            new Effect("Husk", "entity_husk", "summon_entity"),
            new Effect("Illusioner", "entity_illusioner", "summon_entity"),
            new Effect("Iron Golem", "entity_iron_golem", "summon_entity"),
            new Effect("Lightning Bolt", "entity_lightning", "summon_entity"),
            new Effect("Llama", "entity_llama", "summon_entity"),
            new Effect("Magma Cube", "entity_magma_cube", "summon_entity"),
            new Effect("Minecart", "entity_minecart", "summon_entity"),
            new Effect("Minecart with Chest", "entity_minecart_chest", "summon_entity"),
            new Effect("Minecart with Furnace", "entity_minecart_furnace", "summon_entity"),
            new Effect("Minecart with Hopper", "entity_minecart_hopper", "summon_entity"),
            new Effect("Minecart with TNT", "entity_minecart_tnt", "summon_entity"),
            new Effect("Mooshroom", "entity_mushroom_cow", "summon_entity"),
            new Effect("Mule", "entity_mule", "summon_entity"),
            new Effect("Ocelot", "entity_ocelot", "summon_entity"),
            new Effect("Panda", "entity_panda", "summon_entity"),
            new Effect("Parrot", "entity_parrot", "summon_entity"),
            new Effect("Phantom", "entity_phantom", "summon_entity"),
            new Effect("Pig", "entity_pig", "summon_entity"),
            new Effect("Piglin", "entity_piglin", "summon_entity"),
            new Effect("Piglin Brute", "entity_piglin_brute", "summon_entity"),
            new Effect("Pillager", "entity_pillager", "summon_entity"),
            new Effect("Polar Bear", "entity_polar_bear", "summon_entity"),
            new Effect("Primed TNT", "entity_primed_tnt", "summon_entity"),
            new Effect("Pufferfish", "entity_pufferfish", "summon_entity"),
            new Effect("Rabbit", "entity_rabbit", "summon_entity"),
            new Effect("Ravager", "entity_ravager", "summon_entity"),
            new Effect("Salmon", "entity_salmon", "summon_entity"),
            new Effect("Sheep", "entity_sheep", "summon_entity"),
            new Effect("Shulker", "entity_shulker", "summon_entity"),
            new Effect("Silverfish", "entity_silverfish", "summon_entity"),
            new Effect("Skeleton", "entity_skeleton", "summon_entity"),
            new Effect("Skeleton Horse", "entity_skeleton_horse", "summon_entity"),
            new Effect("Slime", "entity_slime", "summon_entity"),
            new Effect("Snow Golem", "entity_snowman", "summon_entity"),
            new Effect("Spider", "entity_spider", "summon_entity"),
            new Effect("Squid", "entity_squid", "summon_entity"),
            new Effect("Stray", "entity_stray", "summon_entity"),
            new Effect("Strider", "entity_strider", "summon_entity"),
            new Effect("Trader Llama", "entity_trader_llama", "summon_entity"),
            new Effect("Tropical Fish", "entity_tropical_fish", "summon_entity"),
            new Effect("Turtle", "entity_turtle", "summon_entity"),
            new Effect("Vex", "entity_vex", "summon_entity"),
            new Effect("Villager", "entity_villager", "summon_entity"),
            new Effect("Vindicator", "entity_vindicator", "summon_entity"),
            new Effect("Wandering Trader", "entity_wandering_trader", "summon_entity"),
            new Effect("Witch", "entity_witch", "summon_entity"),
            new Effect("Wither", "entity_wither", "summon_entity"),
            new Effect("Wither Skeleton", "entity_wither_skeleton", "summon_entity"),
            new Effect("Wolf", "entity_wolf", "summon_entity"),
            new Effect("Zoglin", "entity_zoglin", "summon_entity"),
            new Effect("Zombie", "entity_zombie", "summon_entity"),
            new Effect("Zombie Horse", "entity_zombie_horse", "summon_entity"),
            new Effect("Zombie Villager", "entity_zombie_villager", "summon_entity"),
            new Effect("Zombified Piglin", "entity_zombified_piglin", "summon_entity"),
            // remove nearest entity
            new Effect("Remove Entity", "remove_entity", ItemKind.Folder),
            new Effect("Armor Stand", "remove_entity_armor_stand", "remove_entity"),
            new Effect("Axolotl", "remove_entity_axolotl", "remove_entity"),
            new Effect("Bat", "remove_entity_bat", "remove_entity"),
            new Effect("Bee", "remove_entity_bee", "remove_entity"),
            new Effect("Blaze", "remove_entity_blaze", "remove_entity"),
            new Effect("Boat", "remove_entity_boat", "remove_entity"),
            new Effect("Cat", "remove_entity_cat", "remove_entity"),
            new Effect("Cave Spider", "remove_entity_cave_spider", "remove_entity"),
            new Effect("Chicken", "remove_entity_chicken", "remove_entity"),
            new Effect("Cod", "remove_entity_cod", "remove_entity"),
            new Effect("Cow", "remove_entity_cow", "remove_entity"),
            new Effect("Creeper", "remove_entity_creeper", "remove_entity"),
            new Effect("Dolphin", "remove_entity_dolphin", "remove_entity"),
            new Effect("Donkey", "remove_entity_donkey", "remove_entity"),
            new Effect("Drowned", "remove_entity_drowned", "remove_entity"),
            new Effect("Elder Guardian", "remove_entity_elder_guardian", "remove_entity"),
            new Effect("Ender Dragon", "remove_entity_ender_dragon", "remove_entity"),
            new Effect("Enderman", "remove_entity_enderman", "remove_entity"),
            new Effect("Endermite", "remove_entity_endermite", "remove_entity"),
            new Effect("Evoker", "remove_entity_evoker", "remove_entity"),
            new Effect("Fox", "remove_entity_fox", "remove_entity"),
            new Effect("Ghast", "remove_entity_ghast", "remove_entity"),
            new Effect("Giant", "remove_entity_giant", "remove_entity"),
            new Effect("Glow Squid", "remove_entity_glow_squid", "remove_entity"),
            new Effect("Goat", "remove_entity_goat", "remove_entity"),
            new Effect("Guardian", "remove_entity_guardian", "remove_entity"),
            new Effect("Hoglin", "remove_entity_hoglin", "remove_entity"),
            new Effect("Horse", "remove_entity_horse", "remove_entity"),
            new Effect("Husk", "remove_entity_husk", "remove_entity"),
            new Effect("Illusioner", "remove_entity_illusioner", "remove_entity"),
            new Effect("Iron Golem", "remove_entity_iron_golem", "remove_entity"),
            new Effect("Lightning Bolt", "remove_entity_lightning", "remove_entity"),
            new Effect("Llama", "remove_entity_llama", "remove_entity"),
            new Effect("Magma Cube", "remove_entity_magma_cube", "remove_entity"),
            new Effect("Minecart", "remove_entity_minecart", "remove_entity"),
            new Effect("Minecart with Chest", "remove_entity_minecart_chest", "remove_entity"),
            new Effect("Minecart with Furnace", "remove_entity_minecart_furnace", "remove_entity"),
            new Effect("Minecart with Hopper", "remove_entity_minecart_hopper", "remove_entity"),
            new Effect("Minecart with TNT", "remove_entity_minecart_tnt", "remove_entity"),
            new Effect("Mooshroom", "remove_entity_mushroom_cow", "remove_entity"),
            new Effect("Mule", "remove_entity_mule", "remove_entity"),
            new Effect("Ocelot", "remove_entity_ocelot", "remove_entity"),
            new Effect("Panda", "remove_entity_panda", "remove_entity"),
            new Effect("Parrot", "remove_entity_parrot", "remove_entity"),
            new Effect("Phantom", "remove_entity_phantom", "remove_entity"),
            new Effect("Pig", "remove_entity_pig", "remove_entity"),
            new Effect("Piglin", "remove_entity_piglin", "remove_entity"),
            new Effect("Piglin Brute", "remove_entity_piglin_brute", "remove_entity"),
            new Effect("Pillager", "remove_entity_pillager", "remove_entity"),
            new Effect("Polar Bear", "remove_entity_polar_bear", "remove_entity"),
            new Effect("Primed TNT", "remove_entity_primed_tnt", "remove_entity"),
            new Effect("Pufferfish", "remove_entity_pufferfish", "remove_entity"),
            new Effect("Rabbit", "remove_entity_rabbit", "remove_entity"),
            new Effect("Ravager", "remove_entity_ravager", "remove_entity"),
            new Effect("Salmon", "remove_entity_salmon", "remove_entity"),
            new Effect("Sheep", "remove_entity_sheep", "remove_entity"),
            new Effect("Shulker", "remove_entity_shulker", "remove_entity"),
            new Effect("Silverfish", "remove_entity_silverfish", "remove_entity"),
            new Effect("Skeleton", "remove_entity_skeleton", "remove_entity"),
            new Effect("Skeleton Horse", "remove_entity_skeleton_horse", "remove_entity"),
            new Effect("Slime", "remove_entity_slime", "remove_entity"),
            new Effect("Snow Golem", "remove_entity_snowman", "remove_entity"),
            new Effect("Spider", "remove_entity_spider", "remove_entity"),
            new Effect("Squid", "remove_entity_squid", "remove_entity"),
            new Effect("Stray", "remove_entity_stray", "remove_entity"),
            new Effect("Strider", "remove_entity_strider", "remove_entity"),
            new Effect("Trader Llama", "remove_entity_trader_llama", "remove_entity"),
            new Effect("Tropical Fish", "remove_entity_tropical_fish", "remove_entity"),
            new Effect("Turtle", "remove_entity_turtle", "remove_entity"),
            new Effect("Vex", "remove_entity_vex", "remove_entity"),
            new Effect("Villager", "remove_entity_villager", "remove_entity"),
            new Effect("Vindicator", "remove_entity_vindicator", "remove_entity"),
            new Effect("Wandering Trader", "remove_entity_wandering_trader", "remove_entity"),
            new Effect("Witch", "remove_entity_witch", "remove_entity"),
            new Effect("Wither", "remove_entity_wither", "remove_entity"),
            new Effect("Wither Skeleton", "remove_entity_wither_skeleton", "remove_entity"),
            new Effect("Wolf", "remove_entity_wolf", "remove_entity"),
            new Effect("Zoglin", "remove_entity_zoglin", "remove_entity"),
            new Effect("Zombie", "remove_entity_zombie", "remove_entity"),
            new Effect("Zombie Horse", "remove_entity_zombie_horse", "remove_entity"),
            new Effect("Zombie Villager", "remove_entity_zombie_villager", "remove_entity"),
            new Effect("Zombified Piglin", "remove_entity_zombified_piglin", "remove_entity"),
            // sets the server difficulty (affects how much damage mobs deal)
            new Effect("Set Difficulty", "difficulty", ItemKind.Folder),
            new Effect("Peaceful Mode", "difficulty_peaceful", "difficulty"),
            new Effect("Easy Mode", "difficulty_easy", "difficulty"),
            new Effect("Normal Mode", "difficulty_normal", "difficulty"),
            new Effect("Hard Mode", "difficulty_hard", "difficulty"),
            // applies potion effects to every player
            new Effect("Apply Potion", "apply_potion_effect", ItemKind.Folder),
            new Effect("Absorption", "potion_absorption", "apply_potion_effect"),
            //new Effect("Bad Luck", "potion_bad_luck", "apply_potion_effect"),
            new Effect("Bad Omen", "potion_bad_omen", "apply_potion_effect"),
            new Effect("Blindness", "potion_blindness", "apply_potion_effect"),
            new Effect("Conduit Power", "potion_conduit_power", "apply_potion_effect"),
            new Effect("Dolphins Grace", "potion_dolphins_grace", "apply_potion_effect"),
            new Effect("Fire Resistance", "potion_fire_resistance", "apply_potion_effect"),
            new Effect("Glowing", "potion_glowing", "apply_potion_effect"),
            //new Effect("Harming", "potion_harming", "apply_potion_effect"),
            new Effect("Haste", "potion_haste", "apply_potion_effect"),
            //new Effect("Healing", "potion_healing", "apply_potion_effect"),
            new Effect("Health Boost", "potion_health_boost", "apply_potion_effect"),
            new Effect("Hero Of The Village", "potion_hero_of_the_village", "apply_potion_effect"),
            new Effect("Hunger", "potion_hunger", "apply_potion_effect"),
            new Effect("Invisibility", "potion_invisibility", "apply_potion_effect"),
            new Effect("Jump Boost", "potion_jump_boost", "apply_potion_effect"),
            new Effect("Levitation", "potion_levitation", "apply_potion_effect"),
            //new Effect("Luck", "potion_luck", "apply_potion_effect"),
            new Effect("Mining Fatigue", "potion_mining_fatigue", "apply_potion_effect"),
            new Effect("Nausea", "potion_nausea", "apply_potion_effect"),
            new Effect("Night Vision", "potion_night_vision", "apply_potion_effect"),
            new Effect("Poison", "potion_poison", "apply_potion_effect"),
            new Effect("Regeneration", "potion_regeneration", "apply_potion_effect"),
            new Effect("Resistance", "potion_resistance", "apply_potion_effect"),
            //new Effect("Saturation", "potion_saturation", "apply_potion_effect"),
            new Effect("Slow Falling", "potion_slow_falling", "apply_potion_effect"),
            new Effect("Slowness", "potion_slowness", "apply_potion_effect"),
            new Effect("Speed", "potion_speed", "apply_potion_effect"),
            new Effect("Strength", "potion_strength", "apply_potion_effect"),
            new Effect("Water Breathing", "potion_water_breathing", "apply_potion_effect"),
            new Effect("Weakness", "potion_weakness", "apply_potion_effect"),
            //new Effect("Wither", "potion_wither", "apply_potion_effect"),
            // places a block at everyone's feet
            new Effect("Place Block", "place_block", ItemKind.Folder),
            new Effect("Cobweb", "block_cobweb", "place_block"),
            new Effect("Fire", "block_fire", "place_block"),
            new Effect("Redstone Torch", "block_redstone_torch", "place_block"),
            new Effect("TNT", "block_tnt", "place_block"),
            new Effect("Wither Rose", "block_wither_rose", "place_block"),
            new Effect("Lightning Rod", "block_lightning_rod", "place_block"),
            // places a block several blocks above everyone's head
            new Effect("Place Falling Block", "place_falling_block", ItemKind.Folder),
            new Effect("Anvil", "falling_block_anvil", "place_falling_block"),
            new Effect("Gravel", "falling_block_gravel", "place_falling_block"),
            new Effect("Red Sand", "falling_block_red_sand", "place_falling_block"),
            new Effect("Sand", "falling_block_sand", "place_falling_block"),
            // sets the server weather
            new Effect("Set Weather", "weather", ItemKind.Folder),
            new Effect("Clear Weather", "clear", "weather"),
            new Effect("Rainy Weather", "downfall", "weather"),
            // apply enchants
            new Effect("Enchantments", "enchantments", ItemKind.Folder),
            new Effect("Remove Enchants", "remove_enchants", "enchantments"), // removes all enchants from the held item
            new Effect("Apply Aqua Affinity", "enchant_aqua_affinity", "enchantments"),
            new Effect("Apply Bane of Arthropods V", "enchant_bane_of_arthropods", "enchantments"),
            new Effect("Apply Blast Protection IV", "enchant_blast_protection", "enchantments"),
            new Effect("Apply Channeling", "enchant_channeling", "enchantments"),
            new Effect("Apply Curse of Binding", "enchant_curse_of_binding", "enchantments"),
            new Effect("Apply Curse of Vanishing", "enchant_curse_of_vanishing", "enchantments"),
            new Effect("Apply Depth Strider III", "enchant_depth_strider", "enchantments"),
            new Effect("Apply Efficiency V", "enchant_efficiency", "enchantments"),
            new Effect("Apply Feather Falling IV", "enchant_feather_falling", "enchantments"),
            new Effect("Apply Fire Aspect II", "enchant_fire_aspect", "enchantments"),
            new Effect("Apply Fire Protection IV", "enchant_fire_protection", "enchantments"),
            new Effect("Apply Flame", "enchant_flame", "enchantments"),
            new Effect("Apply Fortune III", "enchant_fortune", "enchantments"),
            new Effect("Apply Frost Walker II", "enchant_frost_walker", "enchantments"),
            new Effect("Apply Impaling V", "enchant_impaling", "enchantments"),
            new Effect("Apply Infinity", "enchant_infinity", "enchantments"),
            new Effect("Apply Knockback II", "enchant_knockback", "enchantments"),
            new Effect("Apply Looting III", "enchant_looting", "enchantments"),
            new Effect("Apply Loyalty III", "enchant_loyalty", "enchantments"),
            new Effect("Apply Luck of the Sea III", "enchant_luck_of_the_sea", "enchantments"),
            new Effect("Apply Lure III", "enchant_lure", "enchantments"),
            new Effect("Apply Mending", "enchant_mending", "enchantments"),
            new Effect("Apply Multishot", "enchant_multishot", "enchantments"),
            new Effect("Apply Piercing IV", "enchant_piercing", "enchantments"),
            new Effect("Apply Power V", "enchant_power", "enchantments"),
            new Effect("Apply Projectile Protection IV", "enchant_projectile_protection", "enchantments"),
            new Effect("Apply Protection IV", "enchant_protection", "enchantments"),
            new Effect("Apply Punch II", "enchant_punch", "enchantments"),
            new Effect("Apply Quick Charge III", "enchant_quick_charge", "enchantments"),
            new Effect("Apply Respiration III", "enchant_respiration", "enchantments"),
            new Effect("Apply Riptide III", "enchant_riptide", "enchantments"),
            new Effect("Apply Sharpness V", "enchant_sharpness", "enchantments"),
            new Effect("Apply Silk Touch", "enchant_silk_touch", "enchantments"),
            new Effect("Apply Smite V", "enchant_smite", "enchantments"),
            new Effect("Apply Soul Speed III", "enchant_soul_speed", "enchantments"),
            new Effect("Apply Sweeping Edge III", "enchant_sweeping_edge", "enchantments"),
            new Effect("Apply Thorns III", "enchant_thorns", "enchantments"),
            new Effect("Apply Unbreaking III", "enchant_unbreaking", "enchantments"),
            // gives 1 item
            new Effect("Give an Item", "give_item", ItemKind.Folder),
            new Effect("Wooden Pickaxe", "give_wooden_pickaxe", "give_item"),
            new Effect("Stone Pickaxe", "give_stone_pickaxe", "give_item"),
            new Effect("Golden Pickaxe", "give_golden_pickaxe", "give_item"),
            new Effect("Iron Pickaxe", "give_iron_pickaxe", "give_item"),
            new Effect("Diamond Pickaxe", "give_diamond_pickaxe", "give_item"),
            new Effect("Netherite Pickaxe", "give_netherite_pickaxe", "give_item"),
            new Effect("Golden Apple", "give_golden_apple", "give_item"),
            new Effect("Enchanted Golden Apple", "give_enchanted_golden_apple", "give_item"),
            new Effect("Eye of Ender", "give_ender_eye", "give_item"),
            new Effect("End Portal Frame", "give_end_portal_frame", "give_item"),
            // takes 1 item
            new Effect("Take an Item", "take_item", ItemKind.Folder),
            new Effect("Wooden Pickaxe", "take_wooden_pickaxe", "take_item"),
            new Effect("Stone Pickaxe", "take_stone_pickaxe", "take_item"),
            new Effect("Golden Pickaxe", "take_golden_pickaxe", "take_item"),
            new Effect("Iron Pickaxe", "take_iron_pickaxe", "take_item"),
            new Effect("Diamond Pickaxe", "take_diamond_pickaxe", "take_item"),
            new Effect("Netherite Pickaxe", "take_netherite_pickaxe", "take_item"),
            new Effect("Golden Apple", "take_golden_apple", "take_item"),
            new Effect("Enchanted Golden Apple", "take_enchanted_golden_apple", "take_item"),
            new Effect("Eye of Ender", "take_ender_eye", "take_item"),
            new Effect("End Portal Frame", "take_end_portal_frame", "take_item"),
        };
    }
}