## Sponge 8: Troubleshooting

### "Failed to load a valid ResourcePackInfo"

This warning is thrown by Sponge when running in the client but is totally safe to ignore.

### "CrowdControl is not a valid mod file"

If you see this warning on startup but *not* the ResourcePackInfo warning described above, it means
that you do not have
[SpongeForge](https://spongepowered.org/downloads/spongeforge?minecraft=1.16.5&offset=0)
installed. You must download it and add it to your `mods` folder.

### Incompatible Mods

Unfortunately, many modpacks bundle mods that are incompatible with SpongeForge, and thus
incompatible with Crowd Control. Many of these incompatible mods are unnecessary or have known fixes
that have yet to be added to the modpack. Remedies for some of these mods can be found below.

#### Unnecessary Mods

The following incompatible mods provide little functionality or provide functionality that is
already provided by Sponge. **If you have any of these in your modpack, you should delete them.**

- LazyDFU (Performance mod that is bundled in Sponge)
- Observable (Profiling mod, not useful to 99.9% of players)

#### Outdated Mods

The following incompatible mods have known fixes that haven't been widely published. **If you have
one of these mods in your modpack, you should delete it and replace it with the fixed build.**

- Abnormals Core: [Fixed](https://cdn.discordapp.com/attachments/406987481825804290/949798054117122058/abnormals_core-1.16.5-3.3.1.jar) by Sponge developers ([source](https://github.com/team-abnormals/blueprint/commit/df4932960266f2e30a541097811193c17d1bb339))

#### Mods with No Known Fix

The following mods have not had a fix developed for them yet or are closed source and thus unlikely
to ever have a fix developed. You will not be able to play Crowd Control with these mods.

- Quests Additions (Closed source)
