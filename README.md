# Minecraft Crowd Control: The Vote Update

Minecraft Crowd Control is a Minecraft mod for various platforms that allows your livestream
viewers to interact with your game using bits, channel points, donations to charity, and more.

Beginning with v2, this plugin has been developed for use
with [crowdcontrol.live](https://crowdcontrol.live). This service handles the integration with
Twitch (and other platforms) and is what your viewers will interact with to control your game.

This mod is fully compatible with multiplayer servers. Players on the server will be able to
receive effects targeted at them using **`/account link <username>`**.  
For example, if you are doing a Crowd Control stream under the Twitch username
**`minecraftfan1992`**, then you would run the in-game command **`/account link minecraftfan1992`**
to receive the effects that your viewers are buying for you.

## The Vote Update

This special release of Minecraft Crowd Control adds support for the Minecraft April Fools' Day
update. This update added a voting system to the game which allows players to enact silly, random
laws that change the behavior of the game. Naturally, we had to take this power from the streamer
and give it to their chats. This special release gives your live viewers on Twitch the power to
change the rules of the game by typing the number or letter corresponding to the law they wish to
enact.

![A screenshot from a player in a taiga forest overlooking a river. In the top left is a HUD displaying a proposal for a chat to vote on. The option "Endermen Pick Up Anything" is winning with 100% of the vote over "Do Nothing."](https://user-images.githubusercontent.com/13265322/231916445-ecd2de5b-9f7f-4794-9b20-b7478dff54ce.png)

Using Twitch chat integration is incredibly simple, as it's automatically started when you type
**`/account link <username>`** in-game like you usually do to receive Crowd Control effects.
The integration even works on servers that aren't running Crowd Control, although you should note
that there will be a delay between when chat finishes voting on a proposal and when the proposal
actually goes into effect.

Note that this update is available exclusively for the Fabric version of the mod. Furthermore, this
update is a special release which is unlikely to see new features going forward.

## Installation

You can follow [this guide](guides/client_installation.md) to install the mod on your client.

Server owners can follow [this guide](guides/manual_installation.md) to install the mod on their
server.
