<script setup lang="ts">
import * as versions from '../../../../utils/versions'
import PortForward from '~/components/PortForward.vue';
import VersionWarning from '~/components/VersionWarning.vue';
import { useVersion } from '~/composables/useVersion';

const versionData = useVersion(versions.neoForgeML)
const { version } = versionData

useSeoMeta({
  title: `NeoForge ${version.id} Local Server Setup · Minecraft Crowd Control`,
  description: `NeoForge ${version.id} Local Server Setup Guide`,
  ogDescription: `NeoForge ${version.id} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>NeoForge {{ version.id }} Local Server Setup</h1>

    <p class="alert alert-warning">
      The NeoForge mod is in beta. It may experience more mod incompatibilities than expected.
    </p>

    <VersionWarning :version-data="versionData" />

    <p>The following steps detail how to manually set up a Minecraft {{ version.id }} local NeoForge server with Crowd Control.</p>

    <ol>
      <li>If you're using a modpack, look on their downloads page for a "server pack." If you find one: download it, extract it, and skip to step 6.</li>
      <li>Download and run the <a href="https://neoforged.net/">NeoForge installer</a>.
        <ul>
          <li>You may first need to download and install <a href="https://adoptium.net/temurin/releases/">Java</a>.</li>
        </ul>
      </li>
      <li>In the installer window, click the <code>Install server</code> option and then the triple dots next to the Install Location to open the directory selector. Find or create a new, empty folder and click Open, then click OK. This folder will be important in later steps so don't lose it. From here on, this folder will be referred to as the "root folder".</li>
      <li>Click the <code>Proceed</code> button to create the NeoForge server. After the setup is complete, click OK.</li>
      <li>Navigate to where you installed the server and create a new folder called <code>mods</code>.</li>
      <li v-if="version.id === '1.21.1'">Download the latest build of <a :href="`https://modrinth.com/mod/forgified-fabric-api/versions?l=neoforge&g=${version.id}&c=release`">Forgified Fabric API</a> and place it in the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=neoforge&g=${version.id}`">Crowd Control for NeoForge</a> and place it in the <code>mods</code> folder.</li>
      <li>(Optional) Copy any other NeoForge {{ version.id }} mods that you want to play with into the <code>mods</code> folder.</li>
      <li>Run the Minecraft server using the <code>start.bat</code> file on Windows or <code>start.sh</code> on Linux to initialize the plugin's configuration files. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement.</li>
      <li>To change the plugin's configuration file, you must first shut down the server by typing <code>stop</code> in the server window. The config file is located at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
      <PortForward :legacy="version.legacy" />
    </ol>

    <p>
      Users may now connect using the <NuxtLink :to="`/guide/neoforge/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide.
      On your computer, you can connect using the host <code>localhost</code>,
      but anyone else will need your public IP address.
    </p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>