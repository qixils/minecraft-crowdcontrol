﻿// ReSharper disable RedundantUsingDirective
// (these imports are required by CC)
using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Text.RegularExpressions;
using ConnectorLib;
using ConnectorLib.JSON;
using ConnectorLib.SimpleTCP;
using CrowdControl.Common;
using Newtonsoft.Json;
using ConnectorType = CrowdControl.Common.ConnectorType;
using EffectStatus = CrowdControl.Common.EffectStatus;
using Log = CrowdControl.Common.Log;
using LogLevel = CrowdControl.Common.LogLevel;

namespace CrowdControl.Games.Packs
{
    [SuppressMessage("Interoperability", "CA1416:Validate platform compatibility")]
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
            new Effect("Annoy Players", "toast", "miscellaneous") { Price = 50, Description = "Plays an obnoxious animation and an obnoxious sound" },
            new Effect("Dig Hole", "dig", "miscellaneous") { Price = 200, Description = "Digs a small hole underneath the streamer" },
            new Effect("Do-or-Die", "do_or_die", "miscellaneous") { Price = 500, Description = "Gives the streamer a task to complete within 30 seconds or else they die" },
            new Effect("Eat Chorus Fruit", "chorus_fruit", "miscellaneous") { Price = 75, Description = "Teleports the player to a random nearby block as if they ate a Chorus Fruit" },
            // disabled because this is killing anyone in the air: new Effect("Explode", "explode", "miscellaneous") { Price = 750, Description = "Spawns a TNT-like explosion at the streamer's feet" },
            new Effect("Fling Randomly", "fling", "miscellaneous") { Price = 100, Description = "Flings the streamer in a totally random direction" },
            new Effect("Flip Mobs Upside-Down", "dinnerbone", "miscellaneous") { Price = 50, Description = "Flips nearby mobs upside-down by naming them after the iconic Minecraft developer Dinnerbone" },
            new Effect("Invert Camera", "invert_look", "miscellaneous") { Price = 200, Duration = 15, Description = "Temporarily inverts mouse movement" },
            new Effect("Invert Controls", "invert_wasd", "miscellaneous") { Price = 200, Duration = 15, Description = "Temporarily inverts WASD movement" },
            new Effect("Open Lootbox", "lootbox", "miscellaneous") { Price = 100, Description = "Gifts a completely random item with varying enchants and modifiers" },
            new Effect("Open Lucky Lootbox", "lootbox_5", "miscellaneous") { Price = 500, Description = "Gifts two random items with vastly higher odds of having beneficial enchantments and modifiers" },
            new Effect("Place Flowers", "flowers", "miscellaneous") { Price = 25, Description = "Randomly places flowers nearby" },
            new Effect("Place Torches", "lit", "miscellaneous") { Price = 100, Description = "Places torches on every nearby block" },
            new Effect("Plant Tree", "plant_tree", "miscellaneous") { Price = 100, Description = "Plant a tree on top of the streamer" },
            new Effect("Remove Torches", "dim", "miscellaneous") { Price = 200, Description = "Removes all nearby torches" },
            new Effect("Replace Area with Gravel", "gravel_hell", "miscellaneous") { Price = 200, Description = "Replaces nearby blocks with gravel" },
            new Effect("Respawn Player", "respawn", "miscellaneous") { Price = 500, Description = "Sends the streamer to their spawn point" },
            new Effect("Spawn Ore Veins", "vein", "miscellaneous") { Price = 100, Description = "Places random ore veins (ore lava) near the streamer" },
            new Effect("Spooky Sound Effect", "sfx", "miscellaneous") { Price = 100, Description = "Plays a random spooky sound effect" },
            new Effect("Swap Locations", "swap", "miscellaneous") { Price = 1000, Description = "Swaps the locations of all players participating in a multiplayer Crowd Control session" },
            new Effect("Teleport to a Nearby Structure", "structure", "miscellaneous") { Price = 750, Description = "Teleports players to a random nearby structure" },
            new Effect("Teleport to a Random Biome", "biome", "miscellaneous") { Price = 750, Description = "Teleports players to a random nearby biome" },
            new Effect("Teleport All Entities To Players", "entity_chaos", "miscellaneous") { Price = 3000, Description = "Teleports every loaded mob on the server to the targeted streamers in an even split. Depending on the server configuration, this may only teleport nearby mobs." },
            new Effect("Water Bucket Clutch", "bucket_clutch", "miscellaneous") { Price = 400, Description = "Teleports players 30 blocks up and gives them a water bucket" },
            // time commands
            new Effect("Time", "time", ItemKind.Folder),
            new Effect("Set Time to Day", "time_day", "time") { Price = 50, Description = "Jumps the clock ahead to daytime" },
            new Effect("Set Time to Night", "time_night", "time") { Price = 50, Description = "Jumps the clock ahead to nighttime" },
            // disabled because i think the above commands are just better: new GlobalEffect("Zip Time", "zip", "time") { Price = 30, Description = "Adds several minutes to the in-game time" },
            // health commands
            new Effect("Health", "health", ItemKind.Folder),
            new Effect("Take Max Health", "max_health_sub", new[]{"halfhealth10"}, "health") { Price = 100, Description = "Subtracts from the streamer's max health" },
            new Effect("Give Max Health", "max_health_add", new[]{"halfhealth10"}, "health") { Price = 50, Description = "Adds to the streamer's max health" },
            new Effect("Damage Player", "damage", new[]{"health10"}, "health") { Price = 25, Description = "Removes health from the streamer (unless it would kill them)" },
            new Effect("Heal Player", "heal", new[]{"health10"}, "health") { Price = 10, Description = "Increases the streamer's health" },
            new Effect("Heal Player to Full", "full_heal", "health") { Price = 50, Description = "Resets the streamer's health to full" },
            new Effect("Halve Health", "half_health", "health") { Price = 125, Description = "Sets the streamer's health to 50% of what they currently have" },
            new Effect("Kill Player", "kill", "health") { Price = 1000, Description = "Immediately kills the streamer on the spot" },
            new Effect("Invincible", "invincible", "health") { Price = 50, Description = "Temporarily makes the streamer immune to damage", Duration = 15 },
            new Effect("One-Hit KO", "ohko", "health") { Price = 250, Description = "Temporarily makes any damage source kill the streamer in one hit", Duration = 15 },
            // food commands
            new Effect("Food", "food", ItemKind.Folder),
            new Effect("Feed Player", "feed", new[]{"food10"}, "food") { Price = 10, Description = "Replenishes some hunger" },
            new Effect("Feed Player to Full", "full_feed", "food") { Price = 100, Description = "Replenishes the hunger bar" },
            new Effect("Remove Food", "starve", new[]{"food10"}, "food") { Price = 50, Description = "Removes some food" },
            new Effect("Starve Players", "full_starve", "food") { Price = 400, Description = "Drains the players' hunger bar" },
            // experience
            new Effect("Experience", "experience", ItemKind.Folder),
            new Effect("Give XP", "xp_add", new[]{"xp100"}, "experience") { Price = 25, Description = "Adds experience levels" },
            new Effect("Take XP", "xp_sub", new[]{"xp100"}, "experience") { Price = 100, Description = "Removes experience levels" },
            new Effect("Reset Experience", "reset_exp_progress", "experience") { Price = 500, Description = "Clears all of the streamer's XP" },
            // gravity commands
            new Effect("Gravity", "gravity", ItemKind.Folder),
            new Effect("High Gravity", "high_gravity", "gravity") { Price = 100, Duration = 20, Description = "Increases the streamer's gravity" },
            new Effect("Low Gravity", "low_gravity", "gravity") { Price = 50, Duration = 20, Description = "Decreases the streamer's gravity" },
            new Effect("Maximum Gravity", "maximum_gravity", "gravity") { Price = 200, Duration = 20, Description = "Maximizes the streamer's gravity" },
            new Effect("Zero Gravity", "zero_gravity", "gravity") { Price = 100, Duration = 20, Description = "Disables the streamer's gravity" },
            // freeze-type commands
            new Effect("Freezing", "freezing", ItemKind.Folder),
            new Effect("Can't Move", "freeze", "freezing") { Price = 100, Duration = 15, Description = "Temporarily prohibits movement" },
            new Effect("Lock Camera", "camera_lock", "freezing") { Price = 100, Duration = 15, Description = "Temporarily freeze the streamer's camera" },
            new Effect("Lock Camera To Ground", "camera_lock_to_ground", "freezing") { Price = 200, Duration = 15, Description = "Temporarily locks the streamer's camera to the ground" },
            new Effect("Lock Camera To Sky", "camera_lock_to_sky", "freezing") { Price = 200, Duration = 15, Description = "Temporarily locks the streamer's camera to the sky" },
            new Effect("Disable Jumping", "disable_jumping", "freezing") { Price = 100, Duration = 15, Description = "Temporarily prevents the streamer from jumping" },
            // inventory commands
            new Effect("Inventory", "inventory", ItemKind.Folder),
            new Effect("Clear Inventory", "clear_inventory", "inventory") { Price = 750, Description = "Wipes every item from the streamer's inventory" },
            new Effect("Clutter Inventory", "clutter", "inventory") { Price = 50, Description = "Shuffles around items in the streamer's inventory" },
            new Effect("Damage Item", "damage_item", "inventory") { Price = 100, Description = "Takes 25% of the durability of a held or worn item" },
            new Effect("Delete Held Item", "delete_item", "inventory") { Price = 200, Description = "Deletes whatever item the streamer is currently holding" },
            new Effect("Delete Random Item", "delete_random_item", "inventory") { Price = 150, Description = "Deletes a random item from the streamer's inventory" },
            new Effect("Disable Keep Inventory", "keep_inventory_off", "inventory") { Price = 300, Description = "Disallows the streamer from keeping their inventory upon death" },
            new Effect("Drop Held Item", "drop_item", "inventory") { Price = 25, Description = "Makes the streamer drop their held item" },
            new Effect("Enable Keep Inventory", "keep_inventory_on", "inventory") { Price = 100, Description = "Allows the streamer to keep their inventory upon death" },
            new Effect("Put Held Item on Head", "hat", "inventory") { Price = 50, Description = "Moves the item in the streamer's hand to their head" },
            new Effect("Repair Item", "repair_item", "inventory") { Price = 50, Description = "Fully repairs a damaged item" },
            // set gamemode for x seconds
            new Effect("Change Gamemode", "change_gamemode", ItemKind.Folder),
            new Effect("Adventure Mode", "adventure_mode", "change_gamemode") { Price = 200, Duration = 20, Description = "Temporarily sets the streamer to Adventure mode, rendering them unable to place or break blocks" },
            new Effect("Creative Mode", "creative_mode", "change_gamemode") { Price = 200, Duration = 20, Description = "Temporarily sets the streamer to Creative mode, allowing them to fly and spawn in items" },
            new Effect("Spectator Mode", "spectator_mode", "change_gamemode") { Price = 200, Duration = 10, Description = "Temporarily sets the streamer to Spectator mode, turning them into a ghost that can fly through blocks" },
            new Effect("Allow Flight", "flight", "change_gamemode") { Price = 150, Duration = 20, Description = "Temporarily allows the streamer to fly" },
            // summons a mob around each player
            new Effect("Summon Entity", "summon_entity", ItemKind.Folder),
            new Effect("Allay", "entity_allay", "summon_entity") { Price = 100, Description = "Spawns an Allay, a friendly creature who helps you find items" },
            new Effect("Armor Stand", "entity_armor_stand", "summon_entity") { Price = 150, Description = "Spawns an Armor Stand, a decorative entity that has a chance of spawning with valuable armor" },
            new Effect("Axolotl", "entity_axolotl", "summon_entity") { Price = 100, Description = "Spawns an Axolotl, a cute and friendly amphibian" },
            new Effect("Bat", "entity_bat", "summon_entity") { Price = 10, Description = "Spawns a Bat, a passive animal that does little more than fly around and squeak" },
            new Effect("Bee", "entity_bee", "summon_entity") { Price = 100, Description = "Spawns a Bee, a neutral animal that passively pollinates crops and angers when attacked" },
            new Effect("Blaze", "entity_blaze", "summon_entity") { Price = 300, Description = "Spawns a Blaze, an enemy that shoots fireballs at players" },
            new Effect("Boat", "entity_boat", "summon_entity") { Price = 50, Description = "Spawns a Boat, a vehicle that can be used to travel across water" },
            new Effect("Boat with Chest", "entity_chest_boat", "summon_entity") { Price = 150, Description = "Spawns a Boat with Chest, a vehicle that can be used to travel across water and store items. Comes filled with items from a random loot table." },
            new Effect("Camel", "entity_camel", "summon_entity") { Price = 100, Description = "Spawns a Camel, a passive animal that can be tamed and ridden" },
            new Effect("Cat", "entity_cat", "summon_entity") { Price = 100, Description = "Spawns a Cat, a passive animal that can be tamed to follow you around" },
            new Effect("Cave Spider", "entity_cave_spider", "summon_entity") { Price = 300, Description = "Spawns a Cave Spider, an enemy that inflicts poison when it attacks" },
            new Effect("Charged Creeper", "entity_charged_creeper", "summon_entity") { Price = 750, Description = "Spawns a Charged Creeper, an enemy that creates an extra powerful explosion to attack" },
            new Effect("Chicken", "entity_chicken", "summon_entity") { Price = 100, Description = "Spawns a Chicken, a passive animal that can be used as a source of food" },
            new Effect("Cod", "entity_cod", "summon_entity") { Price = 100, Description = "Spawns a Cod, a passive fish that can be used as a source of food" },
            new Effect("Cow", "entity_cow", "summon_entity") { Price = 100, Description = "Spawns a Cow, a passive animal that can be used as a source of food or milk" },
            new Effect("Creeper", "entity_creeper", "summon_entity") { Price = 300, Description = "Spawns a Creeper, an enemy that explodes as an attack" },
            new Effect("Dolphin", "entity_dolphin", "summon_entity") { Price = 100, Description = "Spawns a Dolphin, a passive animal that can give you a speed boost when swimming alongside it" },
            new Effect("Donkey", "entity_donkey", "summon_entity") { Price = 100, Description = "Spawns a Donkey, a passive animal that can be tamed and ridden" },
            new Effect("Drowned", "entity_drowned", "summon_entity") { Price = 200, Description = "Spawns a Drowned, an enemy that can breathe underwater and sometimes attack with a trident" },
            new Effect("Elder Guardian", "entity_elder_guardian", "summon_entity") { Price = 1000, Description = "Spawns an Elder Guardian, an enemy with high thorns damage that attacks with a laser beam" },
            new Effect("Ender Dragon", "entity_ender_dragon", "summon_entity") { Price = 2000, Description = "Spawns an Ender Dragon, a boss that flies around the world, destroying blocks and attacking players" },
            new Effect("Enderman", "entity_enderman", "summon_entity") { Price = 300, Description = "Spawns an Enderman, a neutral enemy that teleports around and angers when attacked or looked at" },
            new Effect("Endermite", "entity_endermite", "summon_entity") { Price = 250, Description = "Spawns an Endermite, a tiny enemy that attacks players and Endermen" },
            new Effect("Evoker", "entity_evoker", "summon_entity") { Price = 600, Description = "Spawns an Evoker, an enemy that attacks by summoning Vexes and armor-piercing fangs. Drops a Totem of Undying when killed." },
            new Effect("Fox", "entity_fox", "summon_entity") { Price = 100, Description = "Spawns a Fox, a passive animal that can be tamed to fight by your side" },
            new Effect("Frog", "entity_frog", "summon_entity") { Price = 100, Description = "Spawns a Frog, a passive animal that can eat Slimes" },
            new Effect("Ghast", "entity_ghast", "summon_entity") { Price = 500, Description = "Spawns a Ghast, an enemy that shoots explosive fireballs at players" },
            new Effect("Giant", "entity_giant", "summon_entity") { Price = 100, Description = "Spawns a Giant, an unused enemy that does nothing other than being tall and obnoxious" },
            new Effect("Glow Squid", "entity_glow_squid", "summon_entity") { Price = 100, Description = "Spawns a Glow Squid, a passive animal that emits light" },
            new Effect("Goat", "entity_goat", "summon_entity") { Price = 100, Description = "Spawns a Goat, a passive animal that can be milked and sometimes lunges at players" },
            new Effect("Guardian", "entity_guardian", "summon_entity") { Price = 300, Description = "Spawns a Guardian, an enemy that attacks with a laser beam" },
            new Effect("Hoglin", "entity_hoglin", "summon_entity") { Price = 800, Description = "Spawns a Hoglin, a hostile enemy that can be used as a source of food" },
            new Effect("Horse", "entity_horse", "summon_entity") { Price = 100, Description = "Spawns a Horse, a passive animal that can be tamed and ridden" },
            new Effect("Husk", "entity_husk", "summon_entity") { Price = 200, Description = "Spawns a Husk, an enemy that spawns in deserts and inflicts hunger when it attacks" },
            new Effect("Illusioner", "entity_illusioner", "summon_entity") { Price = 400, Description = "Spawns an Illusioner, an unused enemy that attacks by summoning illusions" },
            new Effect("Iron Golem", "entity_iron_golem", "summon_entity") { Price = 300, Description = "Spawns an Iron Golem, a passive creature that attacks enemies and can be used as a source of iron" },
            new Effect("Lightning Bolt", "entity_lightning", "summon_entity") { Price = 300, Description = "Spawns a Lightning Bolt, inflicting damage and fire burns to the player and nearby entities" },
            new Effect("Llama", "entity_llama", "summon_entity") { Price = 100, Description = "Spawns a Llama, a passive animal that can be used to transport items" },
            new Effect("Magma Cube", "entity_magma_cube", "summon_entity") { Price = 400, Description = "Spawns a Magma Cube, a stronger version of the Slime enemy" },
            new Effect("Minecart", "entity_minecart", "summon_entity") { Price = 25, Description = "Spawns a Minecart, a vehicle that can be used to travel across railways" },
            new Effect("Minecart with Chest", "entity_minecart_chest", "summon_entity") { Price = 100, Description = "Spawns a Minecart with Chest, a vehicle that can be used to travel across railways and store items. Comes filled with items from a random loot table." },
            new Effect("Mooshroom", "entity_mushroom_cow", "summon_entity") { Price = 100, Description = "Spawns a Mooshroom, a passive animal that can be used as a source of food, milk, or stew" },
            new Effect("Mule", "entity_mule", "summon_entity") { Price = 100, Description = "Spawns a Mule, a passive animal that can be ridden and used to transport items" },
            new Effect("Ocelot", "entity_ocelot", "summon_entity") { Price = 100, Description = "Spawns an Ocelot, a passive animal that hunts down chickens and baby turtles. Not to be confused with the Cat." },
            new Effect("Panda", "entity_panda", "summon_entity") { Price = 100, Description = "Spawns a Panda, a neutral animal that is protective of its cubs" },
            new Effect("Parrot", "entity_parrot", "summon_entity") { Price = 100, Description = "Spawns a Parrot, a passive animal that can be tamed to follow you around and imitate other mobs" },
            new Effect("Phantom", "entity_phantom", "summon_entity") { Price = 300, Description = "Spawns a Phantom, an airborne enemy that attacks insomniacs" },
            new Effect("Pig", "entity_pig", "summon_entity") { Price = 100, Description = "Spawns a Pig, a passive animal that can be used as a source of food" },
            new Effect("Piglin", "entity_piglin", "summon_entity") { Price = 300, Description = "Spawns a Piglin, a hostile enemy that can be bartered with" },
            new Effect("Piglin Brute", "entity_piglin_brute", "summon_entity") { Price = 1000, Description = "Spawns a Piglin Brute, a merciless enemy that can kill an unarmed player in 2-3 hits" },
            new Effect("Pillager", "entity_pillager", "summon_entity") { Price = 350, Description = "Spawns a Pillager, an enemy that attacks with a crossbow" },
            new Effect("Polar Bear", "entity_polar_bear", "summon_entity") { Price = 100, Description = "Spawns a Polar Bear, a neutral animal that is protective of its cubs" },
            new Effect("Primed TNT", "entity_primed_tnt", "summon_entity") { Price = 500, Description = "Spawns a Primed TNT which explodes after a short delay" },
            new Effect("Pufferfish", "entity_pufferfish", "summon_entity") { Price = 300, Description = "Spawns a Pufferfish, a passive animal that inflicts poison when threatened" },
            new Effect("Rabbit", "entity_rabbit", "summon_entity") { Price = 100, Description = "Spawns a Rabbit, a passive animal that can be used as a source of food" },
            new Effect("Ravager", "entity_ravager", "summon_entity") { Price = 1000, Description = "Spawns a Ravager, a hostile enemy that attacks with a powerful bite" },
            new Effect("Salmon", "entity_salmon", "summon_entity") { Price = 100, Description = "Spawns a Salmon, a passive animal that can be used as a source of food" },
            new Effect("Sheep", "entity_sheep", "summon_entity") { Price = 100, Description = "Spawns a Sheep, a passive animal that can be used as a source of wool and food" },
            new Effect("Shulker", "entity_shulker", "summon_entity") { Price = 400, Description = "Spawns a Shulker, a hostile enemy that attacks by shooting projectiles that make the player levitate" },
            new Effect("Silverfish", "entity_silverfish", "summon_entity") { Price = 300, Description = "Spawns a Silverfish, a hostile enemy that attacks and hides in blocks" },
            new Effect("Skeleton", "entity_skeleton", "summon_entity") { Price = 300, Description = "Spawns a Skeleton, a hostile enemy that attacks with a bow" },
            new Effect("Skeleton Horse", "entity_skeleton_horse", "summon_entity") { Price = 100, Description = "Spawns a Skeleton Horse, a passive animal that can be tamed and ridden" },
            new Effect("Slime", "entity_slime", "summon_entity") { Price = 300, Description = "Spawns a Slime, a hostile enemy that attacks by jumping on the player" },
            new Effect("Sniffer", "entity_sniffer", "summon_entity") { Price = 100, Description = "Spawns a Sniffer, a passive creature that digs seeds out of the ground" },
            new Effect("Snow Golem", "entity_snowman", "summon_entity") { Price = 100, Description = "Spawns a Snow Golem, a passive creature that throws snowballs at hostile mobs... to little effect" },
            new Effect("Spider", "entity_spider", "summon_entity") { Price = 300, Description = "Spawns a Spider, a hostile enemy that climbs up walls attacks by jumping on the player" },
            new Effect("Squid", "entity_squid", "summon_entity") { Price = 100, Description = "Spawns a Squid, a passive animal that can be used as a source of ink sacs" },
            new Effect("Stray", "entity_stray", "summon_entity") { Price = 325, Description = "Spawns a Stray, a hostile enemy that attacks with a bow and inflicts slowness" },
            new Effect("Strider", "entity_strider", "summon_entity") { Price = 100, Description = "Spawns a Strider, a passive animal that can be ridden and used to travel over lava" },
            new Effect("Tadpole", "entity_tadpole", "summon_entity") { Price = 100, Description = "Spawns a Tadpole, a passive animal that grows into a frog" },
            new Effect("Trader Llama", "entity_trader_llama", "summon_entity") { Price = 100, Description = "Spawns a Trader Llama, a passive animal that accompanies Wandering Traders" },
            new Effect("Tropical Fish", "entity_tropical_fish", "summon_entity") { Price = 100, Description = "Spawns a Tropical Fish, a passive animal that can be used as a source of food" },
            new Effect("Turtle", "entity_turtle", "summon_entity") { Price = 100, Description = "Spawns a Turtle, a passive animal that can be used to get scutes" },
            new Effect("Vex", "entity_vex", "summon_entity") { Price = 300, Description = "Spawns a Vex, a small hostile enemy that lunges at players and phases through blocks" },
            new Effect("Villager", "entity_villager", "summon_entity") { Price = 150, Description = "Spawns a Villager, a passive creature that can be traded with" },
            new Effect("Vindicator", "entity_vindicator", "summon_entity") { Price = 500, Description = "Spawns a Vindicator, a hostile enemy that attacks with an axe" },
            new Effect("Wandering Trader", "entity_wandering_trader", "summon_entity") { Price = 150, Description = "Spawns a Wandering Trader, a passive creature that can be traded with" },
            new Effect("Warden", "entity_warden", "summon_entity") { Price = 3000, Description = "Spawns a Warden, a powerful hostile enemy that follows players by sensing vibrations in the ground" },
            new Effect("Witch", "entity_witch", "summon_entity") { Price = 300, Description = "Spawns a Witch, a hostile enemy that attacks with potions" },
            new Effect("Wither", "entity_wither", "summon_entity") { Price = 3000, Description = "Spawns a Wither, a powerful aerial boss that attacks by firing its heads as projectiles" },
            new Effect("Wither Skeleton", "entity_wither_skeleton", "summon_entity") { Price = 400, Description = "Spawns a Wither Skeleton, a hostile enemy that attacks with a sword and inflicts the withering effect" },
            new Effect("Wolf", "entity_wolf", "summon_entity") { Price = 200, Description = "Spawns a Wolf, a passive animal that can be tamed to fight by your side" },
            new Effect("Zoglin", "entity_zoglin", "summon_entity") { Price = 1000, Description = "Spawns a Zoglin, a hostile enemy that flings its targets into the air" },
            new Effect("Zombie", "entity_zombie", "summon_entity") { Price = 200, Description = "Spawns a Zombie, a hostile enemy that attacks with its fists" },
            new Effect("Zombie Horse", "entity_zombie_horse", "summon_entity") { Price = 100, Description = "Spawns a Zombie Horse, a passive animal that can be tamed and ridden" },
            new Effect("Zombie Villager", "entity_zombie_villager", "summon_entity") { Price = 200, Description = "Spawns a Zombie Villager, a hostile enemy that attacks with its fists. It can be cured using magical potions." },
            new Effect("Zombified Piglin", "entity_zombified_piglin", "summon_entity") { Price = 200, Description = "Spawns a Zombified Piglin, a neutral enemy that attacks with a sword when it or its allies are attacked" },
            // remove nearest entity
            new Effect("Remove Entity", "remove_entity", ItemKind.Folder),
            new Effect("Allay", "remove_entity_allay", "remove_entity") { Price = 150 },
            new Effect("Armor Stand", "remove_entity_armor_stand", "remove_entity") { Price = 200 },
            new Effect("Axolotl", "remove_entity_axolotl", "remove_entity") { Price = 150 },
            new Effect("Bat", "remove_entity_bat", "remove_entity") { Price = 1 },
            new Effect("Bee", "remove_entity_bee", "remove_entity") { Price = 100 },
            new Effect("Blaze", "remove_entity_blaze", "remove_entity") { Price = 150 },
            new Effect("Boat", "remove_entity_boat", "remove_entity") { Price = 150 },
            new Effect("Boat with Chest", "remove_entity_chest_boat", "remove_entity") { Price = 150 },
            new Effect("Camel", "remove_entity_camel", "remove_entity") { Price = 150 },
            new Effect("Cat", "remove_entity_cat", "remove_entity") { Price = 150 },
            new Effect("Cave Spider", "remove_entity_cave_spider", "remove_entity") { Price = 150 },
            new Effect("Chicken", "remove_entity_chicken", "remove_entity") { Price = 150 },
            new Effect("Cod", "remove_entity_cod", "remove_entity") { Price = 150 },
            new Effect("Cow", "remove_entity_cow", "remove_entity") { Price = 150 },
            new Effect("Creeper", "remove_entity_creeper", "remove_entity") { Price = 150 },
            new Effect("Dolphin", "remove_entity_dolphin", "remove_entity") { Price = 150 },
            new Effect("Donkey", "remove_entity_donkey", "remove_entity") { Price = 150 },
            new Effect("Drowned", "remove_entity_drowned", "remove_entity") { Price = 150 },
            new Effect("Elder Guardian", "remove_entity_elder_guardian", "remove_entity") { Price = 1000 },
            new Effect("Ender Dragon", "remove_entity_ender_dragon", "remove_entity") { Price = 2000 },
            new Effect("Enderman", "remove_entity_enderman", "remove_entity") { Price = 150 },
            new Effect("Endermite", "remove_entity_endermite", "remove_entity") { Price = 150 },
            new Effect("Evoker", "remove_entity_evoker", "remove_entity") { Price = 200 },
            new Effect("Fox", "remove_entity_fox", "remove_entity") { Price = 150 },
            new Effect("Frog", "remove_entity_frog", "remove_entity") { Price = 150 },
            new Effect("Ghast", "remove_entity_ghast", "remove_entity") { Price = 150 },
            new Effect("Giant", "remove_entity_giant", "remove_entity") { Price = 150 },
            new Effect("Glow Squid", "remove_entity_glow_squid", "remove_entity") { Price = 150 },
            new Effect("Goat", "remove_entity_goat", "remove_entity") { Price = 150 },
            new Effect("Guardian", "remove_entity_guardian", "remove_entity") { Price = 150 },
            new Effect("Hoglin", "remove_entity_hoglin", "remove_entity") { Price = 300 },
            new Effect("Horse", "remove_entity_horse", "remove_entity") { Price = 150 },
            new Effect("Husk", "remove_entity_husk", "remove_entity") { Price = 150 },
            new Effect("Illusioner", "remove_entity_illusioner", "remove_entity") { Price = 150 },
            new Effect("Iron Golem", "remove_entity_iron_golem", "remove_entity") { Price = 150 },
            new Effect("Llama", "remove_entity_llama", "remove_entity") { Price = 150 },
            new Effect("Magma Cube", "remove_entity_magma_cube", "remove_entity") { Price = 150 },
            new Effect("Minecart", "remove_entity_minecart", "remove_entity") { Price = 150 },
            new Effect("Minecart with Chest", "remove_entity_minecart_chest", "remove_entity") { Price = 100 },
            new Effect("Mooshroom", "remove_entity_mushroom_cow", "remove_entity") { Price = 150 },
            new Effect("Mule", "remove_entity_mule", "remove_entity") { Price = 150 },
            new Effect("Ocelot", "remove_entity_ocelot", "remove_entity") { Price = 150 },
            new Effect("Panda", "remove_entity_panda", "remove_entity") { Price = 150 },
            new Effect("Parrot", "remove_entity_parrot", "remove_entity") { Price = 150 },
            new Effect("Phantom", "remove_entity_phantom", "remove_entity") { Price = 150 },
            new Effect("Pig", "remove_entity_pig", "remove_entity") { Price = 150 },
            new Effect("Piglin", "remove_entity_piglin", "remove_entity") { Price = 150 },
            new Effect("Piglin Brute", "remove_entity_piglin_brute", "remove_entity") { Price = 400 },
            new Effect("Pillager", "remove_entity_pillager", "remove_entity") { Price = 150 },
            new Effect("Polar Bear", "remove_entity_polar_bear", "remove_entity") { Price = 150 },
            //new Effect("Primed TNT", "remove_entity_primed_tnt", "remove_entity") { Price = 200 },
            new Effect("Pufferfish", "remove_entity_pufferfish", "remove_entity") { Price = 150 },
            new Effect("Rabbit", "remove_entity_rabbit", "remove_entity") { Price = 150 },
            new Effect("Ravager", "remove_entity_ravager", "remove_entity") { Price = 400 },
            new Effect("Salmon", "remove_entity_salmon", "remove_entity") { Price = 150 },
            new Effect("Sheep", "remove_entity_sheep", "remove_entity") { Price = 150 },
            new Effect("Shulker", "remove_entity_shulker", "remove_entity") { Price = 150 },
            new Effect("Silverfish", "remove_entity_silverfish", "remove_entity") { Price = 150 },
            new Effect("Skeleton", "remove_entity_skeleton", "remove_entity") { Price = 150 },
            new Effect("Skeleton Horse", "remove_entity_skeleton_horse", "remove_entity") { Price = 150 },
            new Effect("Slime", "remove_entity_slime", "remove_entity") { Price = 150 },
            new Effect("Sniffer", "remove_entity_sniffer", "remove_entity") { Price = 150 },
            new Effect("Snow Golem", "remove_entity_snowman", "remove_entity") { Price = 100 },
            new Effect("Spider", "remove_entity_spider", "remove_entity") { Price = 150 },
            new Effect("Squid", "remove_entity_squid", "remove_entity") { Price = 150 },
            new Effect("Stray", "remove_entity_stray", "remove_entity") { Price = 150 },
            new Effect("Strider", "remove_entity_strider", "remove_entity") { Price = 300 },
            new Effect("Tadpole", "remove_entity_tadpole", "remove_entity") { Price = 150 },
            new Effect("Trader Llama", "remove_entity_trader_llama", "remove_entity") { Price = 100 },
            new Effect("Tropical Fish", "remove_entity_tropical_fish", "remove_entity") { Price = 150 },
            new Effect("Turtle", "remove_entity_turtle", "remove_entity") { Price = 150 },
            new Effect("Vex", "remove_entity_vex", "remove_entity") { Price = 150 },
            new Effect("Villager", "remove_entity_villager", "remove_entity") { Price = 200 },
            new Effect("Vindicator", "remove_entity_vindicator", "remove_entity") { Price = 200 },
            new Effect("Wandering Trader", "remove_entity_wandering_trader", "remove_entity") { Price = 150 },
            new Effect("Warden", "remove_entity_warden", "remove_entity") { Price = 3000 },
            new Effect("Witch", "remove_entity_witch", "remove_entity") { Price = 150 },
            new Effect("Wither", "remove_entity_wither", "remove_entity") { Price = 3000 },
            new Effect("Wither Skeleton", "remove_entity_wither_skeleton", "remove_entity") { Price = 150 },
            new Effect("Wolf", "remove_entity_wolf", "remove_entity") { Price = 200 },
            new Effect("Zoglin", "remove_entity_zoglin", "remove_entity") { Price = 400 },
            new Effect("Zombie", "remove_entity_zombie", "remove_entity") { Price = 150 },
            new Effect("Zombie Horse", "remove_entity_zombie_horse", "remove_entity") { Price = 150 },
            new Effect("Zombie Villager", "remove_entity_zombie_villager", "remove_entity") { Price = 150 },
            new Effect("Zombified Piglin", "remove_entity_zombified_piglin", "remove_entity") { Price = 150 },
            // sets the server difficulty (affects how much damage mobs deal)
            new Effect("Set Difficulty", "difficulty", ItemKind.Folder),
            new Effect("Peaceful Mode", "difficulty_peaceful", "difficulty") { Price = 200, Description = "Removes all hostile mobs and prevents new ones spawning" },
            new Effect("Easy Mode", "difficulty_easy", "difficulty") { Price = 100 },
            new Effect("Normal Mode", "difficulty_normal", "difficulty") { Price = 200 },
            new Effect("Hard Mode", "difficulty_hard", "difficulty") { Price = 400 },
            // applies potion effects to every player
            new Effect("Apply Potion", "apply_potion_effect", ItemKind.Folder),
            new Effect("Absorption", "potion_absorption", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Grants extra health that cannot be regenerated" },
            new Effect("Bad Omen", "potion_bad_omen", "apply_potion_effect") { Price = 400, Duration = 20, Description = "Causes a village raid when a player possessing this effect is inside of a village" },
            new Effect("Blindness", "potion_blindness", "apply_potion_effect") { Price = 100, Duration = 20, Description = "Temporarily reduces a player's range of vision and disables their sprinting" },
            new Effect("Conduit Power", "potion_conduit_power", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Grants water breathing, night vision, and haste when underwater" },
            new Effect("Darkness", "potion_darkness", "apply_potion_effect") { Price = 75, Duration = 20, Description = "Temporarily reduces a player's range of vision" },
            new Effect("Dolphin's Grace", "potion_dolphins_grace", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Increases swimming speed" },
            new Effect("Fire Resistance", "potion_fire_resistance", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Grants invincibility from fire and lava damage" },
            new Effect("Glowing", "potion_glowing", "apply_potion_effect") { Price = 25, Duration = 20, Description = "Gives the player a glowing white outline that can be seen through walls" },
            new Effect("Haste", "potion_haste", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Increases mining speed" },
            new Effect("Health Boost", "potion_health_boost", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Increases maximum health" },
            new Effect("Invisibility", "potion_invisibility", "apply_potion_effect") { Price = 25, Duration = 20, Description = "Makes the player invisible" },
            // disabled in favor of "Low Gravity" -- new Effect("Jump Boost", "potion_jump_boost", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Makes the player jump higher" },
            new Effect("Levitation", "potion_levitation", "apply_potion_effect") { Price = 100, Duration = 20, Description = "Gradually lifts the player up into the air" },
            new Effect("Mining Fatigue", "potion_mining_fatigue", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Decreases mining speed" },
            new Effect("Nausea", "potion_nausea", "apply_potion_effect") { Price = 100, Duration = 20, Description = "Makes the player's screen shake" },
            new Effect("Night Vision", "potion_night_vision", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Allows the player to see inside dark areas" },
            new Effect("Poison", "potion_poison", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Gradually damages the player" },
            new Effect("Regeneration", "potion_regeneration", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Gradually heals the player" },
            new Effect("Resistance", "potion_resistance", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Reduces damage taken" },
            // disabled in favor of "Low Gravity" -- new Effect("Slow Falling", "potion_slow_falling", "apply_potion_effect") { Price = 25, Duration = 20, Description = "Reduces the player's falling speed" },
            new Effect("Slowness", "potion_slowness", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Decreases the player's walking speed" },
            new Effect("Speed", "potion_speed", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Increases the player's walking speed" },
            new Effect("Strength", "potion_strength", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Increases the player's damage output" },
            new Effect("Water Breathing", "potion_water_breathing", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Grants the ability to breathe underwater" },
            new Effect("Weakness", "potion_weakness", "apply_potion_effect") { Price = 50, Duration = 20, Description = "Decreases the player's damage output" },
            // places a block at everyone's feet
            new Effect("Place Block", "place_block", ItemKind.Folder),
            new Effect("Bedrock", "block_bedrock", "place_block") { Price = 200, Description = "Places the unbreakable bedrock block at the streamer's feet" },
            new Effect("Cobweb", "block_cobweb", "place_block") { Price = 25, Description = "Places a cobweb block on the streamer" },
            new Effect("Fire", "block_fire", "place_block") { Price = 200, Description = "Places a fire block on the streamer" },
            // unlikely to do anything -- new Effect("Redstone Torch", "block_redstone_torch", "place_block") { Price = 25, Description = "Places a redstone torch on the streamer" },
            new Effect("Sculk Catalyst", "block_sculk_catalyst", "place_block") { Price = 25, Description = "Places a sculk catalyst on the streamer which absorbs the XP from mobs that die nearby" },
            // very unlikely to "work", better off using "Summon TNT" -- new Effect("TNT", "block_tnt", "place_block") { Price = 200, Description = "Places a TNT block on the streamer" },
            new Effect("Wither Rose", "block_wither_rose", "place_block") { Price = 100, Description = "Places a wither rose on the streamer which poisons them" },
            new Effect("Lightning Rod", "block_lightning_rod", "place_block") { Price = 25, Description = "Places a lightning rod on the streamer" },
            new Effect("Water", "block_water", "place_block") { Price = 50, Description = "Places a flowing water block on the streamer" },
            // places a block several blocks above everyone's head
            new Effect("Place Falling Block", "place_falling_block", ItemKind.Folder),
            new Effect("Anvil", "falling_block_anvil", "place_falling_block") { Price = 100, Description = "Drops an anvil block on the streamer" },
            new Effect("Gravel", "falling_block_gravel", "place_falling_block") { Price = 25, Description = "Drops a gravel block on the streamer" },
            new Effect("Red Sand", "falling_block_red_sand", "place_falling_block") { Price = 25, Description = "Drops a red sand block on the streamer" },
            new Effect("Sand", "falling_block_sand", "place_falling_block") { Price = 25, Description = "Drops a sand block on the streamer" },
            // sets the server weather
            new Effect("Set Weather", "weather", ItemKind.Folder),
            new Effect("Clear Weather", "clear", "weather") { Price = 25, Description = "Makes the weather sunny to allow rays of fire to shine down on hostile mobs" },
            new Effect("Rainy Weather", "downfall", "weather") { Price = 50, Description = "Makes the weather rainy which prevents hostile mobs from burning in the daylight" },
            new Effect("Stormy Weather", "thunder_storm", "weather") { Price = 75, Description = "Starts a thunderstorm with sporadic lightning strikes. Combine with placing a lightning rod to perform some electrocution!" },
            // apply enchants
            new Effect("Enchantments", "enchantments", ItemKind.Folder),
            new Effect("Remove Enchantments", "remove_enchants", "enchantments") { Price = 200, Description = "Removes all enchants from the held item" },
            new Effect("Apply Aqua Affinity", "enchant_aqua_affinity", "enchantments") { Price = 50, Description = "Increases underwater mining speed" },
            new Effect("Apply Bane of Arthropods V", "enchant_bane_of_arthropods", "enchantments") { Price = 50, Description = "Increases damage dealt to arthropod mobs (spiders, cave spiders, bees, silverfish, and endermites)" },
            new Effect("Apply Blast Protection IV", "enchant_blast_protection", "enchantments") { Price = 50, Description = "Reduces damage taken from explosions" },
            new Effect("Apply Channeling", "enchant_channeling", "enchantments") { Price = 50, Description = "Makes tridents produce lightning when thrown while raining" },
            new Effect("Apply Curse of Binding", "enchant_curse_of_binding", "enchantments") { Price = 100, Description = "Armor pieces with this enchantment cannot be taken off until death" },
            new Effect("Apply Curse of Vanishing", "enchant_curse_of_vanishing", "enchantments") { Price = 150, Description = "Items with this enchantment will disappear upon death" },
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

            // TODO: add goat horns to give/remove items? or maybe just add it as a sound effect to Annoying Popup? idk

            // gives 1 item
            new Effect("Give an Item", "give_item", ItemKind.Folder),
            new Effect("Elytra", "give_elytra", "give_item") { Price = 500, Description = "Gives an Elytra, a pair of wings that allows the player to fly" },
            new Effect("Eye of Ender", "give_ender_eye", new[]{"items64"}, "give_item") { Price = 100, Description = "Gives an Eye of Ender, a critical part of finding the End Portal and completing the game" },
            new Effect("End Portal Frame", "give_end_portal_frame", new[]{"items64"}, "give_item") { Price = 200, Description = "Gives you a part of the frame required to build an end portal. Note that some server configurations may limit how many players get one." },
            new Effect("Recovery Compass", "give_recovery_compass", new[]{"items64"}, "give_item") { Price = 400, Description = "Gives you a compass that points to the location of your most recent death" },
            new Effect("Trident", "give_trident", "give_item") { Price = 300, Description = "Gives you a Trident, a powerful melee weapon that can be thrown" },

            new Effect("Food", "give_food", ItemKind.Folder, "give_item"),
            new Effect("Cooked Porkchop", "give_cooked_porkchop", new[]{"items64"}, "give_food") { Price = 40, Description = "Gives you a Cooked Porkchop, a food item that restores 4 bars of hunger" },
            new Effect("Golden Apple", "give_golden_apple", new[]{"items64"}, "give_food") { Price = 200, Description = "Gives you a Golden Apple, a food item that restores 2 bars of hunger, 2 bars of health, and temporarily grants an extra 2 hearts of maximum health" },
            new Effect("Enchanted Golden Apple", "give_enchanted_golden_apple", new[]{"items64"}, "give_food") { Price = 300, Description = "Gives you an Enchanted Golden Apple, a food item that restores 2 bars of hunger, 8 bars of health, and temporarily grants fire resistance, an extra 8 hearts of maximum health, resistance, and fire resistance" },

            new Effect("Minerals", "give_minerals", ItemKind.Folder, "give_item"),
            new Effect("Coal", "give_coal", new[]{"items64"}, "give_minerals") { Price = 10, Description = "Gives you a piece of Coal, a mineral that can be used to smelt ores and cook food" },
            new Effect("Gold Ingot", "give_gold_ingot", new[]{"items64"}, "give_minerals") { Price = 20, Description = "Gives you a Gold Ingot, a mineral that can be used to craft tools, armor, and golden apples" },
            new Effect("Iron Ingot", "give_iron_ingot", new[]{"items64"}, "give_minerals") { Price = 25, Description = "Gives you an Iron Ingot, a mineral that can be used to craft tools, armor, and shields" },
            new Effect("Diamond", "give_diamond", new[]{"items64"}, "give_minerals") { Price = 100, Description = "Gives you a Diamond, a mineral that can be used to craft tools and armor" },
            new Effect("Netherite Ingot", "give_netherite_ingot", new[]{"items64"}, "give_minerals") { Price = 200, Description = "Gives you a Netherite Ingot, a mineral that can be used to upgrade diamond tools and armor" },

            new Effect("Tools", "give_tools", ItemKind.Folder, "give_item"),
            new Effect("Wooden Pickaxe", "give_wooden_pickaxe", "give_tools") { Price = 25 },
            new Effect("Stone Pickaxe", "give_stone_pickaxe", "give_tools") { Price = 50 },
            new Effect("Golden Pickaxe", "give_golden_pickaxe", "give_tools") { Price = 50 },
            new Effect("Iron Pickaxe", "give_iron_pickaxe", "give_tools") { Price = 100 },
            new Effect("Diamond Pickaxe", "give_diamond_pickaxe", "give_tools") { Price = 250 },
            new Effect("Netherite Pickaxe", "give_netherite_pickaxe", "give_tools") { Price = 350 },

            new Effect("Weapons", "give_weapons", ItemKind.Folder, "give_item"),
            new Effect("Wooden Sword", "give_wooden_sword", "give_weapons") { Price = 25 },
            new Effect("Stone Sword", "give_stone_sword", "give_weapons") { Price = 50 },
            new Effect("Golden Sword", "give_golden_sword", "give_weapons") { Price = 50 },
            new Effect("Iron Sword", "give_iron_sword", "give_weapons") { Price = 100 },
            new Effect("Diamond Sword", "give_diamond_sword", "give_weapons") { Price = 250 },
            new Effect("Netherite Sword", "give_netherite_sword", "give_weapons") { Price = 350 },

            // takes 1 item
            new Effect("Take an Item", "take_item", ItemKind.Folder),
            new Effect("Elytra", "take_elytra", "take_item") { Price = 700 },
            new Effect("Eye of Ender", "take_ender_eye", new[]{"items64"}, "take_item") { Price = 200 },
            new Effect("End Portal Frame", "take_end_portal_frame", new[]{"items64"}, "take_item") { Price = 300 },
            new Effect("Recovery Compass", "take_recovery_compass", new[]{"items64"}, "take_item") { Price = 500 },
            new Effect("Trident", "take_trident", "take_item") { Price = 400 },

            new Effect("Food", "take_food", ItemKind.Folder, "take_item"),
            new Effect("Cooked Porkchop", "take_cooked_porkchop", new[]{"items64"}, "take_food") { Price = 100 },
            new Effect("Golden Apple", "take_golden_apple", new[]{"items64"}, "take_food") { Price = 400 },
            new Effect("Enchanted Golden Apple", "take_enchanted_golden_apple", new[]{"items64"}, "take_food") { Price = 500 },

            new Effect("Minerals", "take_minerals", ItemKind.Folder, "take_item"),
            new Effect("Coal", "take_coal", new[]{"items64"}, "take_minerals") { Price = 50 },
            new Effect("Iron Ingot", "take_iron_ingot", new[]{"items64"}, "take_minerals") { Price = 100 },
            new Effect("Gold Ingot", "take_gold_ingot", new[]{"items64"}, "take_minerals") { Price = 100 },
            new Effect("Diamond", "take_diamond", new[]{"items64"}, "take_minerals") { Price = 300 },
            new Effect("Netherite Ingot", "take_netherite_ingot", new[]{"items64"}, "take_minerals") { Price = 400 },

            new Effect("Tools", "take_tools", ItemKind.Folder, "take_item"),
            new Effect("Wooden Pickaxe", "take_wooden_pickaxe", "take_tools") { Price = 50 },
            new Effect("Stone Pickaxe", "take_stone_pickaxe", "take_tools") { Price = 100 },
            new Effect("Golden Pickaxe", "take_golden_pickaxe", "take_tools") { Price = 100 },
            new Effect("Iron Pickaxe", "take_iron_pickaxe", "take_tools") { Price = 400 },
            new Effect("Diamond Pickaxe", "take_diamond_pickaxe", "take_tools") { Price = 500 },
            new Effect("Netherite Pickaxe", "take_netherite_pickaxe", "take_tools") { Price = 500 },

            new Effect("Weapons", "take_weapons", ItemKind.Folder, "take_item"),
            new Effect("Wooden Sword", "take_wooden_sword", "take_weapons") { Price = 50 },
            new Effect("Stone Sword", "take_stone_sword", "take_weapons") { Price = 100 },
            new Effect("Golden Sword", "take_golden_sword", "take_weapons") { Price = 100 },
            new Effect("Iron Sword", "take_iron_sword", "take_weapons") { Price = 400 },
            new Effect("Diamond Sword", "take_diamond_sword", "take_weapons") { Price = 500 },
            new Effect("Netherite Sword", "take_netherite_sword", "take_weapons") { Price = 500 },

            // shaders
            new Effect("Screen Effects", "shaders", ItemKind.Folder),
            new Effect("Bumpy", "shader_bumpy", "shaders") { Price = 50, Duration = 30, Description = "Adds a faint white outline to everything giving the impression of bumpy textures" },
            new Effect("Creeper TV", "shader_green", "shaders") { Price = 250, Duration = 30, Description = "See the game through the eyes of a creeper... through a CRT" },
            new Effect("CRT", "shader_ntsc", "shaders") { Price = 100, Duration = 30, Description = "Makes the game look like it's running on an old CRT TV" },
            new Effect("Desaturate", "shader_desaturate", "shaders") { Price = 50, Duration = 30, Description = "Sucks the color out of the game" },
            new Effect("Flip", "shader_flip", "shaders") { Price = 500, Duration = 30, Description = "Flips the screen upside-down" },
            new Effect("Invert Colors", "shader_invert", "shaders") { Price = 100, Duration = 30, Description = "Inverts the game's colors to see the game through the eyes of an enderman" },
            new Effect("Oil Painting", "shader_blobs2", "shaders") { Price = 100, Duration = 30, Description = "Makes the game look like a smeary oil painting" },
            new Effect("Pencil Sketch", "shader_pencil", "shaders") { Price = 100, Duration = 30, Description = "Makes the game look like it was sketched with a pencil" },
            new Effect("Prototype", "shader_sobel", "shaders") { Price = 100, Duration = 30, Description = "Makes the game only render edges of textures" },
            new Effect("Psychedelic", "shader_wobble", "shaders") { Price = 200, Duration = 30, Description = "Makes the game rainbowy and wobbly" },
            new Effect("Retro", "shader_bits", "shaders") { Price = 200, Duration = 30, Description = "Makes the game look like it's running on an NES" },
            new Effect("Spider", "shader_spider", "shaders") { Price = 100, Duration = 30, Description = "See the game through the eight eyes of a spider" },
            new Effect("Trail", "shader_phosphor", "shaders") { Price = 200, Duration = 30, Description = "Duplicates every frame to create a ghostly trail effect" },
            //new Effect("Retro", "shader_notch", "shaders"), -- not as retro looking as "bits"
            //new Effect("FXAA", "shader_fxaa", "shaders"), -- doesn't do much
            //new Effect("Oil Painting", "shader_art", "shaders"), -- very very similar to blobs2 but with a slight white glow on everything
            //new Effect("Color Convolve", "shader_color_convolve", "shaders"), -- vanilla but slightly more saturated
            //new Effect("Deconverge", "shader_deconverge", "shaders"), -- kinda minor color channel offsets
            //new Effect("Outline", "shader_outline", "shaders"), -- broken
            //new Effect("Scan Pincushion", "shader_scan_pincushion", "shaders"), -- looks like NTSC but without the blur
            //new Effect("Blur", "shader_blur", "shaders"), -- broken
            //new Effect("Blobs", "shader_blobs", "shaders"), -- less extreme version of blobs2
            //new Effect("Antialias", "shader_antialias", "shaders"), -- just makes the game look a bit smoother
            //new Effect("Creeper", "shader_creeper", "shaders"), -- like green but without the CRT effect
        };

        // Slider Ranges
        public override List<ItemType> ItemTypes => new()
        {
            new ItemType("Items", "items64", ItemType.Subtype.Slider, "{\"min\":1,\"max\":64}"),
            new ItemType("Half-Hearts", "halfhealth10", ItemType.Subtype.Slider, "{\"min\":1,\"max\":10}"),
            new ItemType("Hearts", "health10", ItemType.Subtype.Slider, "{\"min\":1,\"max\":10}"),
            new ItemType("Food Points", "food10", ItemType.Subtype.Slider, "{\"min\":1,\"max\":10}"),
            new ItemType("XP Levels", "xp100", ItemType.Subtype.Slider, "{\"min\":1,\"max\":100}")
        };

        private static readonly Regex UnavailableEffectPattern = new(@"^.+ \[effect: ([a-zA-Z0-9_]+)\]$", RegexOptions.Compiled);

        public override List<Effect> Effects => AllEffects;

        public override SimpleTCPClientConnector Connector
        {
            get => base.Connector;
            set
            {
                value.MessageParsed += InterceptMessageParsed;
                base.Connector = value;
            }
        }

        private void InterceptMessageParsed(ISimpleTCPConnector<Request, Response, ISimpleTCPContext> sender, Response response, ISimpleTCPContext context)
        {
            Log.Debug($"Parsing incoming message #{response.id} of type {response.type} with message \"{response.message}\"");
            if (response.message == null)
            {
                Log.Debug("Message has no message attribute; exiting");
                return;
            }

            if (response.id == 0 && response.message.StartsWith("_mc_cc_server_status_"))
            {
                Log.Debug("Message is server status packet");
                // incoming packet contains info about supported effects
                OnServerStatusPacket(response);
            }
            else if (response.status == ConnectorLib.JSON.EffectStatus.Unavailable)
            {
                Log.Debug("Effect is unavailable");
                // a requested effect was unavailable; it should be hidden from the menu
                OnUnavailablePacket(response);
            }
        }

        private void OnServerStatusPacket(Response response)
        {
            // TODO: not working in CCPAK
            // load packet data
            var payload = response.message.Replace("_mc_cc_server_status_", "");
            var registeredEffects = JsonConvert.DeserializeObject<string[]>(payload);
            if (registeredEffects == null)
            {
                Log.Error("Message payload could not be parsed");
                return;
            }

            // hide effects that are unsupported by the platform
            Log.Message("Hiding unsupported effects");
            var registeredEffectsList = new List<string>(registeredEffects).ConvertAll(input => input.ToLower());
            AllEffects.FindAll(effect => effect.Kind == ItemKind.Effect && !registeredEffectsList.Contains(effect.Code.ToLower()))
                .ForEach(HideEffect);
            Log.Message("Finished hiding effects");
        }

        private void OnUnavailablePacket(Response response)
        {
            var match = UnavailableEffectPattern.Match(response.message);
            if (!match.Success)
            {
                Log.Error($"Unavailable effect pattern match failed on \"{response.message}\"");
                return;
            }
            var effectCode = match.Groups[1].Value;
            var effect = AllEffects.Find(e => e.Code == effectCode);
            if (effect == null)
            {
                Log.Error($"Could not find unavailable effect \"{effect}\" in known effect list");
                return;
            }
            HideEffect(effect);
        }

        private void HideEffect(Effect effect)
        {
            Log.Message($"Hiding effect {effect.Code}");
            ReportStatus(effect, EffectStatus.MenuHidden);
        }
    }
}
