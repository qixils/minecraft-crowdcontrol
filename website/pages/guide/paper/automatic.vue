<script setup lang="ts">
import PortForward from '~/components/PortForward.vue'

const version = paperLatest;

useSeoMeta({
  title: `Paper ${version.id} Automatic Server Setup · Minecraft Crowd Control`,
  description: `Paper ${version.id} Automatic Server Setup Guide`,
  ogDescription: `Paper ${version.id} Automatic Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Paper {{ version.id }} Automatic Setup</h1>

    <p>The following steps provide a quick setup for running a local game server.</p>

    <img id="tutorial" src="https://images.warp.world/site/setup/minecraftconfig2.gif" alt="Demonstration of configuring Minecraft in the Crowd Control app." />

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Under <strong>Select a Pack</strong>, choose the first option <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Select <strong>Local</strong>.</li>
      <li>Select <strong>Paper</strong> and click next.</li>
      <li>Select <strong>Click to install</strong> and <strong>Okay</strong>.</li>
      <li>(Optional) You may now select <strong>Open Folder</strong> next to <strong>Minecraft</strong> on the <strong>Game Paths</strong> tab and copy any Paper plugins you want to play with into the <code>Paper/plugins</code> folder.</li>
      <li>In the <strong>Live</strong> tab, select <strong>Launch Server</strong>.</li>
      <li>Launch your instance of Minecraft {{ version.id }}.</li>
      <li>In Minecraft, add the server <code>localhost</code> to your multiplayer server list and join it.</li>
      <li>Follow the instructions in the chat to link your Crowd Control account and start your session.</li>
      <li>Open the <strong>Effect List</strong> or <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
      <PortForward optional />
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
