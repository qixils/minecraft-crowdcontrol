## SpongeForge 8 Client Installation

The following steps detail how to manually install Crowd Control for Minecraft SpongeForge 1.16.5.

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the Game Selection tab, select **Minecraft (Sponge) 1.16 (PC)**.
3. Download and install [Java](https://adoptium.net/).
4. Download and install the latest build (36.2.39)
   of [Forge 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html).
5. Navigate to your Minecraft installation folder.
    - On Windows, this can be accessed by Pressing `Windows Key + R` and typing
      `%AppData%\.minecraft`.
6. Create a new folder called `mods`.
7. Download the latest build of
   [Sponge 8](https://spongepowered.org/downloads/spongeforge?minecraft=1.16.5&offset=0)
   and place it in the `mods` folder.
8. Create a new folder inside the `mods` folder called `plugins`.
9. Download the latest build of
   [Crowd Control for Sponge 8](https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=1.16.5)
   and place it in the `plugins` folder.
10. Copy all the other Forge 1.16.5 mods that you want to play with into the `mods` folder.

The mod is now installed! Open your Minecraft Launcher and select the `forge` profile to play.
To use Crowd Control on a single player world:

1. Open the world you want to play on.
2. Click **Start Session** in the Crowd Control app.
3. Enter `localhost` as the host and `crowdcontrol` as the password.
4. In Minecraft, run the command `/account link <your twitch username>` to ensure you receive
   effects that viewers purchase for you. Example: `/account link jaku`
5. If you experience rubber-banding/laggy movement:
   - Navigate to `%AppData%\.minecraft\config\sponge`
   - Open `global.conf` and set `moved-wrongly` to `false`
   - Open `sponge.conf` and set `movement-checks` to `true`
   - Restart your game

To use Crowd Control on an external server, see the
[**Joining a Server**](sponge_8_joining_a_server.md) guide.
