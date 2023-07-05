## Sponge 8: Joining an External Server

The following steps are for streamers who are connecting to another individual's server.

1. Download and install the [Crowd Control 2.0 app](https://beta.crowdcontrol.live/).
2. In the **Game Selection** tab, select **Minecraft**.
3. Select **Configure Minecraft**.
4. Enter your Minecraft username and click next.
5. Select **Sponge 8** and click next.
6. Select **Remote**.
7. Enter the IP address of the server (usually the same as the one you connect to in Minecraft)
   and click next.
8. If you were provided a secret passphrase by the server administrator, enter it here. Otherwise,
   leave the default value of `crowdcontrol`. Click next.
9. The checklist should now all have green checks.
    - If you are the server owner and are seeing Awaiting Connector or Connector Error, make sure
      the port 58431 is open on the server's firewall. If you are assigned a random port, make sure
      to specify that port in the mod's config file.
10. Join the Minecraft server in your game
    (usually using the same IP as the one you entered in step 7).
11. (Optional) If you're sharing effects with another streamer, run the command
    `/account link INSERT_USERNAME` to receive their effects. The username should come from what's
    displayed in the top left corner of their Crowd Control app.
12. Click the **Start Session** button in the Crowd Control app.
    - If you are the server owner and the button is greyed out, make sure the server is using the
      latest version of the mod.

If you experience issues at any point, please reach out for help on our
[Discord](https://discord.gg/warpworld).