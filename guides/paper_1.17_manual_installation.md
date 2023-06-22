## Paper 1.17 Manual Installation

The following steps detail how to manually set up a Minecraft 1.17 server with Crowd Control.

1. Download and
   install [the latest version of Java](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot).
   Java 16 or later is required.
2. Download the latest version of [Paper 1.17.1](https://papermc.io/downloads/all). Spigot is not
   supported.
3. Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the
   "root folder."
4. Create a new folder named `plugins` inside the root folder.
5. Download
   the [latest Paper plugin jar](https://modrinth.com/plugin/crowdcontrol/versions?l=paper&g=1.17)
   and place it in the `plugins` folder.
6. Run the Minecraft server to initialize the plugin's configuration files. You will have to run it
   twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing
   `eula.txt` in a text editing program like Notepad. To run the server:
    - On Windows, hold shift and right click inside the root folder. From the context menu, select "
      Open command window here" or "Open PowerShell here".
    - After opening your command window, run the command `java -Xmx2G -Xms2G -jar paper.jar nogui` (
      by typing that in and pressing enter)
    - When running for the first time you will have to edit the file `eula.txt` in a program like
      Notepad or `nano` and then run the command again.
7. Once you shut down the server using `/stop`, you can edit the plugin's configuration file. A
   password must be set to use the plugin.
    - The `config.yml` file can be found inside the `<root>/plugins/CrowdControl` folder.
    - Be sure to enter a password into the `password` field.
8. Ensure the ports 25565 and 58431 are open so that users may connect to the Minecraft server and
   its Crowd Control server.
9. Pre-generating chunks using a plugin
   like [Chunky](https://www.spigotmc.org/resources/chunky.81534/) is recommended for optimal
   performance though not required.

Users may now connect using the [**Joining a Server**](paper_joining_a_server.md) guide. Make sure to
provide your public IP address and the password used in the config file to your streamers.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command. This
prevents unknown players from joining the server and potentially griefing your builds.