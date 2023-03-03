## SpongeForge 7 Client Installation

The following steps detail how to manually install Crowd Control for Minecraft SpongeForge 1.12.2.

> **Warning**  
> Client installation is not recommended at this time as Sponge 7 has some issues that cause
> crashing when run on the client. For this reason, we instead recommend using a local server with
> all of your mods copied to it. The easiest way to do this is through the
> [One-Click Server Setup](sponge_7_one_click.md), although you may also set up a server
> [manually](sponge_7_manual_installation.md).

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the Game Selection tab, select **Minecraft (Sponge) 1.12 (PC)**.
3. Download and install [Java](https://adoptium.net/).
4. Download and install the latest build (14.23.5.2860)
   of [Forge 1.12.2](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html).
5. Navigate to your Minecraft installation folder.
    - On Windows, this can be accessed by Pressing `Windows Key + R` and typing
      `%AppData%\.minecraft`.
6. Create a new folder called `mods`.
7. Download the latest build of
   [Sponge 7](https://spongepowered.org/downloads/spongeforge?minecraft=1.12.2&offset=0)
   and place it in the `mods` folder.
8. Download the latest build of
   [Crowd Control for Sponge 7](https://github.com/qixils/minecraft-crowdcontrol/releases/latest)
   and place it in the `mods` folder.
9. Copy all the other Forge 1.12.2 mods that you want to play with into the `mods` folder.

The mod is now installed! Open your Minecraft Launcher and select the `forge` profile to play.
To use Crowd Control on a single player world:

1. Open the world you want to play on.
2. Click **Start Session** in the Crowd Control app.
3. Enter `localhost` as the host and `crowdcontrol` as the password.
4. In Minecraft, run the command `/account link <your twitch username>` to ensure you receive
   effects that viewers purchase for you. Example: `/account link jaku`

To use Crowd Control on an external server, see the
[**Joining a Server**](sponge_7_joining_a_server.md) guide.
