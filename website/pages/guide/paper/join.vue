<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.paperML)
const { version } = versionData

useSeoMeta({
  title: `Paper ${version.id} Join Guide · Minecraft Crowd Control`,
  description: `Paper ${version.id} Join Guide`,
  ogDescription: `Paper ${version.id} Join Guide`,
})
</script>

<template>
  <div>
    <h1>Paper: Joining an External Server</h1>

    <p>The following steps are for streamers who are connecting to another individual's server.</p>

    <ol v-if="!version.legacy">
      <li>(Recommended) Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a> to be able to manage the cost of effects among other settings.</li>
      <li>(Optional) For extra effects and the best experience, install the client mod using the <NuxtLink :to="`/guide/fabric/client?v=${version.id}`">Fabric</NuxtLink> or <NuxtLink :to="`/guide/neoforge/client?v=${version.id}`">NeoForge</NuxtLink> Client Setup guide.</li>
      <li>Join the Minecraft server in your game using the IP address provided to you by your server administrator.</li>
      <li>Follow the instructions in the chat to link your Crowd Control account and start your session.</li>
      <li>Test some effects from the Interact Link shared in the chat to ensure everything is working.</li>
    </ol>
    <ol v-else>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Selection</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Under <strong>Select a Pack</strong>, choose the last option <strong>Minecraft (Legacy)</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter the IP address of the server and click next.</li>
      <li>If you were provided a secret passphrase by the server administrator, enter it here. Otherwise, leave the default value of <code>crowdcontrol</code>. Click next.</li>
      <li>The checklist should now all have green checks.
        <ul>
          <li>If you are the server owner and are seeing Awaiting Connector or Connector Error, make sure the port 58431 is open on the server's firewall.
            If your server host only assigns random ports, make sure to specify that port in the config file at <code>&lt;root&gt;/plugins/CrowdControl/config.yml</code>.</li>
        </ul>
      </li>
      <li>(Optional) For extra effects and the best experience, install the client mod using the <NuxtLink :to="`/guide/fabric/client?v=${version.id}`">Fabric</NuxtLink> or <NuxtLink :to="`/guide/neoforge/client?v=${version.id}`">NeoForge</NuxtLink> Client Setup guide.</li>
      <li>Join the Minecraft server in your game (usually using the same IP as the one you entered in step 7).</li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. Use the username from the top left corner of the Crowd Control app.</li>
      <li>Click the <strong>Start Session</strong> button in the Crowd Control app.
        <ul>
          <li>If you are the server owner and the button is greyed out, make sure the server is using the latest version of the mod.</li>
        </ul>
      </li>
      <li>Open the <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
    </ol>
  </div>
</template>
