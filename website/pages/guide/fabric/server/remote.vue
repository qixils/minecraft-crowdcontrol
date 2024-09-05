<script setup lang="ts">
import {computed} from 'vue';
import PortInstructions from '~/components/PortInstructions.vue'

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !fabricVersions.includes(version.value)) { version.value = fabricLatest; }
const supported = computed(() => supportedFabricVersions.includes(version.value));
const latest = computed(() => version.value === fabricLatest);

useSeoMeta({
  title: `Fabric ${version.value} Remote Server Setup · Minecraft Crowd Control`,
  description: `Fabric ${version.value} Remote Server Setup Guide`,
  ogDescription: `Fabric ${version.value} Remote Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Fabric {{ version }} Remote Server Setup</h1>

    <p class="alert alert-warning" v-if="!supported">
      The selected Minecraft version is no longer receiving mod updates.
      Please consider updating to {{ fabricLatest }}.
    </p>

    <p>
      The following steps detail how to set up a Minecraft {{ version }} remote Fabric server with Crowd Control.
      It is expected that you have already rented a remote server through a service like
      <a href="https://bloom.host/">bloom.host</a>.

      Note that no free hosts that we are aware of, including Aternos,
      support the firewall features necessary to use Crowd Control.
    </p>

    <ol>
      <li>In your admin panel, setup a Fabric {{ version }} server.</li>
      <li>In the file upload panel, create a new folder named <code>mods</code>.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/fabric-api/versions?g=${version}&c=release`">Fabric API</a> and upload it to the <code>mods</code> folder.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=fabric&g=${version}`">Crowd Control</a> and upload it to the <code>mods</code> folder.</li>
      <li>(Optional) Upload any other Fabric mods you want to play with into the <code>mods</code> folder.
        <ul>
          <li>(Optional) Pre-generating chunks using a mod like <a :href="`https://modrinth.com/plugin/chunky/versions?g=${version}&l=fabric`">Chunky</a> is recommended for optimal performance.</li>
        </ul>
      </li>
      <li>
        <PortInstructions :localLink="`/guide/fabric/server/local?v=${version}`" />
      </li>
      <li>Run the Minecraft server.</li>
      <li>(Optional) To edit the plugin's config file, you must first shut down the server using <code>/stop</code>. The config file can be found at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/fabric/join?v=${version}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide the server's IP address and the password used in the config file (default: <code>crowdcontrol</code>) to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>