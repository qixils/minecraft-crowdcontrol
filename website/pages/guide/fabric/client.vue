<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import VersionWarning from '~/components/VersionWarning.vue'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.fabricML)
const { version } = versionData

useSeoMeta({
  title: `Fabric ${version.id} Client Setup · Minecraft Crowd Control`,
  description: `Fabric ${version.id} Client Setup Guide`,
  ogDescription: `Fabric ${version.id} Client Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Fabric {{ version.id }} Client Setup</h1>

    <VersionWarning :version-data="versionData" />

    <h2>Installing the Mod</h2>

    <p>
      The following steps detail how to install the Crowd Control mod in the vanilla Minecraft launcher.
      Users of third-party launchers likely can skip this section, as you should be able to download and install the mod
      from <a href="https://modrinth.com/plugin/crowdcontrol">Modrinth</a>
      or <a href="https://curseforge.com/minecraft/mc-mods/crowdcontrol">CurseForge</a>
      using your launcher's mod management system.
    </p>

    <ol>
      <li>Download and run the <a href="https://fabricmc.net/use/installer/">Fabric Loader installer</a>.
        <ul>
          <li>You may first need to download and install <a href="https://adoptium.net/temurin/releases/">Java</a>.</li>
        </ul>
      </li>
      <li>Ensure the Minecraft version is set to {{ version.id }} and click the <code>Install</code> button.</li>
      <li>Navigate to your Minecraft installation folder.
        <ul>
          <li>On Windows, this can be accessed by Pressing <code>Windows Key + R</code> and typing <code>%AppData%\.minecraft</code>.</li>
        </ul>
      </li>
      <li>Create a new folder called <code>mods</code>.</li>
      <li>Download the latest build of the <a :href="`https://modrinth.com/mod/fabric-api/versions?g=${version.id}&c=release`">Fabric API</a> and place it in the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=fabric&g=${version.id}`">Crowd Control for Fabric</a> and place it in the <code>mods</code> folder.</li>
      <li>(Recommended) For the best experience and extra effects, we also suggest downloading and installing the following mods to the <code>mods</code> folder:
        <ul>
          <li><a :href="`https://modrinth.com/mod/language-reload/versions?l=fabric&g=${version.id}`">Language Reload</a></li>
        </ul>
      </li>
      <li>(Optional) Copy all the other Fabric {{ version.id }} mods that you want to play with into the <code>mods</code> folder.</li>
    </ol>

    <p>
      The mod is now installed! Open your Minecraft Launcher and select the <code>fabric-loader-{{version.id}}</code> profile to play.
      You may now <NuxtLink :to="`/guide/fabric/join?v=${version.id}`">join a Crowd Control server</NuxtLink>
      or <a href="#starting">start a single player session</a>.
    </p>

    <h2 id="starting">Starting a Single Player Session</h2>

    <p>
      The following steps detail how to start a Crowd Control session on a single player world.
      It assumes you have already downloaded and installed the mod.
    </p>

    <ol>
      <li>(Recommended) Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a> to be able to manage the cost of effects among other settings.</li>
      <li>Launch your modded instance of Minecraft {{ version.id }} and open the world you want to play on.</li>
      <li>Follow the instructions in the chat to link your Crowd Control account and start your session.</li>
      <li>Test some effects from the Interact Link shared in the chat to ensure everything is working.</li>
    </ol>
  </div>
</template>