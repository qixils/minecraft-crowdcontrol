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