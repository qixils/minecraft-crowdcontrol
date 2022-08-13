// ReSharper disable RedundantUsingDirective
// (these imports are required by CC)
using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using ConnectorLib;
using ConnectorLib.JSON;
using CrowdControl.Common;
using Newtonsoft.Json;
using ConnectorType = CrowdControl.Common.ConnectorType;
using Log = CrowdControl.Common.Log;

namespace CrowdControl.Games.Packs
{
    [Serializable]
    public record ServerStatus(bool GlobalEffects, bool ClientEffects, string[] RegisteredEffects);

    public sealed class GlobalEffect : Effect
    {
        public GlobalEffect(string name, string code, string parent) : base(name, code, parent) { }
        public GlobalEffect(string name, string code, ItemKind kind) : base(name, code, kind) { }
        public GlobalEffect(string name, string code, ItemKind kind, string auto) : base(name, code, kind, auto) { }
    }

    public sealed class ClientEffect : Effect
    {
        public ClientEffect(string name, string code, string parent) : base(name, code, parent) { }
        public ClientEffect(string name, string code, ItemKind kind) : base(name, code, kind) { }
        public ClientEffect(string name, string code, ItemKind kind, string auto) : base(name, code, kind, auto) { }
    }

    public class MinecraftServer : SimpleTCPPack<SimpleTCPClientConnector>
    {
        // default port: 58731
        public override ISimpleTCPPack.PromptType PromptType => ISimpleTCPPack.PromptType.Host | ISimpleTCPPack.PromptType.Password;

        public override ISimpleTCPPack.AuthenticationType AuthenticationMode => ISimpleTCPPack.AuthenticationType.SimpleTCPSendKey;

        public override ISimpleTCPPack.DigestAlgorithm AuthenticationHashMode => ISimpleTCPPack.DigestAlgorithm.SHA_512;

        public MinecraftServer(IPlayer player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler) : base(player, responseHandler, statusUpdateHandler) { }

        public override Game Game => new(108, "Minecraft", "MinecraftServer", "PC", ConnectorType.SimpleTCPClientConnector);

        private static readonly List<Effect> AllEffects = new()
        {
            // miscellaneous
            new Effect("Miscellaneous", "miscellaneous", ItemKind.Folder),
            new Effect("-1 Max Health", "max_health_sub1", "miscellaneous") { Price = 100, Description = "Subtracts half a heart from the streamer's max health" },
            new Effect("+1 Max Health", "max_health_plus1", "miscellaneous") { Price = 50, Description = "Adds half a heart to the streamer's max health" },
            new Effect("Annoying Pop-Ups", "toast", "miscellaneous") { Price = 50, Description = "Plays an obnoxious animation and an obnoxious sound" },
            new Effect("Camera Lock (10s)", "camera_lock", "miscellaneous") { Price = 100, Description = "Temporarily freeze the streamer's camera" },
            new Effect("Camera Lock To Ground (10s)", "camera_lock_to_ground", "miscellaneous") { Price = 150, Description = "Temporarily locks the streamer's camera to the ground" },
            new Effect("Camera Lock To Sky (10s)", "camera_lock_to_sky", "miscellaneous") { Price = 150, Description = "Temporarily locks the streamer's camera to the sky" },
            new Effect("Clutter Inventory", "clutter", "miscellaneous") { Price = 50, Description = "Shuffles around items in the streamer's inventory" },
            new Effect("Damage Held Item", "damage_item", "miscellaneous") { Price = 100, Description = "Halves the durability of the held item" },
            new Effect("Damage Player (1 Heart)", "damage_1", "miscellaneous") { Price = 25, Description = "Removes a single heart of health from the streamer (unless it would kill them)" },
            new Effect("Delete Held Item", "delete_item", "miscellaneous") { Price = 200, Description = "Deletes whatever item the streamer is currently holding" },
            new Effect("Dig Hole", "dig", "miscellaneous") { Price = 250, Description = "Digs a small hole underneath the streamer" },
            new Effect("Disable Jumping (10s)", "disable_jumping", "miscellaneous") { Price = 25, Description = "Temporarily prevents the streamer from jumping" },
            new Effect("Do-or-Die", "do_or_die", "miscellaneous") { Price = 500, Description = "Gives the streamer a task to complete within 30 seconds or else they die" },
            new Effect("Drop Held Item", "drop_item", "miscellaneous") { Price = 25, Description = "Makes the streamer drop their held item" },
            new Effect("Eat Chorus Fruit", "chorus_fruit", "miscellaneous") { Price = 75, Description = "Teleports the player to a random nearby block as if they ate a Chorus Fruit" },
            new Effect("Explode", "explode", "miscellaneous") { Price = 750, Description = "Spawns a TNT-like explosion at the streamer's feet" },
            new Effect("Feed Player", "feed", "miscellaneous") { Price = 15, Description = "Replenishes the hunger bar" },
            new Effect("Feed Player (1 Bar)", "feed_1", "miscellaneous") { Price = 2, Description = "Replenishes a single bar of hunger" },
            new Effect("Flip Mobs Upside-Down", "dinnerbone", "miscellaneous") { Price = 25, Description = "Flips nearby mobs upside-down by naming them after the iconic Minecraft developer Dinnerbone" },
            new Effect("Freeze (10s)", "freeze", "miscellaneous") { Price = 100, Description = "Temporarily prohibits movement" },
            new Effect("Give One XP Level", "xp_plus1", "miscellaneous") { Price = 75, Description = "Adds one level of experience" },
            new Effect("Heal Player", "full_heal", "miscellaneous") { Price = 50, Description = "Resets the streamer's health to full" },
            new Effect("Heal Player (1 Heart)", "heal_1", "miscellaneous") { Price = 10, Description = "Increases the streamer's health by a single bar" },
            new Effect("Halve Health", "half_health", "miscellaneous") { Price = 300, Description = "Sets the player's health to 50% of what they currently have" },
            new Effect("Kill Player", "kill", "miscellaneous") { Price = 1500, Description = "Immediately kills the streamer on the spot" },
            new Effect("Open Lootbox", "lootbox", "miscellaneous") { Price = 100, Description = "Gifts a completely random item with varying enchants and modifiers" },
            new Effect("Open Lucky Lootbox", "lootbox_5", "miscellaneous") { Price = 300, Description = "Gifts two random items with higher odds of having beneficial enchantments and modifiers" },
            new Effect("Place Flowers", "flowers", "miscellaneous") { Price = 25, Description = "Randomly places flowers nearby" },
            new Effect("Place Torches", "lit", "miscellaneous") { Price = 100, Description = "Places torches on every nearby block" },
            new Effect("Plant Tree", "plant_tree", "miscellaneous") { Price = 200, Description = "Plant a tree on top of the streamer" },
            new Effect("Put Held Item on Head", "hat", "miscellaneous") { Price = 25, Description = "Moves the item in the streamer's hand to their head" },
            new Effect("Remove One Hunger Bar", "starve_1", "miscellaneous") { Price = 50, Description = "Removes a single bar of food" },
            new Effect("Remove Torches", "dim", "miscellaneous") { Price = 200, Description = "Removes all nearby torches" },
            new Effect("Repair Held Item", "repair_item", "miscellaneous") { Price = 100, Description = "Fully repairs a damaged item" },
            new Effect("Replace Area With Gravel", "gravel_hell", "miscellaneous") { Price = 300, Description = "Replaces nearby stone-like blocks with gravel" },
            new Effect("Reset Experience", "reset_exp_progress", "miscellaneous") { Price = 1000, Description = "Clears all of the streamer's XP" },
            new Effect("Respawn Player", "respawn", "miscellaneous") { Price = 500, Description = "Sends the streamer to their spawn point" },
            new GlobalEffect("Set Time to Day", "time_day", "miscellaneous") { Price = 50, Description = "Jumps the clock ahead to daytime" },
            new GlobalEffect("Set Time to Night", "time_night", "miscellaneous") { Price = 50, Description = "Jumps the clock ahead to nighttime" },
            new Effect("Spawn Ore Veins", "vein", "miscellaneous") { Price = 100, Description = "Places random ore veins (ore lava) near the streamer" },
            new Effect("Spooky Sound Effect", "sfx", "miscellaneous") { Price = 50, Description = "Plays a random spooky sound effect" },
            new Effect("Starve Player", "starve", "miscellaneous") { Price = 500, Description = "Drains the players' hunger bar" },
            new Effect("Swap Locations", "swap", "miscellaneous") { Price = 1000, Description = "Swaps the locations of all players participating in a multiplayer Crowd Control session" },
            new Effect("Take One XP Level", "xp_sub1", "miscellaneous") { Price = 500, Description = "Removes one level of experience" },
            new Effect("Teleport to a Nearby Structure", "structure", "miscellaneous") { Price = 1000, Description = "Teleports players to a random nearby structure" },
            new Effect("Teleport to a Random Biome", "biome", "miscellaneous") { Price = 1000, Description = "Teleports players to a random nearby biome" },
            new Effect("Teleport All Entities To Players", "entity_chaos", "miscellaneous") { Price = 2000, Description = "Teleports every loaded mob on the server to the targeted streamers in an even split. Depending on the server configuration, this may only teleport nearby mobs." },
            new Effect("Water Bucket Clutch", "bucket_clutch", "miscellaneous") { Price = 400, Description = "Teleports players 30 blocks up and gives them a water bucket" },
            new GlobalEffect("Zip Time", "zip", "miscellaneous") { Price = 30, Description = "Adds several minutes to the in-game time" },
            // inventory commands
            new Effect("Inventory", "inventory", ItemKind.Folder),
            new Effect("Clear Inventory", "clear_inventory", "inventory") { Price = 1000, Description = "Wipes every item from the streamer's inventory" },
            new Effect("Disable Keep Inventory", "keep_inventory_off", "inventory") { Price = 200, Description = "Disallows the streamer from keeping their inventory upon death" },
            new Effect("Enable Keep Inventory", "keep_inventory_on", "inventory") { Price = 100, Description = "Allows the streamer to keep their inventory upon death" },
            // set gamemode for 30 seconds
            new Effect("Change Gamemode", "change_gamemode", ItemKind.Folder),
            new Effect("Adventure Mode (15s)", "adventure_mode", "change_gamemode") { Price = 200, Description = "Temporarily sets the streamer to Adventure mode, rendering them unable to place or break blocks" },
            new Effect("Creative Mode (15s)", "creative_mode", "change_gamemode") { Price = 150, Description = "Temporarily sets the streamer to Creative mode, allowing them to fly and spawn in items" },
            new Effect("Spectator Mode (8s)", "spectator_mode", "change_gamemode") { Price = 150, Description = "Temporarily sets the streamer to Spectator mode, turning them into a ghost that can fly through blocks" },
            new Effect("Allow Flight (15s)", "flight", "change_gamemode") { Price = 100, Description = "Temporarily allows the streamer to fly" },
            // teleports players by a few blocks in the specified direction
            new Effect("Fling Players", "teleportation", ItemKind.Folder),
            new Effect("Fling Up", "up", "teleportation") { Price = 100, Description = "Flings the streamer high into the air" },
            new Effect("Fling Down", "down", "teleportation") { Price = 100, Description = "Every few seconds, this will try to suddenly fling the streamer downwards if they're in the air" },
            new Effect("Fling Randomly", "fling", "teleportation") { Price = 100, Description = "Flings the streamer in a totally random direction" },
            // summons a mob around each player
            new Effect("Summon Entity", "summon_entity", ItemKind.Folder),
            new Effect("Allay", "entity_allay", "summon_entity") { Price = 100 },
            new Effect("Armor Stand", "entity_armor_stand", "summon_entity") { Price = 10 },
            new Effect("Axolotl", "entity_axolotl", "summon_entity") { Price = 200 },
            new Effect("Bat", "entity_bat", "summon_entity") { Price = 5 },
            new Effect("Bee", "entity_bee", "summon_entity") { Price = 200 },
            new Effect("Blaze", "entity_blaze", "summon_entity") { Price = 300 },
            new Effect("Boat", "entity_boat", "summon_entity") { Price = 50 },
            new Effect("Boat with Chest", "entity_chest_boat", "summon_entity") { Price = 125 },
            new Effect("Cat", "entity_cat", "summon_entity") { Price = 200 },
            new Effect("Cave Spider", "entity_cave_spider", "summon_entity") { Price = 300 },
            new Effect("Charged Creeper", "entity_charged_creeper", "summon_entity") { Price = 500 },
            new Effect("Chicken", "entity_chicken", "summon_entity") { Price = 100 },
            new Effect("Cod", "entity_cod", "summon_entity") { Price = 100 },
            new Effect("Cow", "entity_cow", "summon_entity") { Price = 100 },
            new Effect("Creeper", "entity_creeper", "summon_entity") { Price = 300 },
            new Effect("Dolphin", "entity_dolphin", "summon_entity") { Price = 200 },
            new Effect("Donkey", "entity_donkey", "summon_entity") { Price = 200 },
            new Effect("Drowned", "entity_drowned", "summon_entity") { Price = 300 },
            new Effect("Elder Guardian", "entity_elder_guardian", "summon_entity") { Price = 1000 },
            new Effect("Ender Dragon", "entity_ender_dragon", "summon_entity") { Price = 2000 },
            new Effect("Enderman", "entity_enderman", "summon_entity") { Price = 300 },
            new Effect("Endermite", "entity_endermite", "summon_entity") { Price = 300 },
            new Effect("Evoker", "entity_evoker", "summon_entity") { Price = 750 },
            new Effect("Fox", "entity_fox", "summon_entity") { Price = 200 },
            new Effect("Frog", "entity_frog", "summon_entity") { Price = 200 },
            new Effect("Ghast", "entity_ghast", "summon_entity") { Price = 500 },
            new Effect("Giant", "entity_giant", "summon_entity") { Price = 100 }, // this mob does literally nothing but it is big and obnoxious :-)
            new Effect("Glow Squid", "entity_glow_squid", "summon_entity") { Price = 150 },
            new Effect("Goat", "entity_goat", "summon_entity") { Price = 250 },
            new Effect("Guardian", "entity_guardian", "summon_entity") { Price = 300 },
            new Effect("Hoglin", "entity_hoglin", "summon_entity") { Price = 1000 },
            new Effect("Horse", "entity_horse", "summon_entity") { Price = 200 },
            new Effect("Husk", "entity_husk", "summon_entity") { Price = 300 },
            new Effect("Illusioner", "entity_illusioner", "summon_entity") { Price = 500 },
            new Effect("Iron Golem", "entity_iron_golem", "summon_entity") { Price = 300 },
            new Effect("Lightning Bolt", "entity_lightning", "summon_entity") { Price = 300 },
            new Effect("Llama", "entity_llama", "summon_entity") { Price = 200 },
            new Effect("Magma Cube", "entity_magma_cube", "summon_entity") { Price = 350 },
            new Effect("Minecart", "entity_minecart", "summon_entity") { Price = 10 },
            new Effect("Minecart with Chest", "entity_minecart_chest", "summon_entity") { Price = 100 },
            new Effect("Mooshroom", "entity_mushroom_cow", "summon_entity") { Price = 200 },
            new Effect("Mule", "entity_mule", "summon_entity") { Price = 200 },
            new Effect("Ocelot", "entity_ocelot", "summon_entity") { Price = 100 },
            new Effect("Panda", "entity_panda", "summon_entity") { Price = 200 },
            new Effect("Parrot", "entity_parrot", "summon_entity") { Price = 200 },
            new Effect("Phantom", "entity_phantom", "summon_entity") { Price = 300 },
            new Effect("Pig", "entity_pig", "summon_entity") { Price = 100 },
            new Effect("Piglin", "entity_piglin", "summon_entity") { Price = 350 },
            new Effect("Piglin Brute", "entity_piglin_brute", "summon_entity") { Price = 1000 },
            new Effect("Pillager", "entity_pillager", "summon_entity") { Price = 500 },
            new Effect("Polar Bear", "entity_polar_bear", "summon_entity") { Price = 200 },
            new Effect("Primed TNT", "entity_primed_tnt", "summon_entity") { Price = 1000 },
            new Effect("Pufferfish", "entity_pufferfish", "summon_entity") { Price = 300 },
            new Effect("Rabbit", "entity_rabbit", "summon_entity") { Price = 100 },
            new Effect("Ravager", "entity_ravager", "summon_entity") { Price = 1000 },
            new Effect("Salmon", "entity_salmon", "summon_entity") { Price = 100 },
            new Effect("Sheep", "entity_sheep", "summon_entity") { Price = 100 },
            new Effect("Shulker", "entity_shulker", "summon_entity") { Price = 500 },
            new Effect("Silverfish", "entity_silverfish", "summon_entity") { Price = 300 },
            new Effect("Skeleton", "entity_skeleton", "summon_entity") { Price = 300 },
            new Effect("Skeleton Horse", "entity_skeleton_horse", "summon_entity") { Price = 200 },
            new Effect("Slime", "entity_slime", "summon_entity") { Price = 300 },
            new Effect("Snow Golem", "entity_snowman", "summon_entity") { Price = 200 },
            new Effect("Spider", "entity_spider", "summon_entity") { Price = 300 },
            new Effect("Squid", "entity_squid", "summon_entity") { Price = 100 },
            new Effect("Stray", "entity_stray", "summon_entity") { Price = 300 },
            new Effect("Strider", "entity_strider", "summon_entity") { Price = 200 },
            new Effect("Tadpole", "entity_tadpole", "summon_entity") { Price = 200 },
            new Effect("Trader Llama", "entity_trader_llama", "summon_entity") { Price = 150 },
            new Effect("Tropical Fish", "entity_tropical_fish", "summon_entity") { Price = 100 },
            new Effect("Turtle", "entity_turtle", "summon_entity") { Price = 200 },
            new Effect("Vex", "entity_vex", "summon_entity") { Price = 350 },
            new Effect("Villager", "entity_villager", "summon_entity") { Price = 200 },
            new Effect("Vindicator", "entity_vindicator", "summon_entity") { Price = 500 },
            new Effect("Wandering Trader", "entity_wandering_trader", "summon_entity") { Price = 200 },
            new Effect("Warden", "entity_warden", "summon_entity") { Price = 6000 },
            new Effect("Witch", "entity_witch", "summon_entity") { Price = 300 },
            new Effect("Wither", "entity_wither", "summon_entity") { Price = 4000 },
            new Effect("Wither Skeleton", "entity_wither_skeleton", "summon_entity") { Price = 500 },
            new Effect("Wolf", "entity_wolf", "summon_entity") { Price = 200 },
            new Effect("Zoglin", "entity_zoglin", "summon_entity") { Price = 1000 },
            new Effect("Zombie", "entity_zombie", "summon_entity") { Price = 300 },
            new Effect("Zombie Horse", "entity_zombie_horse", "summon_entity") { Price = 200 },
            new Effect("Zombie Villager", "entity_zombie_villager", "summon_entity") { Price = 300 },
            new Effect("Zombified Piglin", "entity_zombified_piglin", "summon_entity") { Price = 300 },
            // remove nearest entity
            new Effect("Remove Entity", "remove_entity", ItemKind.Folder),
            new Effect("Allay", "remove_entity_allay", "remove_entity") { Price = 150 },
            new Effect("Armor Stand", "remove_entity_armor_stand", "remove_entity") { Price = 50 },
            new Effect("Axolotl", "remove_entity_axolotl", "remove_entity") { Price = 300 },
            new Effect("Bat", "remove_entity_bat", "remove_entity") { Price = 1 },
            new Effect("Bee", "remove_entity_bee", "remove_entity") { Price = 50 },
            new Effect("Blaze", "remove_entity_blaze", "remove_entity") { Price = 300 },
            new Effect("Boat", "remove_entity_boat", "remove_entity") { Price = 100 },
            new Effect("Boat with Chest", "remove_entity_boat", "remove_entity") { Price = 250 },
            new Effect("Cat", "remove_entity_cat", "remove_entity") { Price = 500 },
            new Effect("Cave Spider", "remove_entity_cave_spider", "remove_entity") { Price = 150 },
            new Effect("Chicken", "remove_entity_chicken", "remove_entity") { Price = 150 },
            new Effect("Cod", "remove_entity_cod", "remove_entity") { Price = 150 },
            new Effect("Cow", "remove_entity_cow", "remove_entity") { Price = 150 },
            new Effect("Creeper", "remove_entity_creeper", "remove_entity") { Price = 150 },
            new Effect("Dolphin", "remove_entity_dolphin", "remove_entity") { Price = 250 },
            new Effect("Donkey", "remove_entity_donkey", "remove_entity") { Price = 250 },
            new Effect("Drowned", "remove_entity_drowned", "remove_entity") { Price = 150 },
            new Effect("Elder Guardian", "remove_entity_elder_guardian", "remove_entity") { Price = 750 },
            new Effect("Ender Dragon", "remove_entity_ender_dragon", "remove_entity") { Price = 1000 },
            new Effect("Enderman", "remove_entity_enderman", "remove_entity") { Price = 150 },
            new Effect("Endermite", "remove_entity_endermite", "remove_entity") { Price = 150 },
            new Effect("Evoker", "remove_entity_evoker", "remove_entity") { Price = 200 },
            new Effect("Fox", "remove_entity_fox", "remove_entity") { Price = 200 },
            new Effect("Frog", "remove_entity_frog", "remove_entity") { Price = 200 },
            new Effect("Ghast", "remove_entity_ghast", "remove_entity") { Price = 200 },
            new Effect("Giant", "remove_entity_giant", "remove_entity") { Price = 25 },
            new Effect("Glow Squid", "remove_entity_glow_squid", "remove_entity") { Price = 50 },
            new Effect("Goat", "remove_entity_goat", "remove_entity") { Price = 200 },
            new Effect("Guardian", "remove_entity_guardian", "remove_entity") { Price = 250 },
            new Effect("Hoglin", "remove_entity_hoglin", "remove_entity") { Price = 300 },
            new Effect("Horse", "remove_entity_horse", "remove_entity") { Price = 750 },
            new Effect("Husk", "remove_entity_husk", "remove_entity") { Price = 200 },
            new Effect("Illusioner", "remove_entity_illusioner", "remove_entity") { Price = 300 },
            new Effect("Iron Golem", "remove_entity_iron_golem", "remove_entity") { Price = 300 },
            //new Effect("Lightning Bolt", "remove_entity_lightning", "remove_entity"),
            new Effect("Llama", "remove_entity_llama", "remove_entity") { Price = 200 },
            new Effect("Magma Cube", "remove_entity_magma_cube", "remove_entity") { Price = 300 },
            new Effect("Minecart", "remove_entity_minecart", "remove_entity") { Price = 25 },
            new Effect("Minecart with Chest", "remove_entity_minecart_chest", "remove_entity") { Price = 50 },
            new Effect("Mooshroom", "remove_entity_mushroom_cow", "remove_entity") { Price = 200 },
            new Effect("Mule", "remove_entity_mule", "remove_entity") { Price = 200 },
            new Effect("Ocelot", "remove_entity_ocelot", "remove_entity") { Price = 50 },
            new Effect("Panda", "remove_entity_panda", "remove_entity") { Price = 200 },
            new Effect("Parrot", "remove_entity_parrot", "remove_entity") { Price = 250 },
            new Effect("Phantom", "remove_entity_phantom", "remove_entity") { Price = 200 },
            new Effect("Pig", "remove_entity_pig", "remove_entity") { Price = 150 },
            new Effect("Piglin", "remove_entity_piglin", "remove_entity") { Price = 350 },
            new Effect("Piglin Brute", "remove_entity_piglin_brute", "remove_entity") { Price = 150 },
            new Effect("Pillager", "remove_entity_pillager", "remove_entity") { Price = 150 },
            new Effect("Polar Bear", "remove_entity_polar_bear", "remove_entity") { Price = 200 },
            new Effect("Primed TNT", "remove_entity_primed_tnt", "remove_entity") { Price = 50 },
            new Effect("Pufferfish", "remove_entity_pufferfish", "remove_entity") { Price = 50 },
            new Effect("Rabbit", "remove_entity_rabbit", "remove_entity") { Price = 50 },
            new Effect("Ravager", "remove_entity_ravager", "remove_entity") { Price = 150 },
            new Effect("Salmon", "remove_entity_salmon", "remove_entity") { Price = 50 },
            new Effect("Sheep", "remove_entity_sheep", "remove_entity") { Price = 75 },
            new Effect("Shulker", "remove_entity_shulker", "remove_entity") { Price = 150 },
            new Effect("Silverfish", "remove_entity_silverfish", "remove_entity") { Price = 150 },
            new Effect("Skeleton", "remove_entity_skeleton", "remove_entity") { Price = 150 },
            new Effect("Skeleton Horse", "remove_entity_skeleton_horse", "remove_entity") { Price = 750 },
            new Effect("Slime", "remove_entity_slime", "remove_entity") { Price = 150 },
            new Effect("Snow Golem", "remove_entity_snowman", "remove_entity") { Price = 50 },
            new Effect("Spider", "remove_entity_spider", "remove_entity") { Price = 150 },
            new Effect("Squid", "remove_entity_squid", "remove_entity") { Price = 25 },
            new Effect("Stray", "remove_entity_stray", "remove_entity") { Price = 150 },
            new Effect("Strider", "remove_entity_strider", "remove_entity") { Price = 750 },
            new Effect("Tadpole", "remove_entity_tadpole", "remove_entity") { Price = 200 },
            new Effect("Trader Llama", "remove_entity_trader_llama", "remove_entity") { Price = 50 },
            new Effect("Tropical Fish", "remove_entity_tropical_fish", "remove_entity") { Price = 50 },
            new Effect("Turtle", "remove_entity_turtle", "remove_entity") { Price = 100 },
            new Effect("Vex", "remove_entity_vex", "remove_entity") { Price = 50 },
            new Effect("Villager", "remove_entity_villager", "remove_entity") { Price = 200 },
            new Effect("Vindicator", "remove_entity_vindicator", "remove_entity") { Price = 150 },
            new Effect("Wandering Trader", "remove_entity_wandering_trader", "remove_entity") { Price = 100 },
            new Effect("Warden", "remove_entity_warden", "remove_entity") { Price = 2000 },
            new Effect("Witch", "remove_entity_witch", "remove_entity") { Price = 150 },
            new Effect("Wither", "remove_entity_wither", "remove_entity") { Price = 1000 },
            new Effect("Wither Skeleton", "remove_entity_wither_skeleton", "remove_entity") { Price = 150 },
            new Effect("Wolf", "remove_entity_wolf", "remove_entity") { Price = 200 },
            new Effect("Zoglin", "remove_entity_zoglin", "remove_entity") { Price = 150 },
            new Effect("Zombie", "remove_entity_zombie", "remove_entity") { Price = 150 },
            new Effect("Zombie Horse", "remove_entity_zombie_horse", "remove_entity") { Price = 750 },
            new Effect("Zombie Villager", "remove_entity_zombie_villager", "remove_entity") { Price = 150 },
            new Effect("Zombified Piglin", "remove_entity_zombified_piglin", "remove_entity") { Price = 150 },
            // sets the server difficulty (affects how much damage mobs deal)
            new GlobalEffect("Set Difficulty", "difficulty", ItemKind.Folder),
            new GlobalEffect("Peaceful Mode", "difficulty_peaceful", "difficulty") { Price = 100, Description = "Removes all hostile mobs and prevents new ones spawning" },
            new GlobalEffect("Easy Mode", "difficulty_easy", "difficulty") { Price = 100 },
            new GlobalEffect("Normal Mode", "difficulty_normal", "difficulty") { Price = 200 },
            new GlobalEffect("Hard Mode", "difficulty_hard", "difficulty") { Price = 400 },
            // applies potion effects to every player
            new Effect("Apply Potion (20s)", "apply_potion_effect", ItemKind.Folder),
            new Effect("Absorption", "potion_absorption", "apply_potion_effect") { Price = 50, Description = "Grants extra health that cannot be regenerated" },
            new Effect("Bad Omen", "potion_bad_omen", "apply_potion_effect") { Price = 500, Description = "Causes a village raid when a player possessing this effect is inside of a village" },
            new Effect("Blindness", "potion_blindness", "apply_potion_effect") { Price = 75, Description = "Temporarily reduces a player's range of vision and disables their sprinting" },
            new Effect("Conduit Power", "potion_conduit_power", "apply_potion_effect") { Price = 50, Description = "Grants water breathing, night vision, and haste when underwater" },
            new Effect("Darkness", "potion_darkness", "apply_potion_effect") { Price = 75, Description = "Temporarily reduces a player's range of vision" },
            new Effect("Dolphins Grace", "potion_dolphins_grace", "apply_potion_effect") { Price = 50, Description = "Increases swimming speed" },
            new Effect("Fire Resistance", "potion_fire_resistance", "apply_potion_effect") { Price = 50, Description = "Grants invincibility from fire and lava damage" },
            new Effect("Glowing", "potion_glowing", "apply_potion_effect") { Price = 25, Description = "Gives the player a glowing white outline that can be seen through walls" },
            new Effect("Haste", "potion_haste", "apply_potion_effect") { Price = 50, Description = "Increases mining speed" },
            new Effect("Health Boost", "potion_health_boost", "apply_potion_effect") { Price = 50, Description = "Increases maximum health" },
            new Effect("Invisibility", "potion_invisibility", "apply_potion_effect") { Price = 25, Description = "Makes the player's skin invisible" },
            new Effect("Jump Boost", "potion_jump_boost", "apply_potion_effect") { Price = 50, Description = "Makes the player jump higher" },
            new Effect("Levitation", "potion_levitation", "apply_potion_effect") { Price = 100, Description = "Gradually lifts the player up into the air" },
            new Effect("Mining Fatigue", "potion_mining_fatigue", "apply_potion_effect") { Price = 75, Description = "Decreases mining speed" },
            new Effect("Nausea", "potion_nausea", "apply_potion_effect") { Price = 50, Description = "Makes the player's screen shake" },
            new Effect("Night Vision", "potion_night_vision", "apply_potion_effect") { Price = 50, Description = "Allows the player to see inside dark areas" },
            new Effect("Poison", "potion_poison", "apply_potion_effect") { Price = 50, Description = "Gradually damages the player" },
            new Effect("Regeneration", "potion_regeneration", "apply_potion_effect") { Price = 25, Description = "Gradually heals the player" },
            new Effect("Resistance", "potion_resistance", "apply_potion_effect") { Price = 25, Description = "Reduces damage taken" },
            new Effect("Slow Falling", "potion_slow_falling", "apply_potion_effect") { Price = 25, Description = "Reduces the player's falling speed" },
            new Effect("Slowness", "potion_slowness", "apply_potion_effect") { Price = 50, Description = "Decreases the player's walking speed" },
            new Effect("Speed", "potion_speed", "apply_potion_effect") { Price = 50, Description = "Increases the player's walking speed" },
            new Effect("Strength", "potion_strength", "apply_potion_effect") { Price = 50, Description = "Increases the player's damage output" },
            new Effect("Water Breathing", "potion_water_breathing", "apply_potion_effect") { Price = 50, Description = "Grants the ability to breathe underwater" },
            new Effect("Weakness", "potion_weakness", "apply_potion_effect") { Price = 50, Description = "Decreases the player's damage output" },
            // places a block at everyone's feet
            // TODO: sculk sensor & sculk shrieker blocks? would need to ensure the sculk shrieker can summon the warden (it can't by default)
            new Effect("Place Block", "place_block", ItemKind.Folder),
            new Effect("Bedrock", "block_bedrock", "place_block") { Price = 500, Description = "Places bedrock at the streamer's feet" },
            new Effect("Cobweb", "block_cobweb", "place_block") { Price = 25, Description = "Places a cobweb block on the streamer" },
            new Effect("Fire", "block_fire", "place_block") { Price = 200, Description = "Places a fire block on the streamer" },
            new Effect("Redstone Torch", "block_redstone_torch", "place_block") { Price = 25, Description = "Places a redstone torch on the streamer" },
            new Effect("Sculk Catalyst", "block_sculk_catalyst", "place_block") { Price = 25, Description = "Places a sculk catalyst on the streamer" },
            new Effect("TNT", "block_tnt", "place_block") { Price = 400, Description = "Places a TNT block on the streamer" },
            new Effect("Wither Rose", "block_wither_rose", "place_block") { Price = 100, Description = "Places a wither rose on the streamer" },
            new Effect("Lightning Rod", "block_lightning_rod", "place_block") { Price = 25, Description = "Places a lightning rod on the streamer" },
            new Effect("Water", "block_water", "place_block") { Price = 50, Description = "Places a flowing water block on the streamer" },
            // places a block several blocks above everyone's head
            new Effect("Place Falling Block", "place_falling_block", ItemKind.Folder),
            new Effect("Anvil", "falling_block_anvil", "place_falling_block") { Price = 75, Description = "Drops an anvil block on the streamer" },
            new Effect("Gravel", "falling_block_gravel", "place_falling_block") { Price = 25, Description = "Drops a gravel block on the streamer" },
            new Effect("Red Sand", "falling_block_red_sand", "place_falling_block") { Price = 25, Description = "Drops a red sand block on the streamer" },
            new Effect("Sand", "falling_block_sand", "place_falling_block") { Price = 25, Description = "Drops a sand block on the streamer" },
            // sets the server weather
            new GlobalEffect("Set Weather", "weather", ItemKind.Folder),
            new GlobalEffect("Clear Weather", "clear", "weather") { Price = 25, Description = "Makes the weather sunny to allow rays of fire to shine down on hostile mobs" },
            new GlobalEffect("Rainy Weather", "downfall", "weather") { Price = 50, Description = "Makes the weather rainy which prevents hostile mobs from burning in the daylight" },
            new GlobalEffect("Stormy Weather", "thunder_storm", "weather") { Price = 75, Description = "Starts a thunderstorm with sporadic lightning strikes. Combine with placing a lightning rod to perform some electrocution!" },
            // apply enchants
            new Effect("Enchantments", "enchantments", ItemKind.Folder),
            new Effect("Remove Enchants", "remove_enchants", "enchantments") { Price = 200, Description = "Removes all enchants from the held item" },
            new Effect("Apply Aqua Affinity", "enchant_aqua_affinity", "enchantments") { Price = 50, Description = "Increases underwater mining speed" },
            new Effect("Apply Bane of Arthropods V", "enchant_bane_of_arthropods", "enchantments") { Price = 50, Description = "Increases damage dealt to arthropod mobs (spiders, cave spiders, bees, silverfish, and endermites)" },
            new Effect("Apply Blast Protection IV", "enchant_blast_protection", "enchantments") { Price = 50, Description = "Reduces damage taken from explosions" },
            new Effect("Apply Channeling", "enchant_channeling", "enchantments") { Price = 50, Description = "Makes tridents produce lightning when thrown while raining" },
            new Effect("Apply Curse of Binding", "enchant_curse_of_binding", "enchantments") { Price = 50, Description = "Armor pieces with this enchantment cannot be taken off until death" },
            new Effect("Apply Curse of Vanishing", "enchant_curse_of_vanishing", "enchantments") { Price = 50, Description = "Items with this enchantment will disappear upon death" },
            new Effect("Apply Depth Strider III", "enchant_depth_strider", "enchantments") { Price = 50, Description = "Increases swimming speed" },
            new Effect("Apply Efficiency V", "enchant_efficiency", "enchantments") { Price = 50, Description = "Increases mining speed" },
            new Effect("Apply Feather Falling IV", "enchant_feather_falling", "enchantments") { Price = 50, Description = "Reduces damage taken from fall damage" },
            new Effect("Apply Fire Aspect II", "enchant_fire_aspect", "enchantments") { Price = 50, Description = "Melee weapons with this enchantment set mobs on fire upon dealing damage" },
            new Effect("Apply Fire Protection IV", "enchant_fire_protection", "enchantments") { Price = 50, Description = "Reduces damage taken from fire" },
            new Effect("Apply Flame", "enchant_flame", "enchantments") { Price = 50, Description = "Bows with this enchantment shoot flaming arrows that set mobs on fire" },
            new Effect("Apply Fortune III", "enchant_fortune", "enchantments") { Price = 50, Description = "Increases the drops of minerals (iron, diamond, etc.)" },
            new Effect("Apply Frost Walker II", "enchant_frost_walker", "enchantments") { Price = 50, Description = "Walking near water with this enchantment will temporarily turn it into ice" },
            new Effect("Apply Impaling V", "enchant_impaling", "enchantments") { Price = 50, Description = "Increases damage dealt by tridents to aquatic mobs" },
            new Effect("Apply Infinity", "enchant_infinity", "enchantments") { Price = 50, Description = "Prevents bows from consuming arrows" },
            new Effect("Apply Knockback II", "enchant_knockback", "enchantments") { Price = 50, Description = "Increases the distance that mobs get knocked back when attacked by a melee weapon" },
            new Effect("Apply Looting III", "enchant_looting", "enchantments") { Price = 50, Description = "Increases the number of items dropped by mobs when killed" },
            new Effect("Apply Loyalty III", "enchant_loyalty", "enchantments") { Price = 50, Description = "Tridents with this enchantment will return to the thrower" },
            new Effect("Apply Luck of the Sea III", "enchant_luck_of_the_sea", "enchantments") { Price = 50, Description = "Increases luck while fishing" },
            new Effect("Apply Lure III", "enchant_lure", "enchantments") { Price = 50, Description = "Decreases the wait time for a bite on your fishing hook" },
            new Effect("Apply Mending", "enchant_mending", "enchantments") { Price = 50, Description = "Items with this enchantment that are held or worn by a player will be repaired as XP is collected" },
            new Effect("Apply Multishot", "enchant_multishot", "enchantments") { Price = 50, Description = "Makes crossbows fire three arrows" },
            new Effect("Apply Piercing IV", "enchant_piercing", "enchantments") { Price = 50, Description = "Lets crossbow arrows penetrate four mobs" },
            new Effect("Apply Power V", "enchant_power", "enchantments") { Price = 50, Description = "Increases damage dealt by bows" },
            new Effect("Apply Projectile Protection IV", "enchant_projectile_protection", "enchantments") { Price = 50, Description = "Reduces damage taken from arrows" },
            new Effect("Apply Protection IV", "enchant_protection", "enchantments") { Price = 50, Description = "Reduces damage taken from all sources" },
            new Effect("Apply Punch II", "enchant_punch", "enchantments") { Price = 50, Description = "Increases the distance that mobs get knocked back when shot by a bow" },
            new Effect("Apply Quick Charge III", "enchant_quick_charge", "enchantments") { Price = 50, Description = "Reduces the time required to charge a crossbow" },
            new Effect("Apply Respiration III", "enchant_respiration", "enchantments") { Price = 50, Description = "Extends breathing time underwater" },
            new Effect("Apply Riptide III", "enchant_riptide", "enchantments") { Price = 50, Description = "When throwing a riptide trident inside rain or a body of water, the thrower will be rocketed in the direction they are facing" },
            new Effect("Apply Sharpness V", "enchant_sharpness", "enchantments") { Price = 50, Description = "Increases damage dealt by melee damage" },
            new Effect("Apply Silk Touch", "enchant_silk_touch", "enchantments") { Price = 50, Description = "Allows various blocks to drop themselves instead of their usual items" },
            new Effect("Apply Smite V", "enchant_smite", "enchantments") { Price = 50, Description = "Increases damage dealt to undead mobs (zombies, skeletons, etc.)" },
            new Effect("Apply Soul Speed III", "enchant_soul_speed", "enchantments") { Price = 50, Description = "Increases walking speed on soul sand at the cost of armor durability" },
            new Effect("Apply Sweeping Edge III", "enchant_sweeping_edge", "enchantments") { Price = 50, Description = "Increases the damage done by sweeping attacks" },
            new Effect("Apply Swift Sneak III", "enchant_swift_sneak", "enchantments") { Price = 50, Description = "Increases sneaking speed" },
            new Effect("Apply Thorns III", "enchant_thorns", "enchantments") { Price = 50, Description = "Deals damage to attackers when hit" },
            new Effect("Apply Unbreaking III", "enchant_unbreaking", "enchantments") { Price = 50, Description = "Lessens the speed at which items break" },

            // TODO: add goat horns to give/remove items?

            // gives 1 item
            new Effect("Give an Item", "give_item", ItemKind.Folder),
            new Effect("Elytra", "give_elytra", "give_item") { Price = 500 },
            new Effect("Eye of Ender", "give_ender_eye", "give_item") { Price = 100 },
            new Effect("End Portal Frame", "give_end_portal_frame", "give_item") { Price = 300 },
            new Effect("Recovery Compass", "give_recovery_compass", "give_item") { Price = 700 },
            new Effect("Trident", "give_trident", "give_item") { Price = 500 },

            new Effect("Food", "give_food", ItemKind.Folder, "give_item"),
            new Effect("Cooked Porkchop", "give_cooked_porkchop", "give_food") { Price = 20 },
            new Effect("Golden Apple", "give_golden_apple", "give_food") { Price = 200 },
            new Effect("Enchanted Golden Apple", "give_enchanted_golden_apple", "give_food") { Price = 300 },

            new Effect("Minerals", "give_minerals", ItemKind.Folder, "give_item"),
            new Effect("Coal", "give_coal", "give_minerals") { Price = 20 },
            new Effect("Iron Ingot", "give_iron_ingot", "give_minerals") { Price = 300 },
            new Effect("Gold Ingot", "give_gold_ingot", "give_minerals") { Price = 300 },
            new Effect("Netherite Ingot", "give_netherite_ingot", "give_minerals") { Price = 300 },
            new Effect("Diamond", "give_diamond", "give_minerals") { Price = 300 },

            new Effect("Tools", "give_tools", ItemKind.Folder, "give_item"),
            new Effect("Wooden Pickaxe", "give_wooden_pickaxe", "give_tools") { Price = 25 },
            new Effect("Stone Pickaxe", "give_stone_pickaxe", "give_tools") { Price = 50 },
            new Effect("Golden Pickaxe", "give_golden_pickaxe", "give_tools") { Price = 25 },
            new Effect("Iron Pickaxe", "give_iron_pickaxe", "give_tools") { Price = 100 },
            new Effect("Diamond Pickaxe", "give_diamond_pickaxe", "give_tools") { Price = 250 },
            new Effect("Netherite Pickaxe", "give_netherite_pickaxe", "give_tools") { Price = 350 },

            new Effect("Weapons", "give_weapons", ItemKind.Folder, "give_item"),
            new Effect("Wooden Sword", "give_wooden_sword", "give_weapons") { Price = 25 },
            new Effect("Stone Sword", "give_stone_sword", "give_weapons") { Price = 50 },
            new Effect("Golden Sword", "give_golden_sword", "give_weapons") { Price = 25 },
            new Effect("Iron Sword", "give_iron_sword", "give_weapons") { Price = 100 },
            new Effect("Diamond Sword", "give_diamond_sword", "give_weapons") { Price = 250 },
            new Effect("Netherite Sword", "give_netherite_sword", "give_weapons") { Price = 350 },

            // takes 1 item
            new Effect("Take an Item", "take_item", ItemKind.Folder),
            new Effect("Elytra", "take_elytra", "take_item") { Price = 1000 },
            new Effect("Eye of Ender", "take_ender_eye", "take_item") { Price = 300 },
            new Effect("End Portal Frame", "take_end_portal_frame", "take_item") { Price = 600 },
            new Effect("Recovery Compass", "take_recovery_compass", "give_item") { Price = 1250 },
            new Effect("Trident", "take_trident", "take_item") { Price = 1000 },

            new Effect("Food", "take_food", ItemKind.Folder, "take_item"),
            new Effect("Cooked Porkchop", "take_cooked_porkchop", "take_food") { Price = 50 },
            new Effect("Golden Apple", "take_golden_apple", "take_food") { Price = 500 },
            new Effect("Enchanted Golden Apple", "take_enchanted_golden_apple", "take_food") { Price = 600 },

            new Effect("Minerals", "take_minerals", ItemKind.Folder, "take_item"),
            new Effect("Coal", "take_coal", "take_minerals") { Price = 50 },
            new Effect("Iron Ingot", "take_iron_ingot", "take_minerals") { Price = 200 },
            new Effect("Gold Ingot", "take_gold_ingot", "take_minerals") { Price = 200 },
            new Effect("Netherite Ingot", "take_netherite_ingot", "take_minerals") { Price = 700 },
            new Effect("Diamond", "take_diamond", "take_minerals") { Price = 500 },

            new Effect("Tools", "take_tools", ItemKind.Folder, "take_item"),
            new Effect("Wooden Pickaxe", "take_wooden_pickaxe", "take_tools") { Price = 50 },
            new Effect("Stone Pickaxe", "take_stone_pickaxe", "take_tools") { Price = 75 },
            new Effect("Golden Pickaxe", "take_golden_pickaxe", "take_tools") { Price = 50 },
            new Effect("Iron Pickaxe", "take_iron_pickaxe", "take_tools") { Price = 300 },
            new Effect("Diamond Pickaxe", "take_diamond_pickaxe", "take_tools") { Price = 600 },
            new Effect("Netherite Pickaxe", "take_netherite_pickaxe", "take_tools") { Price = 750 },

            new Effect("Weapons", "take_weapons", ItemKind.Folder, "take_item"),
            new Effect("Wooden Sword", "take_wooden_sword", "take_weapons") { Price = 50 },
            new Effect("Stone Sword", "take_stone_sword", "take_weapons") { Price = 75 },
            new Effect("Golden Sword", "take_golden_sword", "take_weapons") { Price = 50 },
            new Effect("Iron Sword", "take_iron_sword", "take_weapons") { Price = 300 },
            new Effect("Diamond Sword", "take_diamond_sword", "take_weapons") { Price = 600 },
            new Effect("Netherite Sword", "take_netherite_sword", "take_weapons") { Price = 750 },
        };

        private static readonly Regex UnavailableEffectPattern = new(@"^.+ \[effect: ([a-zA-Z0-9_])\]$", RegexOptions.Compiled);

        public override List<Effect> Effects => AllEffects;

        public override SimpleTCPClientConnector? Connector
        {
            get => base.Connector;
            set
            {
                if (value != null)
                {
                    value.MessageParsed += OnMessageParsed;
                }
                base.Connector = value;
            }
        }

        private void OnMessageParsed(ISimpleTCPConnector<Request, Response, ISimpleTCPContext.NullContext> sender, Response response, ISimpleTCPContext.NullContext context)
        {
            Log.Debug("Parsing incoming message #" + response.id
                                                   + " of type " + response.type
                                                   + " with message \"" + response.message + "\"");
            if (response.message == null)
            {
                Log.Debug("Message has no message attribute; exiting");
                return;
            }

            if (response.id == 0)
            {
                Log.Debug("Message is ID #0");
                if (response.message.StartsWith("_mc_cc_server_status_"))
                {
                    Log.Debug("Message is server status packet");
                    // incoming packet contains info about supported effects
                    OnServerStatusPacket(response);
                }
                else if (response.message.StartsWith("_mc_cc_hide_effects_:"))
                {
                    Log.Debug("Message is hide effects packet");
                    OnMenuStatusPacket(response, EffectStatus.MenuHidden);
                }
                else if (response.message.StartsWith("_mc_cc_show_effects_:"))
                {
                    Log.Debug("Message is show effects packet");
                    OnMenuStatusPacket(response, EffectStatus.MenuVisible);
                }
            }
            else if (response.status == EffectResult.Unavailable)
            {
                Log.Debug("Effect is unavailable");
                // a requested effect was unavailable; it should be hidden from the menu
                OnUnavailablePacket(response);
            }
        }

        private void OnServerStatusPacket(Response response)
        {
            // load packet data
            var payload = response.message.Replace("_mc_cc_server_status_", "");
            var status = JsonConvert.DeserializeObject<ServerStatus>(payload);
            if (status == null)
            {
                Log.Error("Message payload could not be converted to ServerStatus object");
                return;
            }

            // reset all effects back to visible first
            AllEffects.ForEach(effect => ReportStatus(effect, EffectStatus.MenuVisible));

            // hide global effects if they are not usable
            if (!status.GlobalEffects)
            {
                // does the folder actually get hidden? if so, do its contents also get hidden from search?
                AllEffects.FindAll(effect => effect is GlobalEffect).ForEach(HideEffect);
            }
            // hide client effects if running on a server
            if (!status.ClientEffects)
            {
                AllEffects.FindAll(effect => effect is ClientEffect).ForEach(HideEffect);
            }

            // hide effects that are unsupported by the platform
            var registeredEffectsList = new List<string>(status.RegisteredEffects).ConvertAll(input => input.ToLower());
            AllEffects.FindAll(effect => effect.Kind == ItemKind.Effect && !registeredEffectsList.Contains(effect.Code.ToLower()))
                .ForEach(HideEffect);
            
            Log.Debug("Finished hiding effects");
        }

        private void OnMenuStatusPacket(Response response, EffectStatus status)
        {
            var effectsList = new List<string>(response.message.Split(':')[1].Split(','));
            AllEffects.FindAll(effect => effectsList.Contains(effect.Code)).ForEach(effect =>
            {
                ReportStatus(effect, status);
                Log.Message("Updated visibility of " + effect.Code + " to " + status);
            });
        }

        private void OnUnavailablePacket(Response response)
        {
            var match = UnavailableEffectPattern.Match(response.message);
            if (!match.Success)
            {
                Log.Error("Unavailable effect pattern match failed on \"" + response.message + "\"");
                return;
            }
            var effectCode = match.Groups[0].Value;
            var effect = AllEffects.Find(e => e.Code == effectCode);
            if (effect == null)
            {
                Log.Error("Could not find unavailable effect \"" + effect + "\" in known effect list");
                return;
            }
            HideEffect(effect);
        }

        private void HideEffect(Effect effect)
        {
            Log.Message("Permanently hiding effect " + effect.Code);
            ReportStatus(effect, EffectStatus.MenuHidden);
        }
    }
}
