<script setup lang="ts">
import { computed } from 'vue';
import { neoForgeVersions as versions, supportedNeoForgeVersions as supportedVersions, neoForgeLatest as latestVersion } from '../../../../utils/versions'

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !versions.includes(version.value)) { version.value = latestVersion; }
const supported = computed(() => supportedVersions.includes(version.value));
const latest = computed(() => version.value === latestVersion);

useSeoMeta({
  title: `NeoForge ${version.value} Local Server Setup · Minecraft Crowd Control`,
  description: `NeoForge ${version.value} Local Server Setup Guide`,
  ogDescription: `NeoForge ${version.value} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>NeoForge {{version}} Local Server Setup</h1>

    <p class="alert alert-warning">
      The NeoForge mod is in early alpha. It may experience more mod incompatibilities than expected.
      Additionally, some instructions relating to the Crowd Control app may be unavailable as we roll out support.
    </p>

    <p class="alert alert-warning" v-if="!supported">
      The selected Minecraft version is no longer receiving mod updates.
      Please consider updating to {{ latestVersion }}.
    </p>

    <p>The following steps detail how to manually set up a Minecraft {{version}} local NeoForge server with Crowd Control.</p>

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
      <li>Download the latest build of the <a :href="`https://modrinth.com/mod/forgified-fabric-api/versions?l=neoforge&g=${version}&c=release`">Forgified Fabric API</a> and place it in the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=neoforge&g=${version}`">Crowd Control for NeoForge</a> and place it in the <code>mods</code> folder.</li>
      <li>(Optional) Copy any other NeoForge {{version}} mods that you want to play with into the <code>mods</code> folder.</li>
      <li>Run the Minecraft server using the <code>start.bat</code> file on Windows or <code>start.sh</code> on Linux to initialize the plugin's configuration files. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement.</li>
      <li>To change the plugin's configuration file, you must first shut down the server by typing <code>stop</code> in the server window. The config file is located at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
      <li>Ensure the ports 25565 and 58431 are open in your router's firewall so that users may connect to the Minecraft server and its Crowd Control server.</li>
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/neoforge/join?v=${version}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide your public IP address and the password used in the config file to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>