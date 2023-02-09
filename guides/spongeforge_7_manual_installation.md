## SpongeForge 1.12.2 Manual Installation

The following steps detail how to manually set up a Minecraft Forge 1.12.2 server
with Crowd Control.

1. Download and install [Java 8](https://adoptium.net/?variant=openjdk8&jvmVariant=hotspot).
2. Download and run the latest build (14.23.5.2860)
   of [Forge 1.12.2](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html).
3. In the Forge pop-up window, click `Install server` and then the triple dots in the corner to open
   the directory selector. Find or create a new, empty folder and click Open, then click OK. This
   folder will be important in later steps and will hereafter be referred to as the "root folder".
4. Navigate to the folder you created in your file explorer and create a new folder inside it
   called `mods`.
5. Download the latest recommended build of
   [SpongeForge 1.12.2](https://www.spongepowered.org/downloads/spongeforge?minecraft=1.12.2&offset=0)
   for API v7 and place it in the `mods` folder.
6. Download the
   [latest build of Crowd Control for Sponge 7](https://github.com/qixils/minecraft-crowdcontrol/releases/latest)
   and place it in the `mods` folder.
7. Copy all the other Forge 1.12.2 mods or Sponge 7 plugins that you want to play with into the
   `mods` folder.
8. Run the Minecraft server to initialize the plugin's configuration files. You will have to run it
   twice, as the first will prompt you to accept Minecraft's End User License Agreement.
   To run the server:
    - On Windows, hold shift and right click inside the root folder. From the context menu, select
      "Open command window here" or "Open PowerShell here".
    - After opening your command window, run the
      command `java -Xmx2G -Xms2G -jar forge-1.12.2-14.23.5.2860.jar nogui` (by typing that in and
      pressing enter). You can adjust the gigabytes of RAM used by the software by altering the `2G`
      text, i.e. `-Xmx4G -Xms4G` would allocate 4 gigabytes of RAM to the game. This may be
      necessary if you are playing with large mods.
    - When running for the first time, you will have to edit the file `eula.txt` in a program like
      Notepad or `nano` and then run the server command again.
9. Once you shut down the server using `/stop`, you can edit the plugin's configuration file. A
   password must be set to use the plugin.
    - The config file is located at `<root>/config/crowd-control.conf`.
    - Be sure to enter a password into the `password` field.
10. Ensure the ports 25565 and 58431 are open so that users may connect to the Minecraft server and
    its Crowd Control server.

Users may now connect using the [**Joining a Server**](sponge_7_joining_a_server.md) guide. Make
sure to provide your public IP address and the password used in the config file to your streamers.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command. This
prevents unknown players from joining the server and potentially griefing your builds.
