## SpongeForge 8 Client Installation

The following steps detail how to install the Crowd Control mod in the vanilla Minecraft launcher
for SpongeForge 1.16.5.

1. Download and install the latest build (36.2.39)
   of [Forge 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html).
    - You may first need to download and install [Java](https://adoptium.net/temurin/releases/).
2. Navigate to your Minecraft installation folder.
    - On Windows, this can be accessed by Pressing `Windows Key + R` and typing
      `%AppData%\.minecraft`.
3. Create a new folder called `mods`.
4. Download the latest build of
   [Sponge 8](https://spongepowered.org/downloads/spongeforge?minecraft=1.16.5&offset=0)
   and place it in the `mods` folder.
5. Create a new folder inside the `mods` folder called `plugins`.
6. Download the latest build of
   [Crowd Control for Sponge 8](https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=1.16.5)
   and place it in the `plugins` folder.
7. (Optional) Copy all the other Forge 1.16.5 mods or Sponge 8 plugins that you want to play with,
   such as [my mod that emulates 1.16.1 speedrunning](https://modrinth.com/mod/depiglining/versions?g=1.16.5&l=forge),
   into the `mods` folder and the `plugins` folder respectively. Please be sure to read
   [this section of the troubleshooting guide](sponge_8_troubleshooting.md#incompatible-mods)
   to ensure you do not install any incompatible mods.

The mod is now installed! Open your Minecraft Launcher and select the `forge` profile to play. To
use Crowd Control on a remote server, see the [**Joining a Server**](sponge_8_joining_a_server.md)
guide. Else, to use Crowd Control on a single player world:

1. Download and install the [Crowd Control app](https://crowdcontrol.live/).
2. In the **Game Library** tab, select **Minecraft**.
3. Select **Configure Minecraft**.
4. Enter your Minecraft username and click next.
5. Select **Sponge 8** and click next.
6. Select **Remote**.
7. Enter in `localhost` as the host and click next.
8. Accept the default password by clicking next.
9. Launch your modded instance of Minecraft 1.16.5 and open the world you want to play on.
10. In the app, if you see a **Connector Error** button, click on it to refresh the connection to
    the game.
11. Select **Start Session** in the Crowd Control app.

If you experience issues at any point, please reach out for help on our
[Discord](https://discord.gg/warpworld).
