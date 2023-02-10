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

Installation steps differ depending on the Minecraft version you wish to use, whether you want to
use client mods, and the type of installation you wish to perform. Most casual users of the software
will be interested in the guides listed under **Automatic Local Server Setup**. If this fails to
work or you require a dedicated server for other streamers to join, then follow the steps for
**Dedicated Server** and **Joining an External Server**. If you are playing with client mods, then
follow the steps for **Client Installation**.

| MC Version / Mod Loader |               Automatic Local Server Setup                |                    Joining an External Server                     |                                 Dedicated Server                                  |                              Client Installation                              |
|:-----------------------:|:---------------------------------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------:|
|     1.12.2 / Forge      | [Sponge 7: One-Click Setup](guides/sponge_7_one_click.md) | [Sponge 7: Joining a Server](guides/sponge_7_joining_a_server.md) | [SpongeForge 7: Manual Installation](guides/spongeforge_7_manual_installation.md) |    [Sponge 7: Client Installation](guides/sponge_7_client_installation.md)    |
|     1.16.5 / Forge      | [Sponge 8: One-Click Setup](guides/sponge_8_one_click.md) | [Sponge 8: Joining a Server](guides/sponge_8_joining_a_server.md) | [SpongeForge 8: Manual Installation](guides/spongeforge_8_manual_installation.md) |    [Sponge 8: Client Installation](guides/sponge_8_client_installation.md)    |
|     1.17.1* / Paper     |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.17: Manual Installation](guides/paper_1.17_manual_installation.md)    |                                      N/A                                      |
|     1.18.2* / Paper     |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.18: Manual Installation](guides/paper_1.18_manual_installation.md)    |                                      N/A                                      |
|     1.19.3 / Fabric     |   [Fabric: One-Click Setup](guides/fabric_one_click.md)   |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   |   [Fabric 1.19: Manual Installation](guides/fabric_1.19_manual_installation.md)   | [Fabric 1.19: Client Installation](guides/fabric_1.19_client_installation.md) |
|     1.19.3 / Paper      |    [Paper: One-Click Setup](guides/paper_one_click.md)    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |    [Paper 1.19: Manual Installation](guides/paper_1.19_manual_installation.md)    |                                      N/A                                      |

_* No longer officially supported. Please use the latest version of Minecraft._

By default, all servers support Vanilla clients. This means that players connecting to your server
will **not** need to install any sort of mod or software except for the Crowd Control desktop
application (if necessary for your setup). Thanks to this, setup is much easier than traditional
Minecraft mods as the automatic setup does most of the work for you.

For some integrations, namely Fabric, installing the client mod is highly recommended as it will
grant access to several extra effects and improve the behavior of some server-side effects. You can
use the client mod in either a singleplayer or multiplayer world (provided the server also has the
mod installed).
