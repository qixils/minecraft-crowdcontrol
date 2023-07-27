<h1 align="center">Minecraft Crowd Control
  <br>
  <a href="https://github.com/qixils/minecraft-crowdcontrol/releases/latest">
    <img src="https://img.shields.io/github/v/release/qixils/minecraft-crowdcontrol?label=release&amp;logo=github" alt="Release">
  </a>
  <a href="https://github.com/qixils/minecraft-crowdcontrol/releases">
    <img src="https://img.shields.io/github/v/release/qixils/minecraft-crowdcontrol?include_prereleases&amp;label=pre-release&amp;logo=github&amp;color=orange" alt="Pre-release">
  </a>
  <br>
  <a href="https://github.com/qixils/minecraft-crowdcontrol/releases">
    <img src="https://img.shields.io/github/downloads/qixils/minecraft-crowdcontrol/total?logo=github&amp;color=brightgreen" alt="GitHub">
  </a>
  <a href="https://modrinth.com/plugin/crowdcontrol">
    <img src="https://img.shields.io/modrinth/dt/6XhH9LqD?logo=modrinth&amp;color=brightgreen" alt="Modrinth">
  </a>
  <a href="https://www.curseforge.com/minecraft/mc-mods/crowdcontrol">
    <img src="https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.cfwidget.com%2F830331&amp;query=%24.downloads.total&amp;logo=curseforge&amp;label=downloads&amp;color=brightgreen" alt="CurseForge">
  </a>
</h1>

[![Crowd Control banner](https://i.qixils.dev/cc-banner.png)](https://crowdcontrol.live)

Minecraft Crowd Control is a Minecraft mod for various platforms that allows your livestream
viewers to interact with your game using tips, bits, channel points, donations to charity, and more.
It can run as a client mod or as a server plugin.

Beginning with v2, this mod has been developed for use with
[crowdcontrol.live](https://crowdcontrol.live).
This service handles the integration with Twitch, YouTube, TikTok, Discord, etc. and is what your
viewers will interact with to control your game.

## Installation

> **Note**  
> The following information is for the
> [Crowd Control 2.0 beta](https://beta.crowdcontrol.live/).
> **If you are using [Crowd Control "1.0"](https://crowdcontrol.live/) then please see
> [this document](https://github.com/qixils/minecraft-crowdcontrol/tree/legacy#installation)
> instead.**

Installation steps differ depending on the Minecraft version you wish to use, whether you want to
use client mods, and the type of installation you wish to perform. Most casual users of the software
will be interested in just the [Paper: One-Click Setup](guides/paper_one_click.md) guide. Users
familiar with Minecraft modding may instead want to follow the
[Fabric: Client Installation](guides/fabric_1.20.1_client_installation.md) guide for extra effects.
Speedrunners and modpack players should follow the corresponding **Client Installation** or
**Automatic Setup** guide for their game version. If you require a dedicated server for other
streamers to join, then follow the guide for setting up a **Dedicated Server** and send your players
the guide for **Joining a Server**.

| Game Version  |                      Automatic Setup                      |                         Joining a Server                          |                                 Dedicated Server                                  |                                Client Installation                                |
|:-------------:|:---------------------------------------------------------:|:-----------------------------------------------------------------:|:---------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------:|
| Forge 1.12.2  | [Sponge 7: One-Click Setup](guides/sponge_7_one_click.md) | [Sponge 7: Joining a Server](guides/sponge_7_joining_a_server.md) |   [SpongeForge 7: Manual Installation](guides/sponge_7_manual_installation.md)    |                                  Not supported*                                   |
| Forge 1.16.5  | [Sponge 8: One-Click Setup](guides/sponge_8_one_click.md) | [Sponge 8: Joining a Server](guides/sponge_8_joining_a_server.md) |   [SpongeForge 8: Manual Installation](guides/sponge_8_manual_installation.md)    |      [Sponge 8: Client Installation](guides/sponge_8_client_installation.md)      |
| Fabric 1.19.2 |                    No longer supported                    |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.19.2: Manual Installation](guides/fabric_1.19.2_manual_installation.md) | [Fabric 1.19.2: Client Installation](guides/fabric_1.19.2_client_installation.md) |
| Fabric 1.19.4 |                    No longer supported                    |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.19.4: Manual Installation](guides/fabric_1.19.4_manual_installation.md) | [Fabric 1.19.4: Client Installation](guides/fabric_1.19.4_client_installation.md) |
| Paper 1.19.4  |                    No longer supported                    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |   [Paper 1.19.4: Manual Installation](guides/paper_1.19_manual_installation.md)   |                                        N/A                                        |
| Fabric 1.20.1 |   [Fabric: One-Click Setup](guides/fabric_one_click.md)   |   [Fabric: Joining a Server](guides/fabric_joining_a_server.md)   | [Fabric 1.20.1: Manual Installation](guides/fabric_1.20.1_manual_installation.md) | [Fabric 1.20.1: Client Installation](guides/fabric_1.20.1_client_installation.md) |
| Paper 1.20.1  |    [Paper: One-Click Setup](guides/paper_one_click.md)    |    [Paper: Joining a Server](guides/paper_joining_a_server.md)    |   [Paper 1.20.1: Manual Installation](guides/paper_1.20_manual_installation.md)   |                                        N/A                                        |

_\* Sponge has some crashing issues when running on a 1.12.2 client. Please use a server instead._

By default, all servers support Vanilla clients. This means that players connecting to your server
will **not** need to install any sort of mod or software except for the Crowd Control desktop
application (if necessary for your setup). Thanks to this, setup is much easier than traditional
Minecraft mods as the automatic setup does most of the work for you.

For some integrations, namely Fabric, installing the client mod is highly recommended as it will
grant access to several extra effects and improve the behavior of some server-side effects. You can
use the client mod in either a singleplayer or multiplayer world (provided the server also has the
mod installed).
