# Changelog

A list of all changes made to the software in reverse chronological order.

## 3.2.0

- Created a Sponge 7 implementation for SpongeForge 1.12.2 and SpongeVanilla 1.12.2. Play Pixelmon
  while your chat spawns dragons and creepers on you!
- Enchantment commands now apply enchantments higher than the maximum level if the held item already
  has the maximum enchantment level.
- Potion effect commands now increase the level of existing effects and set the duration to at least
  15 seconds.
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