<script setup lang="ts">
import * as versions from '../../../../utils/versions'
import PortForward from '~/components/PortForward.vue';
import VersionWarning from '~/components/VersionWarning.vue';
import { useVersion } from '~/composables/useVersion';

const versionData = useVersion(versions.paperML)
const { version } = versionData

useSeoMeta({
  title: `Paper ${version.id} Local Server Setup · Minecraft Crowd Control`,
  description: `Paper ${version.id} Local Server Setup Guide`,
  ogDescription: `Paper ${version.id} Local Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Paper {{ version.id }} Local Server Setup</h1>

    <VersionWarning :version-data="versionData" />

    <p>The following steps detail how to manually set up a Minecraft {{ version.id }} local Paper server with Crowd Control.</p>

    <ol>
      <li>Download and install <a href="https://adoptium.net/">the latest version of Java</a>. Java 21 or later is required.</li>
      <li>Download the latest version of <a :href="`https://papermc.io/downloads/${version.latest ? 'paper' : 'all'}`">Paper {{ version.id }}</a>. Spigot is not supported.</li>
      <li>Place the Paper jar in a new, empty folder. This folder will hereafter be referred to as the "root folder."</li>
      <li>Create a new folder named <code>plugins</code> inside the root folder.</li>
      <li>Download the <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=paper&g=${version.id}`">latest Paper plugin jar</a> and place it in the <code>plugins</code> folder.</li>
      <li>(Optional) Copy any other Paper plugins you want to play with into the <code>plugins</code> folder.</li>
      <li>Run the Minecraft server to initialize the plugin's configuration files. You will have to run it twice, as the first will prompt you to accept Minecraft's End User License Agreement by editing <code>eula.txt</code> in a text editing program like Notepad. To run the server:
        <ul>
          <li>On Windows, hold shift and right click inside the root folder. From the context menu, select "Open command window here" or "Open PowerShell here".</li>
          <li>After opening your command window, run the command <code>java -Xmx2G -Xms2G -jar paper.jar nogui</code> (by typing that in and pressing enter).</li>
          <li>When running for the first time you will have to edit the file <code>eula.txt</code> in a program like Notepad or <code>nano</code> and then run the command again.</li>
        </ul>
      </li>
      <li>(Optional) Once you shut down the server using <code>/stop</code>, you can edit the plugin's configuration file. The config file can be found at <code>&lt;root&gt;/plugins/CrowdControl/config.yml</code>.</li>
      <PortForward :legacy="version.legacy" />
    </ol>

    <p>
      Users, including yourself, may now connect using the <NuxtLink :to="`/guide/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide.
      On your computer, you can connect using the host <code>localhost</code>,
      but anyone else will need your public IP address.
    </p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>

    <p>You may also be interested in setting up <a href="https://geysermc.org/">GeyserMC</a> to allow Bedrock edition users (i.e. console players) to play.</p>
  </div>
</template>