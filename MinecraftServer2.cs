// ReSharper disable RedundantUsingDirective
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

        public MinecraftServer(UserRecord player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler) : base(player, responseHandler, statusUpdateHandler) { }

        public override Game Game => new("Minecraft", "MinecraftServer", "PC", ConnectorType.SimpleTCPClientConnector);

        private static readonly EffectList AllEffects = new Effect[]
        {
            // miscellaneous
            new("Annoying Pop-Up", "toast") { Price = 50, Category = "Player", Description = "Plays an obnoxious animation and an obnoxious sound" },
            new("Dig Hole", "dig") { Price = 200, Category = "World", Description = "Digs a small hole underneath players" },
            new("Do-or-Die", "do_or_die") { Price = 500, Category = new("Health & Hunger", "Player"), Description = "Gives players a task to complete within 30 seconds or else they die" },
            new("Eat Chorus Fruit", "chorus_fruit") { Price = 75, Category = "World", Description = "Teleports the player to a random nearby block as if they ate a Chorus Fruit" },
            // TODO: disabled because this is killing anyone in the air -- new("Explode", "explode") { Price = 750, Category = "World", Description = "Spawns a harmless TNT-like explosion at players' feet" },
            new("Fling Randomly", "fling") { Price = 100, Category = "Movement", Description = "Flings players in a totally random direction" },
            new("Flip Mobs Upside-Down", "dinnerbone") { Price = 50, Category = "World", Description = "Flips nearby mobs upside-down by naming them after the iconic Minecraft developer Dinnerbone" },
            new("Invert Camera", "invert_look") { Price = 200, Duration = 15, Category = "Movement", Description = "Temporarily inverts mouse movement" },
            new("Invert Controls", "invert_wasd") { Price = 200, Duration = 15, Category = "Movement", Description = "Temporarily inverts WASD movement" },
            new("Open Lootbox", "lootbox") { Price = 100, Category = new("Inventory", "Give Items"), Description = "Gifts a completely random item with varying enchants and modifiers" },
            new("Open Lucky Lootbox", "lootbox_5") { Price = 500, Category = new("Inventory", "Give Items"), Description = "Gifts two random items with vastly higher odds of having beneficial enchantments and modifiers" },
            new("Place Flowers", "flowers") { Price = 25, Category = "World", Description = "Randomly places flowers nearby, possibly including the toxic Wither Rose" },
            new("Place Torches", "lit") { Price = 100, Category = "World", Description = "Places torches on every nearby block" },
            new("Plant Tree", "plant_tree") { Price = 100, Category = "World", Description = "Plant a tree on top of every player" },
            new("Remove Torches", "dim") { Price = 200, Category = "World", Description = "Removes all nearby torches" },
            new("Replace Area with Gravel", "gravel_hell") { Price = 200, Category = "World", Description = "Replaces all nearby blocks with gravel" },
            new("Respawn Player", "respawn") { Price = 500, Category = "Player", Description = "Sends players to their spawn point" },
            new("Spawn Ore Veins", "vein") { Price = 100, Category = "World", Description = "Places random ore veins (or lava) near every player" },
            new("Spooky Sound Effect", "sfx") { Price = 100, Category = "World", Description = "Plays a random spoooky sound effect" },
            new("Swap Locations", "swap") { Price = 1000, Category = new("World", "Player"), Description = "Randomly swaps the locations of all players in the session" },
            new("Teleport to a Nearby Structure", "structure") { Price = 750, Category = "World", Description = "Teleports players to a random nearby structure (i.e. village, desert temple, nether fortress, etc.)" },
            new("Teleport to a Random Biome", "biome") { Price = 750, Category = "World", Description = "Teleports players to a random nearby biome (i.e. ocean, plains, desert, etc.)" },
            new("Teleport All Entities To Players", "entity_chaos") { Price = 3000, Category = "World", Description = "Teleports every loaded mob on the server to the targeted players in an even split. Note that this may only teleport nearby mobs on certain server configurations." },
            new("Water Bucket Clutch", "bucket_clutch") { Price = 400, Category = new("World", "Player"), Description = "Teleports players 30 blocks up and gives them a water bucket, forcing them to clutch if they want to live" },
            // time commands
            new("Set Time to Day", "time_day") { Price = 50, SortName = "Time: Day", Category = "Server", Description = "Jumps the clock ahead to daytime" },
            new("Set Time to Night", "time_night") { Price = 50, SortName = "Time: Night", Category = "Server", Description = "Jumps the clock ahead to nighttime" },
            // sets the server difficulty (affects how much damage mobs deal)
            new("Peaceful Mode", "difficulty_peaceful") { Price = 200, SortName = "Difficulty: 0", Category = "Server", Description = "Sets the server difficulty to peaceful, removing all hostile mobs and preventing new ones from spawning" },
            new("Easy Mode", "difficulty_easy") { Price = 100, SortName = "Difficulty: 1", Category = "Server", Description = "Sets the server difficulty to easy, reducing the damage dealt by mobs by 50%" },
            new("Normal Mode", "difficulty_normal") { Price = 200, SortName = "Difficulty: 2", Category = "Server", Description = "Sets the server difficulty to normal, the default difficulty" },
            new("Hard Hard", "difficulty_hard") { Price = 400, SortName = "Difficulty: 3", Category = "Server", Description = "Sets the server difficulty to hard, increasing the damage dealt by mobs by 50% and buffing several mobs" },
            // sets the server weather
            new("Clear Weather", "clear") { Price = 25, SortName = "Weather: Clear", Category = "Server", Description = "Makes the weather sunny to allow rays of fire to shine down on hostile mobs" },
            new("Rainy Weather", "downfall") { Price = 50, SortName = "Weather: Rainy", Category = "Server", Description = "Makes the weather rainy which prevents hostile mobs from burning in the daylight" },
            new("Stormy Weather", "thunder_storm") { Price = 75, SortName = "Weather: Stormy", Category = "Server", Description = "Starts a thunderstorm with sporadic lightning strikes. Combine with placing a lightning rod to perform some electrocution!" },
            // health commands
            new("Take Max Health", "max_health_sub") { Price = 100, Quantity = 10, SortName = "MaxHealth-", Category = "Health & Hunger", Description = "Subtracts half hearts from every player's max health" },
            new("Give Max Health", "max_health_add") { Price = 50, Quantity = 10, SortName = "MaxHealth+", Category = "Health & Hunger", Description = "Adds half hearts to every player's max health" },
            new("Damage Player", "damage") { Price = 25, Quantity = 10, SortName = "Health-", Category = "Health & Hunger", Description = "Damages players for the set number of hearts, unless it would kill them" },
            new("Halve Health", "half_health") { Price = 125, Category = "Health & Hunger", Description = "Sets every player's health to 50% of what they currently have" },
            new("One-Hit KO", "ohko") { Price = 250, Duration = 15, Category = "Health & Hunger", Description = "Temporarily makes any damage source kill players in one hit" },
            new("Kill Player", "kill") { Price = 1000, SortName = "Health--", Category = "Health & Hunger", Description = "Immediately kills every player on the spot" },
            new("Heal Player", "heal") { Price = 10, Quantity = 10, SortName = "Health+", Category = "Health & Hunger", Description = "Heals players for the set number of hearts" },
            new("Heal Player to Full", "full_heal") { Price = 50, SortName = "Health++", Category = "Health & Hunger", Description = "Resets every player's health to full" },
            new("Invincible", "invincible") { Price = 50, Duration = 15, Category = "Health & Hunger", Description = "Temporarily makes players immune to damage" },
            // food commands
            new("Feed Player", "feed") { Price = 10, Quantity = 10, SortName = "Food+", Category = "Health & Hunger", Description = "Replenishes the players' food by the set amount of bars" },
            new("Feed Player to Full", "full_feed") { Price = 100, SortName = "Food++", Category = "Health & Hunger", Description = "Replenishes the full food bar" },
            new("Remove Food", "starve") { Price = 50, Quantity = 10, SortName = "Food-", Category = "Health & Hunger", Description = "Drains the players' food by the set amount of bars" },
            new("Starve Players", "full_starve") { Price = 400, SortName = "Food--", Category = "Health & Hunger", Description = "Drains the players' food" },
            // experience
            new("Give XP", "xp_add") { Price = 25, Quantity = 100, SortName = "XP+", Category = "Player", Description = "Adds experience levels" },
            new("Take XP", "xp_sub") { Price = 100, Quantity = 100, SortName = "XP-", Category = "Player", Description = "Removes experience levels" },
            new("Reset Experience", "reset_exp_progress") { Price = 500, SortName = "XP--", Category = "Player", Description = "Clears every players' XP" },
            // freeze-type commands
            new("Can't Move", "freeze") { Price = 100, Duration = 15, Category = "Movement", Description = "Temporarily prohibits player movement" },
            new("Disable Jumping", "disable_jumping") { Price = 100, Duration = 15, Category = "Movement", Description = "Temporarily prevents players from jumping" },
            new("Lock Camera", "camera_lock") { Price = 100, Duration = 15, Category = "Movement", Description = "Temporarily freezes every player's cameras" },
            new("Lock Camera To Ground", "camera_lock_to_ground") { Price = 200, Duration = 15, Category = "Movement", Description = "Temporarily locks every player's cameras to the ground" },
            new("Lock Camera To Sky", "camera_lock_to_sky") { Price = 200, Duration = 15, Category = "Movement", Description = "Temporarily locks every player's camera to the sky" },
            // inventory commands
            new("Clear Inventory", "clear_inventory") { Price = 750, Category = "Inventory", Description = "Wipes every item from players' inventories. Unavailable when 'Enable Keep Inventory' is active." },
            new("Clutter Inventory", "clutter") { Price = 50, Category = "Inventory", Description = "Shuffles around items in every player's inventory" },
            new("Delete Held Item", "delete_item") { Price = 200, Category = "Inventory", Description = "Deletes whatever item every player is currently holding" },
            new("Delete Random Item", "delete_random_item") { Price = 150, Category = "Inventory", Description = "Deletes a random item from every player's inventory" },
            new("Drop Held Item", "drop_item") { Price = 25, Category = "Inventory", Description = "Makes players drop their held item" },
            new("Damage Item", "damage_item") { Price = 100, SortName = "ItemDamage: 0", Category = "Inventory", Description = "Takes 25% of the durability of a held or worn item" },
            new("Repair Item", "repair_item") { Price = 50, SortName = "ItemDamage: 1", Category = "Inventory", Description = "Fully repairs a damaged item" },
            new("Disable Keep Inventory", "keep_inventory_off") { Price = 300, SortName = "KeepInventory: 0", Category = "Inventory", Description = "Disallows players from keeping their inventory upon death" }, // TODO: bid war?
            new("Enable Keep Inventory", "keep_inventory_on") { Price = 100, SortName = "KeepInventory: 1", Category = "Inventory", Description = "Allows players to keep their inventory upon death" },
            new("Put Held Item on Head", "hat") { Price = 50, Category = "Inventory", Description = "Swaps the item in every player's hand slot and head slot" },
            // set gamemode for x seconds
            new("Adventure Mode", "adventure_mode") { Price = 200, Duration = 20, SortName = "GameMode: Adventure", Category = "Player", Description = "Temporarily sets players to Adventure mode, rendering them unable to place or break blocks" },
            new("Creative Mode", "creative_mode") { Price = 200, Duration = 20, SortName = "GameMode: Creative", Category = "Player", Description = "Temporarily sets players to Creative mode, allowing them to fly and spawn in items" },
            new("Spectator Mode", "spectator_mode") { Price = 200, Duration = 10, SortName = "GameMode: Spectator", Category = "Player", Description = "Temporarily sets players to Spectator mode, turning them into a ghost that can fly through blocks" },
            new("Allow Flight", "flight") { Price = 150, Duration = 20, Category = "Player", Description = "Temporarily allows players to fly" },
            // summons a mob around each player
            new("Allay", "entity_allay") { Price = 100, Category = "Summon Entity", Description = "Spawns an Allay, a friendly creature who helps you find items" },
            new("Armor Stand", "entity_armor_stand") { Price = 150, Category = "Summon Entity", Description = "Spawns an Armor Stand, a decorative entity that has a chance of spawning with valuable armor" },
            new("Axolotl", "entity_axolotl") { Price = 100, Category = "Summon Entity", Description = "Spawns an Axolotl, a cute and friendly amphibian" },
            new("Bat", "entity_bat") { Price = 10, Category = "Summon Entity", Description = "Spawns a Bat, a passive animal that does little more than fly around and squeak" },
            new("Bee", "entity_bee") { Price = 100, Category = "Summon Entity", Description = "Spawns a Bee, a neutral animal that passively pollinates crops and angers when attacked" },
            new("Blaze", "entity_blaze") { Price = 300, Category = "Summon Entity", Description = "Spawns a Blaze, an enemy that shoots fireballs at players" },
            new("Boat", "entity_boat") { Price = 50, Category = "Summon Entity", Description = "Spawns a Boat, a vehicle that can be used to travel across water" },
            new("Boat with Chest", "entity_chest_boat") { Price = 150, Category = "Summon Entity", Description = "Spawns a Boat with Chest, a vehicle that can be used to travel across water and store items. Comes filled with items from a random loot table." },
            new("Camel", "entity_camel") { Price = 100, Category = "Summon Entity", Description = "Spawns a Camel, a passive animal that can be tamed and ridden" },
            new("Cat", "entity_cat") { Price = 100, Category = "Summon Entity", Description = "Spawns a Cat, a passive animal that can be tamed to follow you around" },
            new("Cave Spider", "entity_cave_spider") { Price = 300, Category = "Summon Entity", Description = "Spawns a Cave Spider, an enemy that inflicts poison when it attacks" },
            new("Charged Creeper", "entity_charged_creeper") { Price = 750, Category = "Summon Entity", Description = "Spawns a Charged Creeper, an enemy that creates an extra powerful explosion to attack" },
            new("Chicken", "entity_chicken") { Price = 100, Category = "Summon Entity", Description = "Spawns a Chicken, a passive animal that can be used as a source of food" },
            new("Cod", "entity_cod") { Price = 100, Category = "Summon Entity", Description = "Spawns a Cod, a passive fish that can be used as a source of food" },
            new("Cow", "entity_cow") { Price = 100, Category = "Summon Entity", Description = "Spawns a Cow, a passive animal that can be used as a source of food or milk" },
            new("Creeper", "entity_creeper") { Price = 300, Category = "Summon Entity", Description = "Spawns a Creeper, an enemy that explodes as an attack" },
            new("Dolphin", "entity_dolphin") { Price = 100, Category = "Summon Entity", Description ="Spawns a Dolphin, a passive animal that can give you a speed boost when swimming alongside it" },
            new("Donkey", "entity_donkey") { Price = 100, Category = "Summon Entity", Description ="Spawns a Donkey, a passive animal that can be tamed and ridden" },
            new("Drowned", "entity_drowned") { Price = 200, Category = "Summon Entity", Description ="Spawns a Drowned, an enemy that can breathe underwater and sometimes attack with a trident" },
            new("Elder Guardian", "entity_elder_guardian") { Price = 1000, Category = "Summon Entity", Description ="Spawns an Elder Guardian, an enemy with high thorns damage that attacks with a laser beam" },
            new("Ender Dragon", "entity_ender_dragon") { Price = 2000, Category = "Summon Entity", Description ="Spawns an Ender Dragon, a boss that flies around the world, destroying blocks and attacking players" },
            new("Enderman", "entity_enderman") { Price = 300, Category = "Summon Entity", Description ="Spawns an Enderman, a neutral enemy that teleports around and angers when attacked or looked at" },
            new("Endermite", "entity_endermite") { Price = 250, Category = "Summon Entity", Description ="Spawns an Endermite, a tiny enemy that attacks players and Endermen" },
            new("Evoker", "entity_evoker") { Price = 600, Category = "Summon Entity", Description ="Spawns an Evoker, an enemy that attacks by summoning Vexes and armor-piercing fangs. Drops a Totem of Undying when killed." },
            new("Fox", "entity_fox") { Price = 100, Category = "Summon Entity", Description ="Spawns a Fox, a passive animal that can be tamed to fight by your side" },
            new("Frog", "entity_frog") { Price = 100, Category = "Summon Entity", Description ="Spawns a Frog, a passive animal that can eat Slimes" },
            new("Ghast", "entity_ghast") { Price = 500, Category = "Summon Entity", Description ="Spawns a Ghast, an enemy that shoots explosive fireballs at players" },
            new("Giant", "entity_giant") { Price = 100, Category = "Summon Entity", Description ="Spawns a Giant, an unused enemy that does nothing other than being tall and obnoxious" },
            new("Glow Squid", "entity_glow_squid") { Price = 100, Category = "Summon Entity", Description ="Spawns a Glow Squid, a passive animal that emits light" },
            new("Goat", "entity_goat") { Price = 100, Category = "Summon Entity", Description ="Spawns a Goat, a passive animal that can be milked and sometimes lunges at players" },
            new("Guardian", "entity_guardian") { Price = 300, Category = "Summon Entity", Description ="Spawns a Guardian, an enemy that attacks with a laser beam" },
            new("Hoglin", "entity_hoglin") { Price = 800, Category = "Summon Entity", Description ="Spawns a Hoglin, a hostile enemy that can be used as a source of food" },
            new("Horse", "entity_horse") { Price = 100, Category = "Summon Entity", Description ="Spawns a Horse, a passive animal that can be tamed and ridden" },
            new("Husk", "entity_husk") { Price = 200, Category = "Summon Entity", Description ="Spawns a Husk, an enemy that spawns in deserts and inflicts hunger when it attacks" },
            new("Illusioner", "entity_illusioner") { Price = 400, Category = "Summon Entity", Description ="Spawns an Illusioner, an unused enemy that attacks by summoning illusions" },
            new("Iron Golem", "entity_iron_golem") { Price = 300, Category = "Summon Entity", Description ="Spawns an Iron Golem, a passive creature that attacks enemies and can be used as a source of iron" },
            new("Lightning Bolt", "entity_lightning") { Price = 300, Category = "Summon Entity", Description ="Spawns a Lightning Bolt, inflicting damage and fire burns to the player and nearby entities" },
            new("Llama", "entity_llama") { Price = 100, Category = "Summon Entity", Description ="Spawns a Llama, a passive animal that can be used to transport items" },
            new("Magma Cube", "entity_magma_cube") { Price = 400, Category = "Summon Entity", Description ="Spawns a Magma Cube, a stronger version of the Slime enemy" },
            new("Minecart", "entity_minecart") { Price = 25, Category = "Summon Entity", Description ="Spawns a Minecart, a vehicle that can be used to travel across railways" },
            new("Minecart with Chest", "entity_minecart_chest") { Price = 100, Category = "Summon Entity", Description ="Spawns a Minecart with Chest, a vehicle that can be used to travel across railways and store items. Comes filled with items from a random loot table." },
            new("Mooshroom", "entity_mushroom_cow") { Price = 100, Category = "Summon Entity", Description ="Spawns a Mooshroom, a passive animal that can be used as a source of food, milk, or stew" },
            new("Mule", "entity_mule") { Price = 100, Category = "Summon Entity", Description ="Spawns a Mule, a passive animal that can be ridden and used to transport items" },
            new("Ocelot", "entity_ocelot") { Price = 100, Category = "Summon Entity", Description ="Spawns an Ocelot, a passive animal that hunts down chickens and baby turtles. Not to be confused with the Cat." },
            new("Panda", "entity_panda") { Price = 100, Category = "Summon Entity", Description ="Spawns a Panda, a neutral animal that is protective of its cubs" },
            new("Parrot", "entity_parrot") { Price = 100, Category = "Summon Entity", Description ="Spawns a Parrot, a passive animal that can be tamed to follow you around and imitate other mobs" },
            new("Phantom", "entity_phantom") { Price = 300, Category = "Summon Entity", Description ="Spawns a Phantom, an airborne enemy that attacks insomniacs" },
            new("Pig", "entity_pig") { Price = 100, Category = "Summon Entity", Description ="Spawns a Pig, a passive animal that can be used as a source of food" },
            new("Piglin", "entity_piglin") { Price = 300, Category = "Summon Entity", Description ="Spawns a Piglin, a hostile enemy that can be bartered with" },
            new("Piglin Brute", "entity_piglin_brute") { Price = 1000, Category = "Summon Entity", Description ="Spawns a Piglin Brute, a merciless enemy that can kill an unarmed player in 2-3 hits" },
            new("Pillager", "entity_pillager") { Price = 350, Category = "Summon Entity", Description ="Spawns a Pillager, an enemy that attacks with a crossbow" },
            new("Polar Bear", "entity_polar_bear") { Price = 100, Category = "Summon Entity", Description ="Spawns a Polar Bear, a neutral animal that is protective of its cubs" },
            new("Primed TNT", "entity_primed_tnt") { Price = 500, Category = "Summon Entity", Description ="Spawns a Primed TNT which explodes after a short delay" },
            new("Pufferfish", "entity_pufferfish") { Price = 300, Category = "Summon Entity", Description ="Spawns a Pufferfish, a passive animal that inflicts poison when threatened" },
            new("Rabbit", "entity_rabbit") { Price = 100, Category = "Summon Entity", Description ="Spawns a Rabbit, a passive animal that can be used as a source of food" },
            new("Ravager", "entity_ravager") { Price = 1000, Category = "Summon Entity", Description ="Spawns a Ravager, a hostile enemy that attacks with a powerful bite" },
            new("Salmon", "entity_salmon") { Price = 100, Category = "Summon Entity", Description ="Spawns a Salmon, a passive animal that can be used as a source of food" },
            new("Sheep", "entity_sheep") { Price = 100, Category = "Summon Entity", Description ="Spawns a Sheep, a passive animal that can be used as a source of wool and food" },
            new("Shulker", "entity_shulker") { Price = 400, Category = "Summon Entity", Description ="Spawns a Shulker, a hostile enemy that attacks by shooting projectiles that make the player levitate" },
            new("Silverfish", "entity_silverfish") { Price = 300, Category = "Summon Entity", Description ="Spawns a Silverfish, a hostile enemy that attacks and hides in blocks" },
            new("Skeleton", "entity_skeleton") { Price = 300, Category = "Summon Entity", Description ="Spawns a Skeleton, a hostile enemy that attacks with a bow" },
            new("Skeleton Horse", "entity_skeleton_horse") { Price = 100, Category = "Summon Entity", Description ="Spawns a Skeleton Horse, a passive animal that can be tamed and ridden" },
            new("Slime", "entity_slime") { Price = 300, Category = "Summon Entity", Description ="Spawns a Slime, a hostile enemy that attacks by jumping on the player" },
            new("Sniffer", "entity_sniffer") { Price = 100, Category = "Summon Entity", Description ="Spawns a Sniffer, a passive creature that digs seeds out of the ground" },
            new("Snow Golem", "entity_snowman") { Price = 100, Category = "Summon Entity", Description ="Spawns a Snow Golem, a passive creature that throws snowballs at hostile mobs... to little effect" },
            new("Spider", "entity_spider") { Price = 300, Category = "Summon Entity", Description ="Spawns a Spider, a hostile enemy that climbs up walls attacks by jumping on the player" },
            new("Squid", "entity_squid") { Price = 100, Category = "Summon Entity", Description ="Spawns a Squid, a passive animal that can be used as a source of ink sacs" },
            new("Stray", "entity_stray") { Price = 325, Category = "Summon Entity", Description ="Spawns a Stray, a hostile enemy that attacks with a bow and inflicts slowness" },
            new("Strider", "entity_strider") { Price = 100, Category = "Summon Entity", Description ="Spawns a Strider, a passive animal that can be ridden and used to travel over lava" },
            new("Tadpole", "entity_tadpole") { Price = 100, Category = "Summon Entity", Description ="Spawns a Tadpole, a passive animal that grows into a frog" },
            new("Trader Llama", "entity_trader_llama") { Price = 100, Category = "Summon Entity", Description ="Spawns a Trader Llama, a passive animal that accompanies Wandering Traders" },
            new("Tropical Fish", "entity_tropical_fish") { Price = 100, Category = "Summon Entity", Description ="Spawns a Tropical Fish, a passive animal that can be used as a source of food" },
            new("Turtle", "entity_turtle") { Price = 100, Category = "Summon Entity", Description ="Spawns a Turtle, a passive animal that can be used to get scutes" },
            new("Vex", "entity_vex") { Price = 300, Category = "Summon Entity", Description ="Spawns a Vex, a small hostile enemy that lunges at players and phases through blocks" },
            new("Villager", "entity_villager") { Price = 150, Category = "Summon Entity", Description ="Spawns a Villager, a passive creature that can be traded with" },
            new("Vindicator", "entity_vindicator") { Price = 500, Category = "Summon Entity", Description ="Spawns a Vindicator, a hostile enemy that attacks with an axe" },
            new("Wandering Trader", "entity_wandering_trader") { Price = 150, Category = "Summon Entity", Description ="Spawns a Wandering Trader, a passive creature that can be traded with" },
            new("Warden", "entity_warden") { Price = 3000, Category = "Summon Entity", Description ="Spawns a Warden, a powerful hostile enemy that follows players by sensing vibrations in the ground" },
            new("Witch", "entity_witch") { Price = 300, Category = "Summon Entity", Description ="Spawns a Witch, a hostile enemy that attacks with potions" },
            new("Wither", "entity_wither") { Price = 3000, Category = "Summon Entity", Description ="Spawns a Wither, a powerful aerial boss that attacks by firing its heads as projectiles" },
            new("Wither Skeleton", "entity_wither_skeleton") { Price = 400, Category = "Summon Entity", Description ="Spawns a Wither Skeleton, a hostile enemy that attacks with a sword and inflicts the withering effect" },
            new("Wolf", "entity_wolf") { Price = 200, Category = "Summon Entity", Description ="Spawns a Wolf, a passive animal that can be tamed to fight by your side" },
            new("Zoglin", "entity_zoglin") { Price = 1000, Category = "Summon Entity", Description ="Spawns a Zoglin, a hostile enemy that flings its targets into the air" },
            new("Zombie", "entity_zombie") { Price = 200, Category = "Summon Entity", Description ="Spawns a Zombie, a hostile enemy that attacks with its fists" },
            new("Zombie Horse", "entity_zombie_horse") { Price = 100, Category = "Summon Entity", Description ="Spawns a Zombie Horse, a passive animal that can be tamed and ridden" },
            new("Zombie Villager", "entity_zombie_villager") { Price = 200, Category = "Summon Entity", Description ="Spawns a Zombie Villager, a hostile enemy that attacks with its fists. It can be cured using magical potions." },
            new("Zombified Piglin", "entity_zombified_piglin") { Price = 200, Category = "Summon Entity", Description ="Spawns a Zombified Piglin, a neutral enemy that attacks with a sword when it or its allies are attacked" },
            // remove nearest entity
            new("Allay", "remove_entity_allay") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Armor Stand", "remove_entity_armor_stand") { Price = 200, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Axolotl", "remove_entity_axolotl") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Bat", "remove_entity_bat") { Price = 1, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Bee", "remove_entity_bee") { Price = 100, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Blaze", "remove_entity_blaze") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Boat", "remove_entity_boat") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Boat with Chest", "remove_entity_chest_boat") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Camel", "remove_entity_camel") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Cat", "remove_entity_cat") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Cave Spider", "remove_entity_cave_spider") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Chicken", "remove_entity_chicken") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Cod", "remove_entity_cod") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Cow", "remove_entity_cow") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Creeper", "remove_entity_creeper") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Dolphin", "remove_entity_dolphin") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Donkey", "remove_entity_donkey") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Drowned", "remove_entity_drowned") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Elder Guardian", "remove_entity_elder_guardian") { Price = 1000, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Ender Dragon", "remove_entity_ender_dragon") { Price = 2000, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Enderman", "remove_entity_enderman") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Endermite", "remove_entity_endermite") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Evoker", "remove_entity_evoker") { Price = 200, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Fox", "remove_entity_fox") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Frog", "remove_entity_frog") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Ghast", "remove_entity_ghast") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Giant", "remove_entity_giant") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Glow Squid", "remove_entity_glow_squid") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Goat", "remove_entity_goat") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Guardian", "remove_entity_guardian") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Hoglin", "remove_entity_hoglin") { Price = 300, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Horse", "remove_entity_horse") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Husk", "remove_entity_husk") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Illusioner", "remove_entity_illusioner") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Iron Golem", "remove_entity_iron_golem") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Llama", "remove_entity_llama") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Magma Cube", "remove_entity_magma_cube") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Minecart", "remove_entity_minecart") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Minecart with Chest", "remove_entity_minecart_chest") { Price = 100, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Mooshroom", "remove_entity_mushroom_cow") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Mule", "remove_entity_mule") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Ocelot", "remove_entity_ocelot") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Panda", "remove_entity_panda") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Parrot", "remove_entity_parrot") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Phantom", "remove_entity_phantom") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Pig", "remove_entity_pig") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Piglin", "remove_entity_piglin") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Piglin Brute", "remove_entity_piglin_brute") { Price = 400, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Pillager", "remove_entity_pillager") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Polar Bear", "remove_entity_polar_bear") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Pufferfish", "remove_entity_pufferfish") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Rabbit", "remove_entity_rabbit") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Ravager", "remove_entity_ravager") { Price = 400, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Salmon", "remove_entity_salmon") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Sheep", "remove_entity_sheep") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Shulker", "remove_entity_shulker") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Silverfish", "remove_entity_silverfish") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Skeleton", "remove_entity_skeleton") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Skeleton Horse", "remove_entity_skeleton_horse") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Slime", "remove_entity_slime") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Sniffer", "remove_entity_sniffer") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Snow Golem", "remove_entity_snowman") { Price = 100, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Spider", "remove_entity_spider") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Squid", "remove_entity_squid") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Stray", "remove_entity_stray") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Strider", "remove_entity_strider") { Price = 300, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Tadpole", "remove_entity_tadpole") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Trader Llama", "remove_entity_trader_llama") { Price = 100, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Tropical Fish", "remove_entity_tropical_fish") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Turtle", "remove_entity_turtle") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Vex", "remove_entity_vex") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Villager", "remove_entity_villager") { Price = 200, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Vindicator", "remove_entity_vindicator") { Price = 200, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Wandering Trader", "remove_entity_wandering_trader") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Warden", "remove_entity_warden") { Price = 3000, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Witch", "remove_entity_witch") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Wither", "remove_entity_wither") { Price = 3000, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Wither Skeleton", "remove_entity_wither_skeleton") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Wolf", "remove_entity_wolf") { Price = 200, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Zoglin", "remove_entity_zoglin") { Price = 400, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Zombie", "remove_entity_zombie") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Zombie Horse", "remove_entity_zombie_horse") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Zombie Villager", "remove_entity_zombie_villager") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            new("Zombified Piglin", "remove_entity_zombified_piglin") { Price = 150, Category = "Remove Entity", Description ="Removes a single nearby mob if one is nearby. The effect will try waiting for one to be near but will refund after a few minutes if none are found." },
            // applies potion effects to every player
            new("Absorption", "potion_absorption") { Price = 50, Duration = 20, SortName = "Potion: Absorption", Category = "Potion Effects", Description = "Grants extra health that cannot be regenerated" },
            new("Bad Omen", "potion_bad_omen") { Price = 400, Duration = 20, SortName = "Potion: Bad Omen", Category = "Potion Effects", Description = "Causes a village raid when a player possessing this effect is inside of a village" },
            new("Blindness", "potion_blindness") { Price = 100, Duration = 20, SortName = "Potion: Blindness", Category = "Potion Effects", Description = "Temporarily reduces a player's range of vision and disables their sprinting" },
            new("Conduit Power", "potion_conduit_power") { Price = 50, Duration = 20, SortName = "Potion: Conduit Power", Category = "Potion Effects", Description = "Grants water breathing, night vision, and haste when underwater" },
            new("Darkness", "potion_darkness") { Price = 75, Duration = 20, SortName = "Potion: Darkness", Category = "Potion Effects", Description = "Temporarily reduces a player's range of vision" },
            new("Dolphin's Grace", "potion_dolphins_grace") { Price = 50, Duration = 20, SortName = "Potion: Dolphin's Grace", Category = "Potion Effects", Description = "Increases swimming speed" },
            new("Fire Resistance", "potion_fire_resistance") { Price = 50, Duration = 20, SortName = "Potion: Fire Resistance", Category = "Potion Effects", Description = "Grants invincibility from fire and lava damage" },
            new("Glowing", "potion_glowing") { Price = 25, Duration = 20, SortName = "Potion: Glowing", Category = "Potion Effects", Description = "Gives the player a glowing white outline that can be seen through walls" },
            new("Haste", "potion_haste") { Price = 50, Duration = 20, SortName = "Potion: Haste", Category = "Potion Effects", Description = "Increases mining speed" },
            new("Health Boost", "potion_health_boost") { Price = 50, Duration = 20, SortName = "Potion: Health Boost", Category = "Potion Effects", Description = "Increases maximum health" },
            new("Invisibility", "potion_invisibility") { Price = 25, Duration = 20, SortName = "Potion: Invisibility", Category = "Potion Effects", Description = "Makes the player invisible" },
            new("Levitation", "potion_levitation") { Price = 100, Duration = 20, SortName = "Potion: Levitation", Category = "Potion Effects", Description = "Gradually lifts the player up into the air" },
            new("Mining Fatigue", "potion_mining_fatigue") { Price = 50, Duration = 20, SortName = "Potion: Mining Fatigue", Category = "Potion Effects", Description = "Decreases mining speed" },
            new("Nausea", "potion_nausea") { Price = 100, Duration = 20, SortName = "Potion: Nausea", Category = "Potion Effects", Description = "Makes the player's screen shake" },
            new("Night Vision", "potion_night_vision") { Price = 50, Duration = 20, SortName = "Potion: Night Vision", Category = "Potion Effects", Description = "Allows the player to see inside dark areas" },
            new("Poison", "potion_poison") { Price = 50, Duration = 20, SortName = "Potion: Poison", Category = "Potion Effects", Description = "Gradually damages the player" },
            new("Regeneration", "potion_regeneration") { Price = 50, Duration = 20, SortName = "Potion: Regeneration", Category = "Potion Effects", Description = "Gradually heals the player" },
            new("Resistance", "potion_resistance") { Price = 50, Duration = 20, SortName = "Potion: Resistance", Category = "Potion Effects", Description = "Reduces damage taken" },
            new("Slowness", "potion_slowness") { Price = 50, Duration = 20, SortName = "Potion: Slowness", Category = "Potion Effects", Description = "Decreases the player's walking speed" },
            new("Speed", "potion_speed") { Price = 50, Duration = 20, SortName = "Potion: Speed", Category = "Potion Effects", Description = "Increases the player's walking speed" },
            new("Strength", "potion_strength") { Price = 50, Duration = 20, SortName = "Potion: Strength", Category = "Potion Effects", Description = "Increases the player's damage output" },
            new("Water Breathing", "potion_water_breathing") { Price = 50, Duration = 20, SortName = "Potion: Water Breathing", Category = "Potion Effects", Description = "Grants the ability to breathe underwater" },
            new("Weakness", "potion_weakness") { Price = 50, Duration = 20, SortName = "Potion: Weakness", Category = "Potion Effects", Description = "Decreases the player's damage output" },
            // gravity commands
            new("Zero Gravity", "zero_gravity") { Price = 100, Duration = 20, SortName = "Gravity: 0", Category = new[]{"Potion Effects", "Movement"}, Description = "Disables player gravity, allowing players to float in the air" },
            new("Low Gravity", "low_gravity") { Price = 50, Duration = 20, SortName = "Gravity: 1", Category = new[]{"Potion Effects", "Movement"}, Description = "Decreases player gravity, disallowing players from jumping up blocks" },
            new("High Gravity", "high_gravity") { Price = 100, Duration = 20, SortName = "Gravity: 2", Category = new[]{"Potion Effects", "Movement"}, Description = "Increases player gravity, allowing players to jump higher and fall slower" },
            new("Maximum Gravity", "maximum_gravity") { Price = 200, Duration = 20, SortName = "Gravity: 3", Category = new[]{"Potion Effects", "Movement"}, Description = "Maximizes player gravity, disallowing players from jumping up blocks and rapidly pulling them down to the ground" },
            // places a block at everyone's feet
            new("Bedrock", "block_bedrock") { Price = 200, SortName = "Place Bedrock", Category = "Place Blocks", Description = "Places the unbreakable bedrock block at every player's feet" },
            new("Cobweb", "block_cobweb") { Price = 25, SortName = "Place Cobweb", Category = "Place Blocks", Description = "Places a cobweb block at every player's feet to slow them down" },
            // TODO: temporarily disabled as it's prone to failure. should add failsafes or replace with a new effect that uses API methods directly. | new("Fire", "block_fire") { Price = 200, SortName = "Place Fire", Category = "Place Blocks", Description = "Places a fire block at every player's feet to set them ablaze" },
            new("Sculk Catalyst", "block_sculk_catalyst") { Price = 25, SortName = "Place Sculk Catalyst", Category = "Place Blocks", Description = "Places a sculk catalyst at every player's feet to absorb the XP from mobs that die nearby" },
            new("Wither Rose", "block_wither_rose") { Price = 100, SortName = "Place Wither Rose", Category = "Place Blocks", Description = "Places a toxic wither rose at every player's feet to poison them" },
            new("Lightning Rod", "block_lightning_rod") { Price = 25, SortName = "Place Lightning Rod", Category = "Place Blocks", Description = "Places a lightning rod at every player's feet to attract lightning strikes" },
            new("Water", "block_water") { Price = 50, SortName = "Place Water", Category = "Place Blocks", Description = "Places a flowing water block at every player's feet" },
            // places a block several blocks above everyone's head
            new("Falling Anvil", "falling_block_anvil") { Price = 100, SortName = "Place Falling Anvil", Category = "Place Blocks", Description = "Drops a genuine ACME corp. anvil block on every player" },
            new("Falling Sand", "falling_block_sand") { Price = 25, SortName = "Place Falling Sand", Category = "Place Blocks", Description = "Drops a sand block on every player" }, // TODO: drop random between Sand, Red Sand, and Gravel. and maybe drop a whole column?
            // TODO: new("Random Falling Block", "falling_block_random") { Price = 50, SortName = "Place Falling Random Block", Category = "Place Blocks", Description = "Drops a totally random block on every player" },
            // apply enchants
            new("Remove Enchantments", "remove_enchants") { Price = 200, SortName = "Enchantment: 0", Category = "Enchantments", Description = "Removes all enchants from the held item or a random piece of armor" },
            new("Apply Aqua Affinity", "enchant_aqua_affinity") { Price = 50, SortName = "Enchantment: Aqua Affinity", Category = "Enchantments", Description = "Increases underwater mining speed" },
            new("Apply Bane of Arthropods V", "enchant_bane_of_arthropods") { Price = 50, SortName = "Enchantment: Bane of Arthropods", Category = "Enchantments", Description = "Increases damage dealt to arthropod mobs (spiders, cave spiders, bees, silverfish, and endermites)" },
            new("Apply Blast Protection IV", "enchant_blast_protection") { Price = 50, SortName = "Enchantment: Blast Protection", Category = "Enchantments", Description = "Reduces damage taken from explosions" },
            new("Apply Channeling", "enchant_channeling") { Price = 50, SortName = "Enchantment: Channeling", Category = "Enchantments", Description = "Makes tridents produce lightning when thrown while raining" },
            new("Apply Curse of Binding", "enchant_curse_of_binding") { Price = 100, SortName = "Enchantment: Curse of Binding", Category = "Enchantments", Description = "Armor pieces with this enchantment cannot be taken off until death" },
            new("Apply Curse of Vanishing", "enchant_curse_of_vanishing") { Price = 150, SortName = "Enchantment: Curse of Vanishing", Category = "Enchantments", Description = "Items with this enchantment will disappear upon death" },
            new("Apply Depth Strider III", "enchant_depth_strider") { Price = 50, SortName = "Enchantment: Depth Strider", Category = "Enchantments", Description = "Increases swimming speed" },
            new("Apply Efficiency V", "enchant_efficiency") { Price = 50, SortName = "Enchantment: Efficiency", Category = "Enchantments", Description = "Increases mining speed" },
            new("Apply Feather Falling IV", "enchant_feather_falling") { Price = 50, SortName = "Enchantment: Feather Falling", Category = "Enchantments", Description = "Reduces damage taken from fall damage" },
            new("Apply Fire Aspect II", "enchant_fire_aspect") { Price = 50, SortName = "Enchantment: Fire Aspect", Category = "Enchantments", Description = "Melee weapons with this enchantment set mobs on fire upon dealing damage" },
            new("Apply Fire Protection IV", "enchant_fire_protection") { Price = 50, SortName = "Enchantment: Fire Protection", Category = "Enchantments", Description = "Reduces damage taken from fire" },
            new("Apply Flame", "enchant_flame") { Price = 50, SortName = "Enchantment: Flame", Category = "Enchantments", Description = "Bows with this enchantment shoot flaming arrows that set mobs on fire" },
            new("Apply Fortune III", "enchant_fortune") { Price = 50, SortName = "Enchantment: Fortune", Category = "Enchantments", Description = "Increases the drops of minerals (iron, diamond, etc.)" },
            new("Apply Frost Walker II", "enchant_frost_walker") { Price = 50, SortName = "Enchantment: Frost Walker", Category = "Enchantments", Description = "Walking near water with this enchantment will temporarily turn it into ice" },
            new("Apply Impaling V", "enchant_impaling") { Price = 50, SortName = "Enchantment: Impaling", Category = "Enchantments", Description = "Increases damage dealt by tridents to aquatic mobs" },
            new("Apply Infinity", "enchant_infinity") { Price = 50, SortName = "Enchantment: Infinity", Category = "Enchantments", Description = "Prevents bows from consuming arrows" },
            new("Apply Knockback II", "enchant_knockback") { Price = 50, SortName = "Enchantment: Knockback", Category = "Enchantments", Description = "Increases the distance that mobs get knocked back when attacked by a melee weapon" },
            new("Apply Looting III", "enchant_looting") { Price = 50, SortName = "Enchantment: Looting", Category = "Enchantments", Description = "Increases the number of items dropped by mobs when killed" },
            new("Apply Loyalty III", "enchant_loyalty") { Price = 50, SortName = "Enchantment: Loyalty", Category = "Enchantments", Description = "Tridents with this enchantment will return to the thrower" },
            new("Apply Luck of the Sea III", "enchant_luck_of_the_sea") { Price = 50, SortName = "Enchantment: Luck of the Sea", Category = "Enchantments", Description = "Increases luck while fishing" },
            new("Apply Lure III", "enchant_lure") { Price = 50, SortName = "Enchantment: Lure", Category = "Enchantments", Description = "Decreases the wait time for a bite on your fishing hook" },
            new("Apply Mending", "enchant_mending") { Price = 50, SortName = "Enchantment: Mending", Category = "Enchantments", Description = "Items with this enchantment that are held or worn by a player will be repaired as XP is collected" },
            new("Apply Multishot", "enchant_multishot") { Price = 50, SortName = "Enchantment: Multishot", Category = "Enchantments", Description = "Makes crossbows fire three arrows" },
            new("Apply Piercing IV", "enchant_piercing") { Price = 50, SortName = "Enchantment: Piercing", Category = "Enchantments", Description = "Lets crossbow arrows penetrate four mobs" },
            new("Apply Power V", "enchant_power") { Price = 50, SortName = "Enchantment: Power", Category = "Enchantments", Description = "Increases damage dealt by bows" },
            new("Apply Projectile Protection IV", "enchant_projectile_protection") { Price = 50, SortName = "Enchantment: Projectile Protection", Category = "Enchantments", Description = "Reduces damage taken from arrows" },
            new("Apply Protection IV", "enchant_protection") { Price = 50, SortName = "Enchantment: Protection", Category = "Enchantments", Description = "Reduces damage taken from all sources" },
            new("Apply Punch II", "enchant_punch") { Price = 50, SortName = "Enchantment: Punch", Category = "Enchantments", Description = "Increases the distance that mobs get knocked back when shot by a bow" },
            new("Apply Quick Charge III", "enchant_quick_charge") { Price = 50, SortName = "Enchantment: Quick Charge", Category = "Enchantments", Description = "Reduces the time required to charge a crossbow" },
            new("Apply Respiration III", "enchant_respiration") { Price = 50, SortName = "Enchantment: Respiration", Category = "Enchantments", Description = "Extends breathing time underwater" },
            new("Apply Riptide III", "enchant_riptide") { Price = 50, SortName = "Enchantment: Riptide", Category = "Enchantments", Description = "When throwing a riptide trident inside rain or a body of water, the thrower will be rocketed in the direction they are facing" },
            new("Apply Sharpness V", "enchant_sharpness") { Price = 50, SortName = "Enchantment: Sharpness", Category = "Enchantments", Description = "Increases damage dealt by melee damage" },
            new("Apply Silk Touch", "enchant_silk_touch") { Price = 50, SortName = "Enchantment: Silk Touch", Category = "Enchantments", Description = "Allows various blocks to drop themselves instead of their usual items" },
            new("Apply Smite V", "enchant_smite") { Price = 50, SortName = "Enchantment: Smite", Category = "Enchantments", Description = "Increases damage dealt to undead mobs (zombies, skeletons, etc.)" },
            new("Apply Soul Speed III", "enchant_soul_speed") { Price = 50, SortName = "Enchantment: Soul Speed", Category = "Enchantments", Description = "Increases walking speed on soul sand at the cost of armor durability" },
            new("Apply Sweeping Edge III", "enchant_sweeping_edge") { Price = 50, SortName = "Enchantment: Sweeping Edge", Category = "Enchantments", Description = "Increases the damage done by sweeping attacks" },
            new("Apply Swift Sneak III", "enchant_swift_sneak") { Price = 50, SortName = "Enchantment: Swift Sneak", Category = "Enchantments", Description = "Increases sneaking speed" },
            new("Apply Thorns III", "enchant_thorns") { Price = 50, SortName = "Enchantment: Thorns", Category = "Enchantments", Description = "Deals damage to attackers when hit" },
            new("Apply Unbreaking III", "enchant_unbreaking") { Price = 50, SortName = "Enchantment: Unbreaking", Category = "Enchantments", Description = "Lessens the speed at which items break" },
            // give items: misc
            // TODO: add goat horns to give/remove items? or maybe just add it as a sound effect to Annoying Popup? idk
            new("Elytra", "give_elytra") { Price = 500, SortName = "Give: 0: Elytra", Category = "Give Items", Description = "Gives an Elytra, a pair of wings that allows the player to fly" },
            new("Eye of Ender", "give_ender_eye") { Price = 100, Quantity = 64, SortName = "Give: 0: Eye of Ender", Category = "Give Items", Description = "Gives an Eye of Ender, a critical part of finding the End Portal and completing the game" },
            new("End Portal Frame", "give_end_portal_frame") { Price = 200, Quantity = 64, SortName = "Give: 0: End Portal Frame", Category = "Give Items", Description = "Gives a part of the frame required to build an end portal. Note that some server configurations may limit how many players get one." },
            new("Recovery Compass", "give_recovery_compass") { Price = 400, Quantity = 64, SortName = "Give: 0: Recovery Compass", Category = "Give Items", Description = "Gives a compass that points to the location of your most recent death" },
            // give items: food
            new("Cooked Porkchop", "give_cooked_porkchop") { Price = 40, Quantity = 64, SortName = "Give: 1: 0", Category = "Give Items", Description = "Gives a Cooked Porkchop, a food item that restores 4 bars of hunger" },
            new("Golden Apple", "give_golden_apple") { Price = 200, Quantity = 64, SortName = "Give: 1: 8", Category = "Give Items", Description = "Gives a Golden Apple, a food item that restores 2 bars of hunger, 2 bars of health, and temporarily grants an extra 2 hearts of maximum health" },
            new("Enchanted Golden Apple", "give_enchanted_golden_apple") { Price = 300, Quantity = 64, SortName = "Give: 1: 9", Category = "Give Items", Description = "Gives an Enchanted Golden Apple, a food item that restores 2 bars of hunger, 8 bars of health, and temporarily grants fire resistance, an extra 8 hearts of maximum health, resistance, and fire resistance" },
            // give items: minerals
            new("Coal", "give_coal") { Price = 10, Quantity = 64, SortName = "Give: 2: 0: 0", Category = "Give Items", Description = "Gives a piece of Coal, a mineral that can be used to smelt ores and cook food" },
            new("Gold Ingot", "give_gold_ingot") { Price = 20, Quantity = 64, SortName = "Give: 2: 0: 1", Category = "Give Items", Description = "Gives a Gold Ingot, a mineral that can be used to craft tools, armor, and golden apples" },
            new("Iron Ingot", "give_iron_ingot") { Price = 25, Quantity = 64, SortName = "Give: 2: 0: 2", Category = "Give Items", Description = "Gives an Iron Ingot, a mineral that can be used to craft tools, armor, and shields" },
            new("Diamond", "give_diamond") { Price = 100, Quantity = 64, SortName = "Give: 2: 0: 3", Category = "Give Items", Description = "Gives a Diamond, a mineral that can be used to craft tools and armor" },
            new("Netherite Ingot", "give_netherite_ingot") { Price = 200, Quantity = 64, SortName = "Give: 2: 0: 4", Category = "Give Items", Description = "Gives a Netherite Ingot, a mineral that can be used to upgrade diamond tools and armor" },
            // give items: tools: pickaxes
            new("Golden Pickaxe", "give_golden_pickaxe") { Price = 25, SortName = "Give: 3: 0", Category = "Give Items", Description = "Gives a Golden Pickaxe, a fast but fragile tool that can't mine much more than stone" },
            new("Wooden Pickaxe", "give_wooden_pickaxe") { Price = 25, SortName = "Give: 3: 1", Category = "Give Items", Description = "Gives a Wooden Pickaxe, a tool that can be used to mine stone and coal ore" },
            new("Stone Pickaxe", "give_stone_pickaxe") { Price = 50, SortName = "Give: 3: 2", Category = "Give Items", Description = "Gives a Stone Pickaxe, a tool that can be used to mine iron ore" },
            new("Iron Pickaxe", "give_iron_pickaxe") { Price = 100, SortName = "Give: 3: 3", Category = "Give Items", Description = "Gives an Iron Pickaxe, a tool that can be used to mine diamond ore" },
            new("Diamond Pickaxe", "give_diamond_pickaxe") { Price = 250, SortName = "Give: 3: 4", Category = "Give Items", Description = "Gives a Diamond Pickaxe, a tool that can be used to mine obsidian and ancient debris" },
            new("Netherite Pickaxe", "give_netherite_pickaxe") { Price = 350, SortName = "Give: 3: 5", Category = "Give Items", Description = "Gives a Netherite Pickaxe, a tool that mines blocks faster and longer than any other" },
            // give items: weapons: swords
            new("Golden Sword", "give_golden_sword") { Price = 15, SortName = "Give: 4: 0: 0", Category = "Give Items", Description = "Gives a Golden Sword, a highly fragile melee weapon" },
            new("Wooden Sword", "give_wooden_sword") { Price = 25, SortName = "Give: 4: 0: 1", Category = "Give Items", Description = "Gives a Wooden Sword, a primitive melee weapon" },
            new("Stone Sword", "give_stone_sword") { Price = 50, SortName = "Give: 4: 0: 2", Category = "Give Items", Description = "Gives a Stone Sword, a slightly stronger melee weapon" },
            new("Iron Sword", "give_iron_sword") { Price = 100, SortName = "Give: 4: 0: 3", Category = "Give Items", Description = "Gives an Iron Sword, an average melee weapon" },
            new("Diamond Sword", "give_diamond_sword") { Price = 250, SortName = "Give: 4: 0: 4", Category = "Give Items", Description = "Gives a Diamond Sword, a powerful melee weapon" },
            new("Netherite Sword", "give_netherite_sword") { Price = 350, SortName = "Give: 4: 0: 5", Category = "Give Items", Description = "Gives a Netherite Sword, a very powerful and durable melee weapon" },
            // give items: weapons: misc
            new("Trident", "give_trident") { Price = 300, SortName = "Give: 4: Trident", Category = "Give Items", Description = "Gives a Trident, a powerful melee weapon that can be thrown" },
            // takes items: misc
            new("Elytra", "take_elytra") { Price = 700, SortName = "Take: 0: Elytra", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Eye of Ender", "take_ender_eye") { Price = 200, Quantity = 64, SortName = "Take: 0: Eye of Ender", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("End Portal Frame", "take_end_portal_frame") { Price = 300, Quantity = 64, SortName = "Take: 0: End Portal Frame", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Recovery Compass", "take_recovery_compass") { Price = 500, Quantity = 64, SortName = "Take: 0: Recovery Compass", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // take items: food
            new("Cooked Porkchop", "take_cooked_porkchop") { Price = 100, Quantity = 64, SortName = "Take: 1: 0", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Golden Apple", "take_golden_apple") { Price = 400, Quantity = 64, SortName = "Take: 1: 8", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Enchanted Golden Apple", "take_enchanted_golden_apple") { Price = 500, Quantity = 64, SortName = "Take: 1: 9", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // take items: minerals
            new("Coal", "take_coal") { Price = 50, Quantity = 64, SortName = "Take: 2: 0", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Iron Ingot", "take_iron_ingot") { Price = 100, Quantity = 64, SortName = "Take: 2: 1", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Gold Ingot", "take_gold_ingot") { Price = 100, Quantity = 64, SortName = "Take: 2: 2", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Diamond", "take_diamond") { Price = 300, Quantity = 64, SortName = "Take: 2: 3", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Netherite Ingot", "take_netherite_ingot") { Price = 400, Quantity = 64, SortName = "Take: 2: 4", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // take items: tools: pickaxes
            new("Wooden Pickaxe", "take_wooden_pickaxe") { Price = 50, SortName = "Take: 3: 0: 0", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Stone Pickaxe", "take_stone_pickaxe") { Price = 100, SortName = "Take: 3: 0: 1", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Golden Pickaxe", "take_golden_pickaxe") { Price = 100, SortName = "Take: 3: 0: 2", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Iron Pickaxe", "take_iron_pickaxe") { Price = 400, SortName = "Take: 3: 0: 3", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Diamond Pickaxe", "take_diamond_pickaxe") { Price = 500, SortName = "Take: 3: 0: 4", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Netherite Pickaxe", "take_netherite_pickaxe") { Price = 500, SortName = "Take: 3: 0: 5", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // take items: weapons: swords
            new("Wooden Sword", "take_wooden_sword") { Price = 50, SortName = "Take: 4: 0: 0", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Stone Sword", "take_stone_sword") { Price = 100, SortName = "Take: 4: 0: 1", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Golden Sword", "take_golden_sword") { Price = 100, SortName = "Take: 4: 0: 2", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Iron Sword", "take_iron_sword") { Price = 400, SortName = "Take: 4: 0: 3", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Diamond Sword", "take_diamond_sword") { Price = 500, SortName = "Take: 4: 0: 4", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            new("Netherite Sword", "take_netherite_sword") { Price = 500, SortName = "Take: 4: 0: 5", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // take items: weapons: misc
            new("Trident", "take_trident") { Price = 400, SortName = "Take: 4: Trident", Category = "Take Items", Description = "Tries to take items from a player. Refunds after a few minutes if none can be found." },
            // shaders
            new("Bumpy", "shader_bumpy") { Price = 50, Duration = 30, Category = "Screen Effects", Description = "Adds a faint white outline to everything giving the impression of bumpy textures" },
            new("Creeper TV", "shader_green") { Price = 250, Duration = 30, Category = "Screen Effects", Description = "See the game through the eyes of a creeper... through a CRT" },
            new("CRT", "shader_ntsc") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "Makes the game look like it's running on an old CRT TV" },
            new("Desaturate", "shader_desaturate") { Price = 50, Duration = 30, Category = "Screen Effects", Description = "Sucks the color out of the game" },
            new("Flip", "shader_flip") { Price = 500, Duration = 30, Category = "Screen Effects", Description = "Flips the screen upside-down" },
            new("Invert Colors", "shader_invert") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "Inverts the game's colors to see the game through the eyes of an enderman" },
            new("Oil Painting", "shader_blobs2") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "Makes the game look like a smeary oil painting" },
            new("Pencil Sketch", "shader_pencil") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "Makes the game look like it was sketched with a pencil" },
            new("Prototype", "shader_sobel") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "Makes the game only render edges of textures" },
            new("Psychedelic", "shader_wobble") { Price = 200, Duration = 30, Category = "Screen Effects", Description = "Makes the game rainbowy and wobbly" },
            new("Retro", "shader_bits") { Price = 200, Duration = 30, Category = "Screen Effects", Description = "Makes the game look like it's running on an NES" },
            new("Spider", "shader_spider") { Price = 100, Duration = 30, Category = "Screen Effects", Description = "See the game through the eight eyes of a spider" },
            new("Trail", "shader_phosphor") { Price = 200, Duration = 30, Category = "Screen Effects", Description = "Duplicates every frame to create a ghostly trail effect" },
        };

        public override EffectList Effects => AllEffects;

        public override SimpleTCPClientConnector Connector
        {
            get => base.Connector;
            set
            {
                value.MessageParsed += (sender, response, context) =>
                {
                    if (response.message == null) return;
                    if (response.id == 0 && response.message.StartsWith("_mc_cc_server_status_"))
                    {
                        Log.Debug("Message is server status packet");
                        // incoming packet contains info about supported effects
                        OnServerStatusPacket(response);
                    }
                };
                base.Connector = value;
            }
        }

        private void OnServerStatusPacket(Response response)
        {
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
            foreach (Effect unsupportedEffect in AllEffects.Where(effect => !registeredEffectsList.Contains(effect.ID.ToLower())))
            {
                HideEffect(unsupportedEffect);
            }
            Log.Message("Finished hiding effects");
        }

        private void HideEffect(Effect effect)
        {
            HideEffect(effect.ID);
        }

        private void HideEffect(string effect)
        {
            Log.Message($"Hiding effect {effect}");
            ReportStatus(effect, EffectStatus.MenuHidden);
        }
    }
}
