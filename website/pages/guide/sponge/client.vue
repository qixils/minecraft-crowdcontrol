<script setup lang="ts">
import {computed} from 'vue';

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !spongeVersions.has(version.value)) { version.value = spongeLatest; }
const latest = computed(() => version.value === spongeLatest);
const api = computed(() => spongeVersions.get(version.value));

useSeoMeta({
  title: `Sponge ${version.value} Client Setup · Minecraft Crowd Control`,
  description: `Sponge ${version.value} Client Setup Guide`,
  ogDescription: `Sponge ${version.value} Client Setup Guide`,
})
</script>

<template>
  <div>
    <h1>SpongeForge {{ version }} Client Setup</h1>

    <p class="alert alert-warning">
      Aside from potentially easier setup for singleplayer sessions,
      there is currently no advantage to adding the Sponge mod to your client,
      so feel free to skip this guide if you only plan to play on servers.
    </p>

    <p class="alert alert-danger" v-if="version === '1.12.2'">
      Usage of Sponge on the client-side is <strong>strongly discouraged</strong> for {{ version }}
      due to a crashing issue caused by colliding with certain entities (boats, shulkers, etc.).
      Please use the <NuxtLink :to="`/guide/sponge/automatic?v=${version}`">automatic</NuxtLink>
      or <NuxtLink :to="`/guide/sponge/server/local?v=${version}`">manual</NuxtLink> installation methods instead.
    </p>

    <h2>Installing the Mod</h2>

    <p>
      The following steps detail how to install the Crowd Control mod in the vanilla Minecraft launcher for SpongeForge {{ version }}.
      Users of third-party launchers likely can skip this section, as you should be able to download
      <a :href="`https://spongepowered.org/downloads/spongeforge?minecraft=${version}&offset=0`">Sponge</a>
      and
      <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=${version}`">the mod</a>,
      then drag and drop them into your launcher's mod management system.
    </p>

    <ol>
      <li>Download and install the latest build of <a :href="`https://files.minecraftforge.net/net/minecraftforge/forge/index_${version}.html`">Forge {{ version }}</a>.
        <ul>
          <li>You may first need to download and install <a href="https://adoptium.net/temurin/releases/">Java</a> to run the installer.</li>
        </ul>
      </li>
      <li>Navigate to your Minecraft installation folder.
        <ul>
          <li>On Windows, this can be accessed by Pressing <code>Windows Key + R</code> and typing <code>%AppData%\.minecraft</code>.</li>
        </ul>
      </li>
      <li>Create a new folder called <code>mods</code>.</li>
      <li>Download the latest build of <a :href="`https://spongepowered.org/downloads/spongeforge?minecraft=${version}&offset=0`">Sponge</a> and place it in the <code>mods</code> folder.</li>
      <li>Create a new folder inside the <code>mods</code> folder called <code>plugins</code>.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=${version}`">Crowd Control for Sponge {{api}}</a> and place it in the <code>plugins</code> folder.</li>
      <li>
        (Optional) Copy all the other Forge {{version}} mods or Sponge {{api}} plugins that you want to play with
        into the <code>mods</code> folder and the <code>plugins</code> folder respectively.
        <ul>
          <li>
            Please read
            <NuxtLink :to="`/guide/sponge/troubleshooting?v=${version}#incompatible-mods`">this section of the troubleshooting guide</NuxtLink>
            to ensure you do not install any incompatible mods.
          </li>
          <li v-if="version === '1.16.5'">
            (Optional) Speedrunners, consider installing <a :href="`https://modrinth.com/mod/depiglining/versions?g=${version}&l=forge`">my mod that emulates 1.16.1 speedrunning</a>!
          </li>
        </ul>
      </li>
    </ol>

    <p>
      The mod is now installed! Open your Minecraft Launcher and select the <code>forge</code> profile to play.
      You may now <NuxtLink :to="`/guide/sponge/join?v=${version}`">join a Crowd Control server</NuxtLink>
      or <a href="#starting">start a single player session</a>.
    </p>

    <h2 id="starting">Starting a Single Player Session</h2>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Sponge {{api}}</strong> and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter in <code>localhost</code> as the host and click next.</li>
      <li>Accept the default password <code>crowdcontrol</code> by clicking next.</li>
      <li>Launch your modded instance of Minecraft {{version}} and open the world you want to play on.</li>
      <li>In the app, if you see a <strong>Connector Error</strong> button, click on it to refresh the connection to the game.</li>
      <li>Select <strong>Start Session</strong> in the Crowd Control app.</li>
      <li>Open the <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
    </ol>
  </div>
</template>