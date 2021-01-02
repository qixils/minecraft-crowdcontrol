# Minecraft Crowd Control
Minecraft Crowd Control is a plugin for [Paper](https://papermc.io/) 1.16.4 that allows your Twitch community to interact with your game.

This plugin is currently aimed towards streamers without features like channel points or bits, as commands are simply used by typing in chat and work off a cooldown system.
Most commands have some sort of cooldown to prevent them from being used every second, though generally these are in the range of half a minute to 15 minutes.
Cooldowns are global, meaning once one person uses a specific command, nobody else can use it until it has recharged.

Commands used are posted to chat along with who used them. The plugin also posts when cooldowns have expired.

Commands are fully compatible with a multiplayer server, i.e. they will affect everyone (or rarely only one person). There's even a multiplayer-only command or two.

## Installation

Download the [latest plugin jar](https://github.com/lexikiq/minecraft-crowdcontrol/releases/latest) and place it in your plugins folder.
You will also need to be using [Paper](https://papermc.io/) (or [Tuinity](https://github.com/Spottedleaf/Tuinity)).

For those unaware, Paper is a fork (a variation) of Spigot that vastly improves server performance. You should be able to upgrade from Spigot to Paper with no hassle.

To set your Twitch channel, run the plugin once, so it can generate its default config file, then replace `lexikiq` in `.\plugins\CrowdControl\plugin.yml` with your Twitch channel name.

## Commands
List may not be fully complete as it is sort-of manually generated.

This description was largely written with my own stream in mind, so the wording assumes you are playing with friends.

### Miscellaneous
- `!dig` -- dig a 2 block hole under us
- `!dinnerbone` -- flips nearby mobs upside down
- `!flowers` -- place flowers around us (only works on surface)
- `!freeze` -- encases us in glass
- `!hell` -- replaces nearby stones and dirt with gravel (good in caves)
- `!sound` -- plays a random spooky noise
- `!toast` -- You've unlocked a recipe! You've unlocked a recipe! You've unlo-
- `!vein` -- spawns a vein of ore near us... or some silverfish stone :)
- `!zip` -- adds a minute to the in-game time

#### Particles
- `!particle <particle>` -- spawns particles around us

<details>
<summary>Valid Particle Effects:</summary>

- ash
- barrier
- bubble_column_up
- bubble_pop
- campfire_cosy_smoke
- campfire_signal_smoke
- cloud
- composter
- crimson_spore
- crit
- crit_magic
- current_down
- damage_indicator
- dolphin
- dragon_breath
- dripping_honey
- dripping_obsidian_tear
- drip_lava
- drip_water
- enchantment_table
- end_rod
- explosion_huge
- explosion_large
- explosion_normal
- falling_honey
- falling_lava
- falling_nectar
- falling_obsidian_tear
- falling_water
- fireworks_spark
- flame
- flash
- heart
- landing_honey
- landing_lava
- landing_obsidian_tear
- lava
- mob_appearance
- nautilus
- note
- portal
- reverse_portal
- slime
- smoke_large
- smoke_normal
- sneeze
- snowball
- snow_shovel
- soul
- soul_fire_flame
- spell
- spell_instant
- spell_mob
- spell_mob_ambient
- spell_witch
- spit
- squid_ink
- suspended
- suspended_depth
- sweep_attack
- totem
- town_aura
- villager_angry
- villager_happy
- warped_spore
- water_bubble
- water_drop
- water_splash
- water_wake
- white_ash
</details>

#### Inventory
- `!clutter` -- swaps the item in our main with a random item in our inventory
- `!give <item name>` -- gives us 1 item (don't include the <>'s)
- `!take <item name>` -- takes 1 item (don't include the <>'s)
- `!lootbox` -- gives us a random amount of a random item (possibly with enchantments and other stuff!)
- `!name <name>` -- renames our held item (don't include the <>'s)

#### Light
Let there be light! Or not.
- `!dim` -- removes nearby torches
- `!lit` -- covers nearby surfaces with torches

#### Blocks
Sets a block at our feet
- `!cobweb`
- `!fire`
- `!redstone_torch`
- `!tnt`
- `!wither_rose`

#### Falling Blocks
What's that falling from the sky?

Optionally supports setting its relative height by typing i.e. `!anvil 10` (spawns an anvil 10 blocks above our heads)
- `!anvil [height]`
- `!gravel [height]`
- `!red_sand [height]`
- `!sand [height]`

#### Weather
Sets the weather on the server
- `!clear`
- `!downfall`

#### Teleport
- `!up` -- teleports us up by one block
- `!down` -- teleports us down by only block
- `!swap` -- multiplayer only! swaps the positions of online players
- `!tp` -- acts like the players ate a chorus fruit

#### Difficulty
Sets the server difficulty
- `!peaceful`
- `!easy`
- `!normal`
- `!hard`

### Summon Entity
Spawns the specified entity close to us.
- `!armor_stand`
- `!bat`
- `!bee`
- `!boat`
- `!blaze`
- `!cave_spider`
- `!cat`
- `!charged` -- charged creeper
- `!chicken`
- `!cod`
- `!cow`
- `!creeper`
- `!dolphin`
- `!donkey`
- `!drowned`
- `!elder_guardian`
- `!enderman`
- `!endermite`
- `!evoker`
- `!fox`
- `!ghast`
- `!giant`
- `!guardian`
- `!hoglin`
- `!horse`
- `!husk`
- `!illusioner`
- `!iron_golem`
- `!lightning`
- `!llama`
- `!magma_cube`
- `!minecart`
- `!minecart_chest`
- `!minecart_furnace`
- `!minecart_hopper`
- `!minecart_tnt`
- `!mule`
- `!mushroom_cow`
- `!ocelot`
- `!panda`
- `!parrot`
- `!phantom`
- `!pig`
- `!piglin`
- `!piglin_brute`
- `!pillager`
- `!polar_bear`
- `!primed_tnt`
- `!pufferfish`
- `!rabbit`
- `!ravager`
- `!salmon`
- `!sheep`
- `!shulker`
- `!silverfish`
- `!skeleton`
- `!skeleton_horse`
- `!slime`
- `!snowman`
- `!spider`
- `!squid`
- `!stray`
- `!strider`
- `!trader_llama`
- `!tropical_fish`
- `!turtle`
- `!vex`
- `!villager`
- `!vindicator`
- `!wandering_trader`
- `!witch`
- `!wither_skeleton`
- `!wolf`
- `!zoglin`
- `!zombie`
- `!zombie_horse`
- `!zombie_villager`
- `!zombified_piglin`

### Enchantments
Applies an enchantment to our held item
- `!aqua_affinity`
- `!bane_of_arthropods`
- `!bind` -- curse of binding
- `!blast_protection`
- `!channeling`
- `!depth_strider`
- `!efficiency`
- `!feather_falling`
- `!fire_aspect`
- `!fire_protection`
- `!flame`
- `!fortune`
- `!frost_walker`
- `!impaling`
- `!infinity`
- `!knockback`
- `!looting`
- `!loyalty`
- `!luck_of_the_sea`
- `!lure`
- `!mending`
- `!multishot`
- `!piercing`
- `!power`
- `!projectile_protection`
- `!protection`
- `!punch`
- `!quick_charge`
- `!respiration`
- `!riptide`
- `!sharpness`
- `!silk_touch`
- `!smite`
- `!soul_speed`
- `!sweeping`
- `!thorns`
- `!unbreaking`
- `!vanish` -- curse of vanishing

### Potions
Gives us a potion effect for 15 seconds (randomly chooses between level 1 and 2)
- `!absorption`
- `!bad_omen`
- `!blindness`
- `!conduit_power`
- `!confusion` -- nausea
- `!damage_resistance`
- `!dolphins_grace`
- `!fast_digging` -- haste
- `!fire_resistance`
- `!glowing`
- `!harm` -- instant damage
- `!heal` -- instant health
- `!health_boost`
- `!hero_of_the_village`
- `!hunger`
- `!increase_damage` -- strength
- `!invisibility`
- `!jump` -- jump boost
- `!levitation`
- `!luck`
- `!night_vision`
- `!poison`
- `!regeneration`
- `!saturation`
- `!slow` -- slowness
- `!slow_digging` -- mining fatigue
- `!slow_falling`
- `!speed` -- swiftness
- `!unluck`
- `!water_breathing`
- `!weakness`
- `!wither`