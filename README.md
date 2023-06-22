# Minecraft Crowd Control

Minecraft Crowd Control is a Minecraft plugin for various platforms that allows your Twitch
community to interact with your game using bits, channel points, donations to charity, and more.

Beginning with v2, this plugin has been developed for use
with [crowdcontrol.live](https://crowdcontrol.live). This service handles the integration with
Twitch and is what your viewers will interact with to control your game.

This plugin is fully compatible with multiplayer servers. Players on the server will be able to
receive effects targeted at them using **/account link \<username\>**.  
For example, if you are doing a Crowd Control stream under the Twitch username
**minecrafter20211130**, then you would run the in-game command **/account link
minecrafter20211130** to receive the effects that your viewers are buying for you.

## Installation

The following information is for the latest full release of Crowd Control.
**If you are using the Crowd Control 2.0 beta, please see
[this document](https://github.com/qixils/minecraft-crowdcontrol/#installation) instead.**

Installation steps differ depending on the Minecraft version you wish to use, whether you want to
use Forge mods, and the type of installation you wish to perform. Most casual users of the software
will be interested in the guides listed under **Automatic Local Server Setup**. If this fails to
work or you require a dedicated server for other streamers to join, then follow the steps for
**Dedicated Server** and **Joining an External Server**.

| MC Version / Mod Loader |               Automatic Local Server Setup                |                    Joining an External Server                     |                                 Dedicated Server                                  |
|:-----------------------:|:---------------------------------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------------------------------------------:|
|  1.12 / Sponge (Forge)  | [Sponge 7: One-Click Setup](guides/sponge_7_one_click.md) | [Sponge 7: Joining a Server](guides/sponge_7_joining_a_server.md) | [SpongeForge 7: Manual Installation](guides/spongeforge_7_manual_installation.md) |
|  1.16 / Sponge (Forge)  |                     Upgrade to CC 2.0                     |                         Upgrade to CC 2.0                         |                                 Upgrade to CC 2.0                                 |
|      1.17 / Paper       |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.17: Manual Installation](guides/paper_1.17_manual_installation.md)    |
|      1.18 / Paper       |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.18: Manual Installation](guides/paper_1.18_manual_installation.md)    |
|      1.19 / Paper       |    [Paper: One-Click Setup](guides/paper_one_click.md)    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.19: Manual Installation](guides/paper_1.19_manual_installation.md)    |
|      1.20 / Paper       |                     Upgrade to CC 2.0                     |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.20: Manual Installation](guides/paper_1.20_manual_installation.md)    |
|         Fabric          |                     Upgrade to CC 2.0                     |                         Upgrade to CC 2.0                         |                                 Upgrade to CC 2.0                                 |

By default, all servers support Vanilla clients. This means that players connecting to your server
will **not** need to install any sort of mod or software except for the Crowd Control desktop
application (if necessary for your setup). Thanks to this, setup is much easier than traditional
Minecraft mods as the automatic setup does most of the work for you.
