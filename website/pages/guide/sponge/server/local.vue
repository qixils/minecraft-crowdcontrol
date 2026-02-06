<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import PortForward from '~/components/PortForward.vue'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.spongeML)
const { version } = versionData

const java = computed(() => {
  if (['1.12.2', '1.16.5'].includes(version.id)) return 8;
  return 21;
});
const forge = computed(() => {
  if (version.id === '1.12.2') return '14.23.5.2860';
  if (version.id === '1.16.5') return '36.2.39';
  if (version.id === '1.19.4') return '41.1.19';
  return 'unknown';
});

useSeoMeta({
  title: `Sponge ${version.id} Local Server Setup · Minecraft Crowd Control`,
  description: `Sponge ${version.id} Local Server Setup Guide`,
  ogDescription: `Sponge ${version.id} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h2>SpongeForge {{version.id}} Local Server Setup</h2>

    <p class="alert alert-warning">
      The Sponge plugins have reached end-of-life and will no longer be updated.
      Our ability to provide further support for these versions will be limited.
    </p>

    <p>The following steps detail how to manually set up a Minecraft {{version.id}} local Forge server with Crowd Control.</p>

    <ol>
      <li>If you're using a modpack, look on their downloads page for a "server pack." If you find one: download it, extract it, and skip to step 6.</li>
      <li>Download and install <a :href="`https://adoptium.net/temurin/releases/?version=${java}`">Java {{java}}</a>.</li>
      <li>Download and run the latest build (at least {{forge}}) of <a :href="`https://files.minecraftforge.net/net/minecraftforge/forge/index_${version.id}.html`">Forge {{version.id}}</a>.</li>
      <li>In the Forge pop-up window, click <code>Install server</code> and then the triple dots in the corner to open
        the directory selector. Find or create a new, empty folder and click Open, then click OK. This
        folder will be important in later steps and will hereafter be referred to as the "root folder".</li>
      <li>Navigate to the folder you created in your file explorer and create a new folder inside it called <code>mods</code>.</li>
      <li>Download the latest build of <a :href="`https://www.spongepowered.org/downloads/spongeforge?minecraft=${version.id}&offset=0`">SpongeForge</a> and place it in the <code>mods</code> folder.</li>
      <li>Create a new <code>plugins</code> folder inside the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=${version.id}`">Crowd Control</a> and place it in the <code>plugins</code> folder.</li>
      <li>(Optional) Copy any other Forge {{version.id}} mods or Sponge {{version.api}} plugins that you want to play with into the <code>mods</code> folder and the <code>plugins</code> folder respectively.
        <ul>
          <li>
            Please read
            <NuxtLink :to="`/guide/sponge/troubleshooting?v=${version.id}#incompatible-mods`">this section of the troubleshooting guide</NuxtLink>
            to ensure you do not install any incompatible mods.
          </li>
          <li v-if="version.id === '1.16.5'">
            (Optional) Speedrunners, consider installing <a :href="`https://modrinth.com/mod/depiglining/versions?g=${version.id}&l=forge`">my mod that emulates 1.16.1 speedrunning</a>!
          </li>
        </ul>
      </li>
      <li>Run the Minecraft server to initialize the plugin's configuration files. You will have to run it
        twice, as the first will prompt you to accept Minecraft's End User License Agreement.
        To run the server:
        <ul>
          <li>On Windows, hold shift and right click inside the root folder. From the context menu, select
            "Open command window here" or "Open PowerShell here".</li>
          <li>After opening your command window, run the
            command <code>java -Xmx2G -Xms2G -jar forge-{{version.id}}-{{forge}}.jar nogui</code> (by typing that in and
            pressing enter). You can adjust the gigabytes of RAM used by the software by altering the <code>2G</code>
            text, i.e. <code>-Xmx4G -Xms4G</code> would allocate 4 gigabytes of RAM to the game. This may be
            necessary if you are playing with large mods.</li>
          <li>When running for the first time, you will have to edit the file <code>eula.txt</code> in a program like
            Notepad or <code>nano</code> and then run the server command again.</li>
        </ul>
      </li>
      <li>(Optional) Once you shut down the server using <code>/stop</code>, you can edit the plugin's configuration
        file. The config file is located at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
      <PortForward />
    </ol>

    <p>
      Users, including yourself, may now connect using the <NuxtLink :to="`/guide/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide.
      On your computer, you can connect using the host <code>localhost</code>,
      but anyone else will need your public IP address.
    </p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This
      prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>