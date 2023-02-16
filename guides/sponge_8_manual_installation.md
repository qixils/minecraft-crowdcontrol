## SpongeForge 1.16.5 Manual Installation

The following steps detail how to manually set up a Minecraft Forge 1.16.5 server
with Crowd Control.

1. Download and install [Java 8](https://adoptium.net/temurin/releases/?version=8).
2. Download and run the latest build (36.2.39)
   of [Forge 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html).
3. In the Forge pop-up window, click `Install server` and then the triple dots in the corner to open
   the directory selector. Find or create a new, empty folder and click Open, then click OK. This
   folder will be important in later steps and will hereafter be referred to as the "root folder".
4. Navigate to the folder you created in your file explorer and create a new folder inside it
   called `mods`.
5. Download the latest recommended build of
   [SpongeForge 1.16.5](https://www.spongepowered.org/downloads/spongeforge?minecraft=1.16.5&offset=0)
   for API v8 and place it in the `mods` folder.
6. Create a new `plugins` folder inside the `mods` folder.
7. Download the
   [latest build of Crowd Control for Sponge 8](https://github.com/qixils/minecraft-crowdcontrol/releases/latest)
   and place it in the `plugins` folder.
8. Copy all the other Forge 1.16.5 mods or Sponge 8 plugins that you want to play with into the
   `mods` folder and the `plugins` folder respectively.
9. Run the Minecraft server to initialize the plugin's configuration files. You will have to run it
   twice, as the first will prompt you to accept Minecraft's End User License Agreement.
   To run the server:
    - On Windows, hold shift and right click inside the root folder. From the context menu, select
      "Open command window here" or "Open PowerShell here".
    - After opening your command window, run the
      command `java -Xmx2G -Xms2G -jar forge-1.16.5-36.2.39.jar nogui` (by typing that in and
      pressing enter). You can adjust the gigabytes of RAM used by the software by altering the `2G`
      text, i.e. `-Xmx4G -Xms4G` would allocate 4 gigabytes of RAM to the game. This may be
      necessary if you are playing with large mods.
    - When running for the first time, you will have to edit the file `eula.txt` in a program like
      Notepad or `nano` and then run the server command again.
10. Once you shut down the server using `/stop`, you can edit the plugin's configuration file. A
    password must be set to use the plugin.
    - The config file is located at `<root>/config/crowd-control.conf`.
    - Be sure to enter a password into the `password` field.
11. Ensure the ports 25565 and 58431 are open so that users may connect to the Minecraft server and
    its Crowd Control server.

Users may now connect using the [**Joining a Server**](sponge_8_joining_a_server.md) guide. Make
sure to provide your public IP address and the password used in the config file to your streamers.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command. This
prevents unknown players from joining the server and potentially griefing your builds.
