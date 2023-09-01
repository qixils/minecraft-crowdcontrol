## Fabric 1.19.4 Manual Installation

The following steps detail how to manually set up a Minecraft Fabric 1.19.4 server
with Crowd Control 2.0.

### Installing a local server

This section walks you through installing a Fabric server to run on your local computer. This
section may be skipped when using a remote server hosting service.

1. Download and run the [Fabric Loader installer](https://fabricmc.net/use/installer/).
    - Users of platforms other than Windows may first need to download and install
      [Java 17](https://adoptium.net/temurin/releases/?version=17).
2. In the installer window, click the `Server` tab and then the triple dots next to the Install
   Location to open the directory selector. Find or create a new, empty folder and click Open, then
   click OK. This folder will be important in later steps so don't lose it. From here on, this
   folder will be referred to as the "root folder".
3. Ensure the Minecraft version is set to 1.19.4 and click the `Install` button to create the Fabric
   server. After the setup is complete, click `Download server jar` and `Generate` when prompted.
4. Close the installer.

### Installing the mod

1. Navigate to where you installed the server and create a new folder called `mods`.
2. Download the latest build of the
   [Fabric API](https://modrinth.com/mod/fabric-api/versions?g=1.19.4&c=release)
   and place it in the `mods` folder.
3. Download the latest build of
   [Crowd Control for Fabric](https://modrinth.com/mod/crowdcontrol/versions?l=fabric&g=1.19.4)
   and place it in the `mods` folder.
4. (Optional) Copy all the other Fabric 1.19.4 mods that you want to play with into the `mods`
   folder.
5. Run the Minecraft server using the `start.bat` file on Windows or `start.sh` on Linux to
   initialize the plugin's configuration files. You will have to run it twice, as the first will
   prompt you to accept Minecraft's End User License Agreement.
6. To change the plugin's configuration file, you must first shut down the server by typing `stop`
   in the server window. The config file is located at `<root>/config/crowdcontrol.conf`.
7. Ensure the ports 25565 and 58431 are open so that users may connect to the Minecraft server and
   the Crowd Control server.

Users may now connect using the [**Joining a Server**](fabric_joining_a_server.md) guide. Make
sure to provide your public IP address and the password used in the config file to your streamers.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command. This
prevents unknown players from joining the server and potentially griefing your builds.
