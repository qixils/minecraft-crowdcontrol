<script setup lang="ts">
import { computed } from 'vue';
import { neoForgeVersions as versions, supportedNeoForgeVersions as supportedVersions, neoForgeLatest as latestVersion } from '../../../utils/versions'

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !versions.includes(version.value)) { version.value = latestVersion; }
const supported = computed(() => supportedVersions.includes(version.value));
const latest = computed(() => version.value === latestVersion);

useSeoMeta({
  title: `NeoForge ${version.value} Client Setup · Minecraft Crowd Control`,
  description: `NeoForge ${version.value} Client Setup Guide`,
  ogDescription: `NeoForge ${version.value} Client Setup Guide`,
})
</script>

<template>
  <div>
    <h1>NeoForge {{ version }} Client Setup</h1>

    <p class="alert alert-warning">
      The NeoForge mod is in early alpha. It may experience more mod incompatibilities than expected.
      Additionally, some instructions relating to the Crowd Control app may be unavailable as we roll out support.
    </p>

    <p class="alert alert-warning" v-if="!supported">
      The selected Minecraft version is no longer receiving mod updates.
      Please consider updating to {{ latestVersion }}.
    </p>

    <h2>Installing the Mod</h2>

    <p>
      The following steps detail how to install the Crowd Control mod in the vanilla Minecraft launcher.
      Users of third-party launchers likely can skip this section, as you should be able to download and install the mod
      from <a href="https://modrinth.com/plugin/crowdcontrol">Modrinth</a>
      or <a href="https://curseforge.com/minecraft/mc-mods/crowdcontrol">CurseForge</a>
      using your launcher's mod management system.
    </p>

    <ol>
      <li>Download and run the <a href="https://neoforged.net/">NeoForge installer</a>.
        <ul>
          <li>You may first need to download and install <a href="https://adoptium.net/temurin/releases/">Java</a>.</li>
        </ul>
      </li>
      <li>Ensure the Minecraft version is set to {{ version }} and click the <code>Install</code> button.</li>
      <li>Navigate to your Minecraft installation folder.
        <ul>
          <li>On Windows, this can be accessed by Pressing <code>Windows Key + R</code> and typing <code>%AppData%\.minecraft</code>.</li>
        </ul>
      </li>
      <li>Create a new folder called <code>mods</code>.</li>
      <li>Download the latest build of the <a :href="`https://modrinth.com/mod/forgified-fabric-api/versions?l=neoforge&g=${version}&c=release`">Forgified Fabric API</a> and place it in the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=neoforge&g=${version}`">Crowd Control for NeoForge</a> and place it in the <code>mods</code> folder.</li>
      <li>(Optional) Copy all the other NeoForge {{ version }} mods that you want to play with into the <code>mods</code> folder.</li>
    </ol>

    <p>
      The mod is now installed! Open your Minecraft Launcher and select the <code>NeoForge</code> profile to play.
      You may now <NuxtLink :to="`/guide/neoforge/join?v=${version}`">join a Crowd Control server</NuxtLink>
      or <a href="#starting">start a single player session</a>.
    </p>

    <h2 id="starting">Starting a Single Player Session</h2>

    <p>The following steps detail how to start a Crowd Control session on a single player world. It assumes
      you have already downloaded and installed the mod.</p>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter in <code>localhost</code> as the host and click next.</li>
      <li>Accept the default password <code>crowdcontrol</code> by clicking next.</li>
      <li>Launch your modded instance of Minecraft {{ version }} and open the world you want to play on.</li>
      <li>In the app, if you see a <strong>Connector Error</strong> button, click on it to refresh the connection to the game.</li>
      <li>Select <strong>Start Session</strong> in the Crowd Control app.</li>
      <li>Open the <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
    </ol>
  </div>
</template>