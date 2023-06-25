# Minecraft Crowd Control [![Release](https://img.shields.io/github/v/release/qixils/minecraft-crowdcontrol?label=Release&logo=github)](https://github.com/qixils/minecraft-crowdcontrol/releases/latest) [![Release](https://img.shields.io/github/v/release/qixils/minecraft-crowdcontrol?include_prereleases&label=Pre-Release&logo=github&color=orange)](https://github.com/qixils/minecraft-crowdcontrol/releases)

Minecraft Crowd Control is a Minecraft mod for various platforms that allows your livestream
viewers to interact with your game using tips, bits, channel points, donations to charity, and more.

Beginning with v2, this plugin has been developed for use
with [crowdcontrol.live](https://crowdcontrol.live). This service handles the integration with
Twitch (and other platforms) and is what your viewers will interact with to control your game.

This mod is fully compatible with multiplayer servers. Players on the server will be able to
receive effects targeted at them using **/account link \<username\>**.  
For example, if you are doing a Crowd Control stream under the Twitch username **minecraftfan1992**,
then you would run the in-game command **/account link minecraftfan1992** to receive the effects
that your viewers are buying for you.

## Installation

> **Note**  
> The following information is for the
> [Crowd Control 2.0 beta](https://beta.crowdcontrol.live/).
> **If you are using [Crowd Control "1.0"](https://crowdcontrol.live/) then please see
> [this document](https://github.com/qixils/minecraft-crowdcontrol/tree/legacy#installation)
> instead.**

Installation steps differ depending on the Minecraft version you wish to use, whether you want to
use client mods, and the type of installation you wish to perform. Most casual users of the software
will be interested in the guides listed under **Automatic Setup**. If you require a dedicated server
for other streamers to join, then follow the steps for **Dedicated Server** and send your players
the guide for **Joining an External Server**.

Not sure which mod loader to choose between Fabric and Paper? If you're running a server for a large
multi-streamer event, then the highly-performant **Paper** is likely the way to go. Otherwise, we
would recommend **Fabric** as it has the most features and is the easiest to use.

| Game Version  |                      Automatic Setup                      |                    Joining an External Server                     |                                Dedicated Server                                 |                                Client Installation                                |
|:-------------:|:---------------------------------------------------------:|:-----------------------------------------------------------------:|:-------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------:|
| Forge 1.12.2  | [Sponge 7: One-Click Setup](guides/sponge_7_one_click.md) | [Sponge 7: Joining a Server](guides/sponge_7_joining_a_server.md) |  [SpongeForge 7: Manual Installation](guides/sponge_7_manual_installation.md)   |                                  Not supported*                                   |
| Forge 1.16.5  | [Sponge 8: One-Click Setup](guides/sponge_8_one_click.md) | [Sponge 8: Joining a Server](guides/sponge_8_joining_a_server.md) |  [SpongeForge 8: Manual Installation](guides/sponge_8_manual_installation.md)   |      [Sponge 8: Client Installation](guides/sponge_8_client_installation.md)      |
| Fabric 1.19.2 |                    No longer supported                    |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.19: Manual Installation](guides/fabric_1.19.2_manual_installation.md) | [Fabric 1.19.2: Client Installation](guides/fabric_1.19.2_client_installation.md) |
| Fabric 1.19.4 |                    No longer supported                    |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.19: Manual Installation](guides/fabric_1.19.4_manual_installation.md) | [Fabric 1.19.4: Client Installation](guides/fabric_1.19.4_client_installation.md) |
| Paper 1.19.4  |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |   [Paper 1.19: Manual Installation](guides/paper_1.19_manual_installation.md)   |                                        N/A                                        |
| Fabric 1.20.1 |   [Fabric: One-Click Setup](guides/fabric_one_click.md)   |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.20: Manual Installation](guides/fabric_1.20.1_manual_installation.md) | [Fabric 1.20.1: Client Installation](guides/fabric_1.20.1_client_installation.md) |
| Paper 1.20.1  |    [Paper: One-Click Setup](guides/paper_one_click.md)    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |   [Paper 1.20: Manual Installation](guides/paper_1.20_manual_installation.md)   |                                        N/A                                        |

_\* Sponge has some crashing issues when running on a 1.12.2 client. Please use a server instead._

By default, all servers support Vanilla clients. This means that players connecting to your server
will **not** need to install any sort of mod or software except for the Crowd Control desktop
application (if necessary for your setup). Thanks to this, setup is much easier than traditional
Minecraft mods as the automatic setup does most of the work for you.

For some integrations, namely Fabric, installing the client mod is highly recommended as it will
grant access to several extra effects and improve the behavior of some server-side effects. You can
use the client mod in either a singleplayer or multiplayer world (provided the server also has the
mod installed).
