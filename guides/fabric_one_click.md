## Fabric: Automatic Local Server

The following steps provide a quick setup for running a local game server.

TODO: rewrite for client-side mod, probably

1. Download and install the [Crowd Control PC app](https://crowdcontrol.live/setup).
2. In the Game Selection tab, select **Minecraft (Fabric) (PC)**.
3. Click **One Click Server Setup** to automatically set up a server. This may take several minutes
   to complete. Make sure to accept any prompts to install software (namely the Java runtime
   environment).
4. Once setup is done, you can click the **Open Server Folder** button and copy any
   Fabric 1.19.3 mods you want to play with into the `mods` folder.
5. Click the **Launch Server** button to start the Minecraft server. You may optionally run
   `/password <password>` in the console to change the password required to connect to the server.
   By default, the password is set to `crowdcontrol`.
6. Once the Minecraft server has started, click **Start** in the Crowd Control app to connect to the
   server. Enter in `localhost` as the host and the password you set (or `crowdcontrol` by default).
7. Open your Minecraft: Java Edition launcher (vanilla or custom is ok), select your 1.19.3-based
   modpack, and press Play.
8. Open the Multiplayer menu, use the Add Server button with the IP `localhost` if you haven't
   already, and connect to the server.
9. In Minecraft, run the command `/account link <your twitch username>` to ensure you receive
   effects that viewers purchase for you. Example: `/account link jaku`
