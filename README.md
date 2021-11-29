# Minecraft Crowd Control
Minecraft Crowd Control is a plugin for [Paper](https://papermc.io/) 1.17.1 that allows your Twitch community to interact with your game.

This version of the plugin is to be used with [crowdcontrol.live](https://crowdcontrol.live) which allows viewers to control your game via bits or channel points.

Commands are fully compatible with multiplayer servers. Everyone online will be affected to some extent.

## Manual Installation

If you are hosting a server for streamers to join, you may use the following steps to setup a Minecraft server running with Crowd Control.

1. Download and install [Java 17](https://adoptium.net/?variant=openjdk17&jvmVariant=hotspot).
2. Download the latest version of [Paper](https://papermc.io/downloads) 1.17.1. Spigot is not supported.
3. Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the "root folder."
4. Create a new folder named `plugins` inside the root folder.
5. Download the [latest plugin jar](https://github.com/qixils/minecraft-crowdcontrol/releases/latest) and place it into the `plugins` folder.
6. Run the Minecraft server. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing `eula.txt`. To run the server:
    - On Windows, hold shift and right click inside the root folder. From the context menu, select "Open command window here" or "Open PowerShell here".
    - After opening your command window, run the command `java -Xmx2G -Xms2G -jar paper.jar nogui` (by typing that in and pressing enter)
    - When running for the first time you will have to edit the file `eula.txt` in a program like Notepad or `nano` and then run the command again.
7. A password must be set to run the server in Server Mode, allowing multiple streamers to connect.
    - The `config.yml` file can be found inside the `<root>/plugins/CrowdControl` folder.
    - Enter a password into the `password` field
8. Ensure the ports 25565 and 58431 so that users may connect to the Minecraft server and its Crowd Control server.
9. Pre-generating chunks using a plugin like [Chunkmaster](https://www.spigotmc.org/resources/chunkmaster.71351/) is recommended for optimal performance.

Users may now connect using the steps below ("Streamer Setup (External Server)"). Make sure to provide your public IP address and the password used in the config file.

For extra security, consider enabling a user whitelist using the vanilla `/whitelist` command.

## Streamer Setup (External Server)

The following steps are for streamers who are connecting to another individual's server.

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the **Game Selection** tab, select **Minecraft (Server) (PC)**.
3. Click **Start**. A pop-up menu will appear, prompting you for information that you should have received from your server administrator.
    - In the host field, you should enter an IP address or a website URL. This will usually be the same as the IP address that you connect to in Minecraft.
    - In the password field, you must enter a password provided to you by the server administrator.
4. Upon joining the Minecraft server, run the command `/account link <your twitch username>` to ensure you receive effects that viewers purchase for you.

## Streamer Setup (Local Server)

The following steps are for streamers who are playing alone.

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the Game Selection tab, select **Minecraft (Server) (PC)**.
3. Click **One Click Server Setup** to automatically setup a server. This may take several minutes to complete.
4. Once done, click **Launch Server** to start the Minecraft server. If you were not already prompted to do so during setup, you may need to set a temporary password to be able to connect to the server. This can be done by typing `password <insert password here>` in the pop-up server console window.
5. Once the server has started, click **Start** in the Crowd Control app to connect to the server, and join the Minecraft server.
6. Upon joining the Minecraft server, run the command `/account link <your twitch username>` to ensure you receive effects that viewers purchase for you.
