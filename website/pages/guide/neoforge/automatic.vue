<script setup lang="ts">
import PortForward from '~/components/PortForward.vue'

const version = neoForgeLatest

useSeoMeta({
  title: `NeoForge ${version.id} Automatic Server Setup · Minecraft Crowd Control`,
  description: `NeoForge ${version.id} Automatic Server Setup Guide`,
  ogDescription: `NeoForge ${version.id} Automatic Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>NeoForge {{ version.id }} Automatic Setup</h1>

    <p class="alert alert-warning">
      The NeoForge mod is in early alpha. It may experience more mod incompatibilities than expected.
      Additionally, some instructions relating to the Crowd Control app may be unavailable as we roll out support.
    </p>

    <p>The following steps provide a quick setup for running a local game server.</p>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Local</strong>.</li>
      <li>Select <strong>NeoForge</strong> and click next.</li>
      <li>Select <strong>Click to install</strong> and <strong>Okay</strong>.</li>
      <li>(Optional) You may now select <strong>Open Folder</strong> next to <strong>Minecraft</strong> on the <strong>Game Paths</strong> tab and copy any NeoForge {{ version.id }} mods you want to play with into the <code>NeoForge/mods</code> folder. If you're playing a modpack, you may also have to copy the modpack's <code>config</code> folder into the <code>NeoForge</code> folder.</li>
      <li>In the <strong>Live</strong> tab, select <strong>Launch Server</strong>.</li>
      <li>(Optional) For extra effects and the best experience, install the client mod using the <NuxtLink to="/guide/neoforge/client">Client Setup</NuxtLink> guide.</li>
      <li>Launch your instance of Minecraft {{ version.id }}. If you added any mods to the server, your client will need to have the same mods installed.</li>
      <li>In Minecraft, add the server <code>localhost</code> to your multiplayer server list and join it.</li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. The username should come from what's displayed in the top left corner of their Crowd Control app.</li>
      <li>Select <strong>Start Session</strong> in the Crowd Control app.</li>
      <li>Open the <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
      <PortForward />
    </ol>
  </div>
</template>