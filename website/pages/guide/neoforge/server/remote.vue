<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import VersionWarning from '~/components/VersionWarning.vue'
import PortInstructions from '~/components/PortInstructions.vue'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.neoForgeML)
const { version } = versionData

useSeoMeta({
  title: `NeoForge ${version.id} Remote Server Setup · Minecraft Crowd Control`,
  description: `NeoForge ${version.id} Remote Server Setup Guide`,
  ogDescription: `NeoForge ${version.id} Remote Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>NeoForge {{ version.id }} Remote Server Setup</h1>

    <p class="alert alert-warning">
      The NeoForge mod is in beta. It may experience more mod incompatibilities than expected.
    </p>

    <VersionWarning :version-data="versionData" />

    <p>
      The following steps detail how to set up a Minecraft {{ version.id }} remote NeoForge server with Crowd Control.
      It is expected that you have already rented a remote server through a service like
      <a href="https://bloom.host/">bloom.host</a>.

      <template v-if="version.legacy">
        Note that no free hosts that we are aware of, including Aternos, support the firewall features
        necessary to use Crowd Control on the selected Minecraft version.
      </template>
    </p>

    <ol>
      <li>In your admin panel, setup a NeoForge {{ version.id }} server.</li>
      <li>In the file upload panel, create a new folder named <code>mods</code>.</li>
      <li v-if="version.id === '1.21.1'">Download the latest build of <a :href="`https://modrinth.com/mod/forgified-fabric-api/versions?l=neoforge&g=${version.id}&c=release`">Forgified Fabric API</a> and upload it to the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=neoforge&g=${version.id}`">Crowd Control</a> and upload it to the <code>mods</code> folder.</li>
      <li>(Optional) Upload any other NeoForge mods you want to play with into the <code>mods</code> folder.
        <ul>
          <li>(Optional) Pre-generating chunks using a mod like <a :href="`https://modrinth.com/plugin/chunky/versions?g=${version.id}&l=neoforge`">Chunky</a> is recommended for optimal performance.</li>
        </ul>
      </li>
      <PortInstructions v-if="version.legacy" :localLink="`/guide/neoforge/server/local?v=${version.id}`" />
      <li>Run the Minecraft server.</li>
      <li>(Optional) To edit the plugin's config file, you must first shut down the server using <code>/stop</code>. The config file can be found at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
    </ol>

    <p>
      Users may now connect using the <NuxtLink :to="`/guide/neoforge/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide.
      Make sure to provide the server's IP address
      <template v-if="version.legacy">and the password used in the config file (default: <code>crowdcontrol</code>)</template>
      to your streamers.
    </p>

    <p>
      For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command.
      This prevents unknown players from joining the server and potentially griefing your builds.
      Additionally, you can use permissions mods such as LuckPerms to restrict the <code>crowdcontrol.use</code> permission to trusted users.
    </p>
  </div>
</template>