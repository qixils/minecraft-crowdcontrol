<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.unsureML)
const { version } = versionData

useSeoMeta({
  title: `${version.id} Join Guide · Minecraft Crowd Control`,
  description: `Tutorial on joining a Minecraft ${version.id} Crowd Control server`,
  ogDescription: `Tutorial on joining a Minecraft ${version.id} Crowd Control server`,
})

const fabricAvailable = computed(() => versions.fabricML.versions.some(item => item.id === version.id))
const neoAvailable = computed(() => versions.neoForgeML.versions.some(item => item.id === version.id))
</script>

<template>
  <div>
    <h1>Joining an External {{ version.id }} Server</h1>

    <p>The following steps are for streamers who are connecting to another individual's Minecraft {{ version.id }} Crowd Control server.</p>

    <ol v-if="!version.legacy">
      <li>(Recommended) Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a> to be able to manage the cost of effects among other settings.</li>
      <li>(Optional) For extra effects and the best experience, install the client mod using the <NuxtLink :to="`/guide/fabric/client?v=${version.id}`">Fabric</NuxtLink> or <NuxtLink :to="`/guide/neoforge/client?v=${version.id}`">NeoForge</NuxtLink> Client Setup guide.</li>
      <li>Join the Minecraft server in your game using the IP address provided to you by your server administrator.
        <ul>
          <li>This is usually the same IP address you use to connect in-game.</li>
          <li>For local servers, use <code>localhost</code>.</li>
        </ul>
      </li>
      <li>Follow the instructions in the chat to link your Crowd Control account and start your session.</li>
      <li>Test some effects from the Interact Link shared in the chat to ensure everything is working.</li>
    </ol>
    <ol v-else>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Selection</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Under <strong>Select a Pack</strong>, choose the last option <strong>Minecraft (Legacy)</strong>.
        <ul>
          <li>If you selected the wrong option, you can click on the blue button that says "Default" on the top of the app to re-open this menu.</li>
        </ul>
      </li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter the IP address provided to you by your server administrator and click next.
        <ul>
          <li>This is usually the same IP address you use to connect in-game.</li>
          <li>For local servers, use <code>localhost</code>.</li>
        </ul>
      </li>
      <li>If you were provided a secret passphrase by the server administrator, enter it here. Otherwise, leave the default value of <code>crowdcontrol</code>. Click next.</li>
      <li>The checklist should now all have green checks.
        <ul>
          <li>If you are the server owner and are seeing Awaiting Connector or Connector Error, make sure the port 58431 is open on the server's firewall.
            If your server host only assigns random ports, make sure to specify that port in the config file at <code>&lt;root&gt;/plugins/CrowdControl/config.yml</code>.</li>
        </ul>
      </li>
      <li v-if="fabricAvailable || neoAvailable">
        (Optional) For extra effects and the best experience, install the client mod using the
        <NuxtLink v-if="fabricAvailable" :to="`/guide/fabric/client?v=${version.id}`">Fabric</NuxtLink>
        <span v-if="fabricAvailable && neoAvailable"> or </span>
        <NuxtLink v-if="neoAvailable" :to="`/guide/neoforge/client?v=${version.id}`">NeoForge</NuxtLink>
        Client Setup guide.
      </li>
      <li>Join the Minecraft server in your game.
        <ul>
          <li>This is usually the same IP address you use to connect in the app.</li>
          <li>For local servers, use <code>localhost</code>.</li>
        </ul>
      </li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. Use the username from the top left corner of the Crowd Control app.</li>
      <li>Click the <strong>Start Session</strong> button in the Crowd Control app.</li>
      <li>Open the <strong>Effect List</strong> in the Crowd Control app to test effects.</li>
    </ol>
  </div>
</template>
