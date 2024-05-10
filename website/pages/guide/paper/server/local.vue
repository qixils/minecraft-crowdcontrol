<script setup lang="ts">
import {computed} from 'vue';

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !paperVersions.includes(version.value)) { version.value = paperLatest; }
const supported = computed(() => supportedPaperVersions.includes(version.value));
const latest = computed(() => version.value === paperLatest);

useSeoMeta({
  title: `Paper ${version.value} Local Server Setup · Minecraft Crowd Control`,
  description: `Paper ${version.value} Local Server Setup Guide`,
  ogDescription: `Paper ${version.value} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Paper {{ version }} Local Server Setup</h1>

    <p class="alert alert-warning" v-if="!supported">
      The selected Minecraft version is no longer receiving mod updates.
      Please consider updating to {{ paperLatest }}.
    </p>

    <p>The following steps detail how to manually set up a Minecraft {{ version }} local Paper server with Crowd Control.</p>

    <ol>
      <li>Download and install <a href="https://adoptium.net/">the latest version of Java</a>. Java 17 or later is required.</li>
      <li>Download the latest version of <a :href="`https://papermc.io/downloads/${latest ? 'paper' : 'all'}`">Paper {{ version }}</a>. Spigot is not supported.</li>
      <li>Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the "root folder."</li>
      <li>Create a new folder named <code>plugins</code> inside the root folder.</li>
      <li>Download the <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=paper&g=${version}`">latest Paper plugin jar</a> and place it in the <code>plugins</code> folder.</li>
      <li>(Optional) Copy any other Paper plugins you want to play with into the <code>plugins</code> folder.</li>
      <li>Run the Minecraft server to initialize the plugin's configuration files. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing <code>eula.txt</code> in a text editing program like Notepad. To run the server:
        <ul>
          <li>On Windows, hold shift and right click inside the root folder. From the context menu, select "Open command window here" or "Open PowerShell here".</li>
          <li>After opening your command window, run the command <code>java -Xmx2G -Xms2G -jar paper.jar nogui</code> (by typing that in and pressing enter).</li>
          <li>When running for the first time you will have to edit the file <code>eula.txt</code> in a program like Notepad or <code>nano</code> and then run the command again.</li>
        </ul>
      </li>
      <li>(Optional) Once you shut down the server using <code>/stop</code>, you can edit the plugin's configuration file. The config file can be found at <code>&lt;root&gt;/plugins/CrowdControl/config.yml</code>.</li>
      <li>Ensure the ports 25565 and 58431 are open in your router's firewall so that users may connect to the Minecraft server and its Crowd Control server.</li>
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/paper/join?v=${version}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide your public IP address and the password used in the config file (default: <code>crowdcontrol</code>) to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>

    <p>You may also be interested in setting up <a href="https://geysermc.org/">GeyserMC</a> to allow Bedrock edition users (i.e. console players) to play.</p>
  </div>
</template>