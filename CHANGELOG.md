# Changelog

A list of all changes made to the software in reverse chronological order.

## 3.3.4

- Added a new effect Unite Players which teleports all players to one random player
- Added a unique sound effect for Open Lucky Lootbox
- Added extra plants to the Place Flowers effect (mushrooms and fungi)
- Added `CrowdControl` to the prefix of all log messages for easier filtering
- Added new `/crowdcontrol version` command to display the version of the mod
- Added extra diagnostic information to `/crowdcontrol status`
- Changed the Bucket Clutch effect to work in the nether by using a cobweb instead of water
- Changed the Bucket Clutch effect to teleport the player even higher where possible
- Changed the Chorus Fruit effect to better match vanilla behavior
- Fixed warnings about duplicate effects on some modpacks
- Fixed summon effects on Sponge 10+ (1.19+)
- Fixed killer rabbits spawning more than intentional on Paper servers
- Removed air from the Lootbox loot table
- Removed the legacy config options which were no longer usable
- Reduced data sent to the Crowd Control app when using large modpacks

## 3.3.3

- Allowed `/account link` to function once again
- Fixed the descriptions of gravity effects
- Fixed some broken metadata from other mods causing initialization to fail on Sponge 7
- Added extra fail-safes to ensure broken effects don't prevent the mod from loading
- Restored some backwards-compatibility with Crowd Control "1.0"

## 3.3.2

- Improved the reliability of the account auto-detection feature
- Further updates and fixes for Crowd Control 2.0
- Added support for 1.20.1 on Paper and Fabric

## 3.3.1

- Added an account auto-detection feature which tries to automatically link a streamer's Minecraft
  account and their Crowd Control app session to remove the need to run `/account link`. Certain
  event setups may wish to utilize the `autodetect` config option to disable this feature.
- Further updates and fixes for Crowd Control 2.0
- Increased the height of the Dig Hole effect to destroy partial blocks (i.e. slabs, stairs, etc.)
- Teleport to X Location effects are now more likely to place a glass block to stand on
- Backported the Fabric mod to 1.19.2 (experimental) and updated to 1.20 snapshots
- Fixed loading the Sponge 1.16 mod in the CurseForge client
- Updated some error messages to be more relevant

## 3.3.0

- Created a Sponge 8 implementation for SpongeForge 1.16 and SpongeVanilla 1.16+. Speedrun Minecraft
  while your chat spawns dragons and creepers on you!
- Created a Fabric implementation for 1.19.4. Play the latest modpacks alone or with your friends!
- Added new timed effect: Invincible
- Added new timed effect: One-Hit KO
- Added new timed effects for modifying gravity
- Added new client-side effects for Fabric: Inverted Controls & Inverted Mouse
- Added new client-side effects for Fabric: Screen Effects
- Added new effect: Delete Random Item (a variant of Delete Held Item which picks randomly from the
  entire inventory)
- Added the missing effects for giving and taking enchanted golden apples on Sponge 7
- "Teleport All Entities to Player" now acts as "Teleport Nearby Entities to Player" when not
  running as a global effect
- Added new conditions to Do-or-Die for crafting and jumping
- Added new items to give and take, blocks to place, etc.
- Added new entity summons for Minecraft 1.19
- Replaced the individual Fling effects with just one general Fling Randomly effect
- Add support for mods which backport features from newer versions of the game
  (i.e. if a mod adds bees to 1.12.2, the "Summon Bee" effect will become available)
- Global effects like changing the server difficulty are now hidden from the streamer's effect menu
  if the effects have not been enabled
- When global effects are available, various commands will be grayed out when they cannot be used
  (i.e. enable/disable keep inventory, set difficulty, etc.)
- Hostile enemy spawns are now greyed out when the difficulty is set to peaceful
- Added a warning in chat for when global effects are unavailable
- Global effects can now always be used on the host of a singleplayer world
- Spawned entities now feature various random attributes, including random armor for armor stands
- Cursed enchantments are less likely to be applied to items awarded by Do-or-Die and lucky
  lootboxes
- Potion effects now render as a timed effect on the Crowd Control overlay
- Repair/Damage Item effects now randomly pick from armor items as well as held items
- Damage Item now takes a fixed 20% of the item's max durability instead of 50% of its remaining
  durability
- Lootbox/Do-or-Die/etc. no longer gives unusable items on 1.19.4
- The Gravel & Ore Vein effects can now replace any non-air block instead of just stones
- "Give XYZ Potion Effect" no longer turns infinite duration effects into finite effects
- Fixed an error in the Water Bucket Clutch effect that could cause your held item to be deleted on
  Paper
- Added support for Crowd Control 2.0 (customizable effect durations, non-Twitch streamer targets,
  refactored CS, etc.)
- Added sliders to various effects (give/take health/hunger/items/etc.)
- Added more descriptions to effects
- Added a config option to hide viewer names in-game
- Added a set of config options for limiting how many items or entities can be summoned at once
- Added support for localization of the mod's messages (currently only English US is supported)
- Effect messages are now displayed to spectators and the server console
- Lots of minor bug fixes and improvements

## 3.2.5

Fixed a harmless error for Paper 1.19.3.

## 3.2.4

[java-crowd-control](https://github.com/qixils/java-crowd-control/releases) has been updated to
[v3.5.1](https://github.com/qixils/java-crowd-control/releases/tag/v3.5.1) which introduces support
for Unicode in viewer usernames.

## 3.2.3

- UUIDs listed in the `hosts` config option now count as Crowd Control administrators
- A new config option called `admin-required` has been added which restricts usage of the `/account`
  command to CC admins (defaults to `false`)
- All the "Summon Minecart with XYZ" effects have been fixed
- The plugin will now load even if errors are encountered while registering effects
- [java-crowd-control](https://github.com/qixils/java-crowd-control/releases) has been updated to
  [v3.4.0](https://github.com/qixils/java-crowd-control/releases/tag/v3.4.0) which accounts for
  breaking changes introduced by an upcoming Crowd Control update
- Fixed a harmless error for Paper 1.19.0

## 3.2.2

Added support for Paper 1.18.2.

## 3.2.1

The `/account` command is no longer case-sensitive.

## 3.2.0

- Created a Sponge 7 implementation for SpongeForge 1.12.2 and SpongeVanilla 1.12.2. Play Pixelmon
  while your chat spawns dragons and creepers on you!
- Enchantment commands now apply enchantments higher than the maximum level if the held item already
  has the maximum enchantment level.
- Enchantment commands now apply to items in the off-hand and in armor slots.
- Certain enchantments could not be applied to certain items; this has been fixed.
- Potion effect commands now increase the level of existing effects and set the duration to at least
  20 seconds.
- Fixed the Take Item commands erroneously returning `SUCCESS` responses when it changed nothing.
- Created a new Thunderstorm command to start a thunderstorm.
- Downfall command no longer has a 50% chance of starting a thunderstorm.
- Reset Experience Progress has been renamed to Reset Experience and now sets the target's XP levels
  to zero instead of clearing their progress since the last level-up. This matches the current price
  and description of the perk in Crowd Control.
- Damage Item and Repair Item will now only be marked as successful if the item's damage was
  actually altered.
- Damage Item now has a minimum value of 1% durability (or 15 usages).
- Enchantments on items in the Lootbox can now rarely be higher than the usual maximum.
- The Render Toasts command has been renamed to Annoying Pop-Ups and now opens an obnoxious animated
  inventory.
- Duration of freeze commands have been increased.
- Do-or-Die now grants the streamer a reward for completing their task which generates a random item
  similarly to the Open Lootbox command but which a much higher likelihood of receiving a "good"
  item depending on the difficulty of the completed task.
- The Chorus Fruit command no longer teleports players into blocks.
- Added a new effect: Teleport to a Nearby Structure
- Added a new effect: Teleport to a Random Biome
- Added a new effect: Open Lucky Lootbox
- Added new effects for setting the time to day or night.
- Added new effects for giving and taking items.
- Items from lootboxes now have less spammy descriptions.
- Added some secret new effects for a secret new feature :)
- Automatic detection and resolution of soft-locks (i.e. death loops) has been implemented. When the
  plugin detects that you are in a death loop, it will kill nearby hostile mobs and delete dangerous
  blocks.

## 3.1.3

Water bucket clutch command is now disabled in the nether.

## 3.1.2

The plugin's port can now be changed in the config file.

## 3.1.1

Users on certain bizarre servers were unable to run `/account` due to the plugin being unable to
obtain the user's unique ID. A workaround was added to fix this issue.

## 3.1.0

- Added support for Minecraft 1.18+.
- Removed the unusable "Remove Lightning Bolt" command.
- Added the "Place Bedrock" command.
- Added the "Give Elytra" and "Take Elytra" commands.
- Added default prices and descriptions to every effect.
- Allowed the "Dig Command" to dig larger holes and break any block.
- Prevent the enchantment commands from adding illegal enchantments to items.
- Only allow one End Portal Frame to be distributed or taken at a time via the
  "Give End Portal Frame" command.
- The duration of potion effect commands are now displayed in chat.
