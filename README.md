# Minecraft Crowd Control
Minecraft Crowd Control is a plugin for [Paper](https://papermc.io/) 1.17.1 that allows your Twitch community to interact with your game.

This version of the plugin is to be used with [crowdcontrol.live](https://crowdcontrol.live) which allows viewers to control your game via bits or channel points.

Commands are fully compatible with multiplayer servers. Everyone online will be affected to some extent.

## Manual Installation

1. Download and install [Java 16](https://adoptium.net/?variant=openjdk16&jvmVariant=hotspot).
2. Download the latest version of [Paper](https://papermc.io/downloads). Spigot is not supported.
3. Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the "root folder."
4. Create a new folder named `plugins` inside the root folder.
5. Download the [latest plugin jar](https://github.com/lexikiq/minecraft-crowdcontrol/releases/latest) and place it into the `plugins` folder.
6. Download and setup the [Crowd Control PC app](https://crowdcontrol.live/setup).
7. Run the Minecraft server. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing `eula.txt`.
    - Windows: Hold shift and right click inside the root folder. From the context menu, select "Open command window here" or "Open PowerShell here".
    - After opening your command window, run the command `java -Xmx2G -Xms2G -jar paper.jar nogui` (by typing that in and pressing enter)
    - When running for the first time you will have to edit the file `eula.txt` and then run the command again.
8. If your Minecraft server is running on a separate machine from the Crowd Control server, you may edit the `plugins/crowdcontrol/config.yml` file to specify a custom IP address.

## Automatic Installation

Soon you will be able to automatically launch a server from the [Crowd Control PC app](https://crowdcontrol.live/setup).
