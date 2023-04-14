## Fabric 1.19.4 Client Installation

The following steps detail how to manually install Crowd Control for Minecraft Fabric 1.19.3.

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the Game Selection tab, select **Minecraft (Fabric) (PC)**.
    - If you do not see this option, you can use **Minecraft (Paper Server) (PC)** instead while we
      work on rolling out this new version.
3. Download and run the [Fabric Loader installer](https://fabricmc.net/use/installer/).
    - Users of platforms other than Windows may first need to download and install
      [Java 17](https://adoptium.net/temurin/releases/?version=17).
4. Ensure the Minecraft version is set to 1.19.4 and click the `Install` button.
5. Navigate to your Minecraft installation folder.
    - On Windows, this can be accessed by Pressing `Windows Key + R` and typing
      `%AppData%\.minecraft`.
6. Create a new folder called `mods`.
7. Download the latest build of the
   [Fabric API](https://modrinth.com/mod/fabric-api/versions?g=1.19.4&c=release)
   and place it in the `mods` folder.
8. Download the latest build of
   [Crowd Control for Fabric](https://modrinth.com/mod/crowdcontrol/versions?l=fabric&g=1.19.4)
   and place it in the `mods` folder.
9. Copy all the other Fabric 1.19.4 mods that you want to play with into the `mods` folder.

The mod is now installed! Open your Minecraft Launcher and select the `fabric-loader` profile to
play. To use Crowd Control on a single player world:

1. Open the world you want to play on.
2. Click **Start Session** in the Crowd Control app.
3. Enter `localhost` as the host and `crowdcontrol` as the password.
4. In Minecraft, run the command `/account link <your twitch username>` to ensure you receive
   effects that viewers purchase for you. Example: `/account link jaku`

To use Crowd Control on an external server, see the
[**Joining a Server**](fabric_joining_a_server.md) guide.
