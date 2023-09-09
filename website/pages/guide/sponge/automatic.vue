<script setup lang="ts">
import {computed} from 'vue';

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !spongeVersions.has(version.value)) { version.value = spongeLatest; }
const latest = computed(() => version.value === spongeLatest);
const api = computed(() => spongeVersions.get(version.value));

useSeoMeta({
  title: `Sponge ${version} Automatic Server Setup · Minecraft Crowd Control`,
  description: `Sponge ${version} Automatic Server Setup Guide`,
  ogDescription: `Sponge ${version} Automatic Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Sponge {{ version }} Automatic Setup</h1>

    <p>The following steps provide a quick setup for running a local game server.</p>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Library</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Sponge {{ api }}</strong> and click next.</li>
      <li>Select <strong>Local</strong>.</li>
      <li>Select <strong>Click to install</strong> and <strong>Okay</strong>.</li>
      <li>
        (Optional) You may now select <strong>Open Folder</strong> next to <strong>Minecraft</strong> on the <strong>Game Paths</strong> tab
        and copy any Sponge {{ api }} or Forge {{ version }} mods you want to play with into the <code>Sponge{{ api }}/mods</code> folder.
        <ul>
          <li>
            If you're playing a modpack, you should instead extract the modpack's server pack into the <code>Sponge{{ api }}</code> folder.
          </li>
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
      <li>In the <strong>Live</strong> tab, select <strong>Launch Server</strong>.</li>
      <li>Launch your instance of Minecraft {{ version }}. If you added any mods to the server, your client will need to have the same mods installed.</li>
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