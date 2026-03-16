<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.spongeML)
const { version } = versionData

useSeoMeta({
  title: `Sponge ${version.id} Remote Server Setup · Minecraft Crowd Control`,
  description: `Sponge ${version.id} Remote Server Setup Guide`,
  ogDescription: `Sponge ${version.id} Remote Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>SpongeForge {{ version.id }} Remote Server Setup</h1>

    <p class="alert alert-warning">
      The Sponge plugins have reached end-of-life and will no longer be updated.
      Our ability to provide further support for these versions will be limited.
    </p>

    <p>
      The following steps detail how to set up a Minecraft {{ version.id }} remote Forge server with Crowd Control.
      It is expected that you have already rented a remote server through a service like
      <a href="https://bloom.host/">bloom.host</a>.

      <template v-if="version.legacy">
        Note that no free hosts that we are aware of, including Aternos, support the firewall features
        necessary to use Crowd Control on the selected Minecraft version.
      </template>
    </p>

    <ol>
      <li>In your admin panel, setup a Forge {{ version.id }} server.</li>
      <li>In the file upload panel, create a new folder named <code>mods</code>.</li>
      <li>Download the latest build of <a :href="`https://www.spongepowered.org/downloads/spongeforge?minecraft=${version.id}&offset=0`">SpongeForge</a> and upload it to the <code>mods</code> folder.</li>
      <li>In the <code>mods</code> folder, create a new folder named <code>plugins</code>.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=${version.id}`">Crowd Control</a> and upload it to the <code>plugins</code> folder.</li>
      <li>(Optional) Upload any other Forge {{version.id}} mods or Sponge {{version.api}} plugins that you want to play with into the <code>mods</code> folder and the <code>plugins</code> folder respectively.
        <ul>
          <li>
            Please read
            <NuxtLink :to="`/guide/sponge/troubleshooting?v=${version.id}#incompatible-mods`">this section of the troubleshooting guide</NuxtLink>
            to ensure you do not install any incompatible mods.
          </li>
          <li v-if="version.api >= 8">(Optional) Pre-generating chunks using a mod like <a :href="`https://modrinth.com/plugin/chunky/versions?g=${version.id}&l=sponge`">Chunky</a> is recommended for optimal performance.</li>
          <li v-if="version.id === '1.16.5'">
            (Optional) Speedrunners, consider installing <a :href="`https://modrinth.com/mod/depiglining/versions?g=${version.id}&l=forge`">my mod that emulates 1.16.1 speedrunning</a>!
          </li>
        </ul>
      </li>
      <li>
        <PortInstructions :localLink="`/guide/sponge/server/local?v=${version.id}`" />
      </li>
      <li>Run the Minecraft server.</li>
      <li>(Optional) To edit the plugin's config file, you must first shut down the server using <code>/stop</code>. The config file can be found at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
    </ol>

    <p>Users, including yourself, may now connect using the <NuxtLink :to="`/guide/join?v=${version.id}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide the server's IP address and the password used in the config file (default: <code>crowdcontrol</code>) to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>