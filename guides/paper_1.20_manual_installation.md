## Paper 1.20 Manual Installation

The following steps detail how to manually set up a Minecraft 1.20.1 server with Crowd Control.

1. Download and install [the latest version of Java](https://adoptium.net/).
   Java 17 or later is required.
2. Download the latest version of [Paper 1.20.1](https://papermc.io/downloads/paper). Spigot is not
   supported.
3. Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the
   "root folder."
4. Create a new folder named `plugins` inside the root folder.
5. Download the
   [latest Paper plugin jar](https://modrinth.com/mod/crowdcontrol/versions?l=paper&g=1.20.1) and
   place it in the `plugins` folder.
6. (Optional) Copy any Paper plugins you want to play with into the `plugins` folder.
    - Pre-generating chunks using a plugin like
      [Chunky](https://modrinth.com/plugin/chunky/versions?g=1.20.1&l=paper) is recommended for
      optimal performance though not required.
7. Run the Minecraft server to initialize the plugin's configuration files. You will have to run it
   twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing
   `eula.txt` in a text editing program like Notepad. To run the server:
    - On Windows, hold shift and right click inside the root folder. From the context menu, select "
      Open command window here" or "Open PowerShell here".
    - After opening your command window, run the command `java -Xmx2G -Xms2G -jar paper.jar nogui` (
      by typing that in and pressing enter)
    - When running for the first time you will have to edit the file `eula.txt` in a program like
      Notepad or `nano` and then run the command again.
8. (Optional) Once you shut down the server using `/stop`, you can edit the plugin's configuration
   file. The `config.yml` file can be found inside the `<root>/plugins/CrowdControl` folder.
9. Ensure the ports 25565 and 58431 are open so that users may connect to the Minecraft server and
   its Crowd Control server.

Users may now connect using the [**Joining a Server**](paper_joining_a_server.md) guide. Make sure
to provide your public IP address and the password used in the config file (default: `crowdcontrol`)
to your streamers.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command. This
prevents unknown players from joining the server and potentially griefing your builds.

You may also be interested in setting up [GeyserMC](https://geysermc.org/) to allow Bedrock edition
users (i.e. console players) to play.