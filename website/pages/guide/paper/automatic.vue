<script setup lang="ts">
const version = paperLatest;

useSeoMeta({
  title: `Paper ${version} Automatic Server Setup · Minecraft Crowd Control`,
  description: `Paper ${version} Automatic Server Setup Guide`,
  ogDescription: `Paper ${version} Automatic Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Paper {{ version }} Automatic Setup</h1>

    <p>The following steps provide a quick setup for running a local game server.</p>

    <img id="tutorial" src="https://images.warp.world/site/setup/minecraftconfig2.gif" alt="Demonstration of configuring Minecraft in the Crowd Control app." />

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Paper</strong> and click next.</li>
      <li>Select <strong>Local</strong>.</li>
      <li>Select <strong>Click to install</strong> and <strong>Okay</strong>.</li>
      <li>(Optional) You may now select <strong>Open Folder</strong> next to <strong>Minecraft</strong> on the <strong>Game Paths</strong> tab and copy any Paper plugins you want to play with into the <code>Paper/plugins</code> folder.</li>
      <li>In the <strong>Live</strong> tab, select <strong>Launch Server</strong>.</li>
      <li>Launch your instance of Minecraft {{ version }}.</li>
      <li>In Minecraft, add the server <code>localhost</code> to your multiplayer server list and join it.</li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. The username should come from what's displayed in the top left corner of their Crowd Control app.</li>
      <li>Select <strong>Start Session</strong> in the Crowd Control app.</li>
      <li>(Optional) To allow friends to connect to your server, you will need to port forward.
        <ul>
          <li>Open the port <strong>25565</strong> to allow players to join the Minecraft server.</li>
          <li>Open the port <strong>58431</strong> to allow players to connect their Crowd Control app to the server.</li>
        </ul>
      </li>
    </ol>
  </div>
</template>

<style scoped>
  img {
    display: block;
    margin: 0 auto;
    max-width: 28rem;
  }
</style>
