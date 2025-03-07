<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import VersionWarning from '~/components/VersionWarning.vue'
import PortForward from '~/components/PortForward.vue'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.fabricML)
const { version } = versionData

useSeoMeta({
  title: `Fabric ${version.id} Local Server Setup · Minecraft Crowd Control`,
  description: `Fabric ${version.id} Local Server Setup Guide`,
  ogDescription: `Fabric ${version.id} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Fabric {{ version.id }} Local Server Setup</h1>

    <VersionWarning :version-data="versionData" />

    <p>The following steps detail how to manually set up a Minecraft {{ version.id }} local Fabric server with Crowd Control.</p>

    <ol>
      <li>If you're using a modpack, look on their downloads page for a "server pack." If you find one: download it, extract it, and skip to step 7.</li>
      <li>Download and run the <a href="https://fabricmc.net/use/installer/">Fabric Loader installer</a>.
        <ul>
          <li>Users of platforms other than Windows may first need to download and install <a href="https://adoptium.net/temurin/releases/?version=21">Java 21</a>.</li>
        </ul>
      </li>
      <li>In the installer window, click the <code>Server</code> tab and then the triple dots next to the Install Location to open the directory selector. Find or create a new, empty folder and click Open, then click OK. This folder will be important in later steps so don't lose it. From here on, this folder will be referred to as the "root folder".</li>
      <li>Ensure the Minecraft version is set to {{ version.id }} and click the <code>Install</code> button to create the Fabric server. After the setup is complete, click <code>Download server jar</code> and <code>Generate</code> when prompted.</li>
      <li>Close the installer.</li>
      <li>Navigate to where you installed the server and create a new folder called <code>mods</code>.</li>
      <li>Download the latest build of the <a :href="`https://modrinth.com/mod/fabric-api/versions?g=${version.id}&c=release`">Fabric API</a> and place it in the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=fabric&g=${version.id}`">Crowd Control for Fabric</a> and place it in the <code>mods</code> folder.</li>
      <li>(Optional) Copy any other Fabric {{ version.id }} mods that you want to play with into the <code>mods</code> folder.</li>
      <li>Run the Minecraft server using the <code>start.bat</code> file on Windows or <code>start.sh</code> on Linux to initialize the plugin's configuration files. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement.</li>
      <li>To change the plugin's configuration file, you must first shut down the server by typing <code>stop</code> in the server window. The config file is located at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
      <PortForward />
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/fabric/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide your public IP address and the password used in the config file to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>