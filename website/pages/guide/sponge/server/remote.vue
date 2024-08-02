<script setup lang="ts">
import {computed} from 'vue';

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !spongeVersions.has(version.value)) { version.value = spongeLatest; }
const latest = computed(() => version.value === spongeLatest);
const api = computed(() => spongeVersions.get(version.value));

useSeoMeta({
  title: `Sponge ${version.value} Remote Server Setup · Minecraft Crowd Control`,
  description: `Sponge ${version.value} Remote Server Setup Guide`,
  ogDescription: `Sponge ${version.value} Remote Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>SpongeForge {{ version }} Remote Server Setup</h1>

    <p>
      The following steps detail how to set up a Minecraft {{ version }} remote Forge server with Crowd Control.
      It is expected that you have already rented a remote server through a service like
      <a href="https://grryno.com/">Grryno</a> or <a href="https://bloom.host/">bloom.host</a>.

      Note that no free hosts that we are aware of, including Aternos,
      support the firewall features necessary to use Crowd Control.
    </p>

    <ol>
      <li>In your admin panel, setup a Forge {{ version }} server.</li>
      <li>In the file upload panel, create a new folder named <code>mods</code>.</li>
      <li>Download the latest build of <a :href="`https://www.spongepowered.org/downloads/spongeforge?minecraft=${version}&offset=0`">SpongeForge</a> and upload it to the <code>mods</code> folder.</li>
      <li>In the <code>mods</code> folder, create a new folder named <code>plugins</code>.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=sponge&g=${version}`">Crowd Control</a> and upload it to the <code>plugins</code> folder.</li>
      <li>(Optional) Upload any other Forge {{version}} mods or Sponge {{api}} plugins that you want to play with into the <code>mods</code> folder and the <code>plugins</code> folder respectively.
        <ul>
          <li>
            Please read
            <NuxtLink :to="`/guide/sponge/troubleshooting?v=${version}#incompatible-mods`">this section of the troubleshooting guide</NuxtLink>
            to ensure you do not install any incompatible mods.
          </li>
          <li v-if="api >= 8">(Optional) Pre-generating chunks using a mod like <a :href="`https://modrinth.com/plugin/chunky/versions?g=${version}&l=sponge`">Chunky</a> is recommended for optimal performance.</li>
          <li v-if="version === '1.16.5'">
            (Optional) Speedrunners, consider installing <a :href="`https://modrinth.com/mod/depiglining/versions?g=${version}&l=forge`">my mod that emulates 1.16.1 speedrunning</a>!
          </li>
        </ul>
      </li>
      <li>
        In the firewall/ports panel, open the port 58431 so that users may connect to the Crowd Control server.
        <br />ℹ️ If you aren't paying for a dedicated IP address then you will likely will be unable to choose a specific port to open and instead get assigned a random one.
        You will have to update the plugin's config file, as described below, to use your assigned port.
        You should also be sure to use your assigned port in the Crowd Control app at the end of the host field,
        i.e. for IP <code>1.2.3.4</code> and port <code>5678</code>, you would connect to <code>1.2.3.4:5678</code>.
      </li>
      <li>Run the Minecraft server.</li>
      <li>(Optional) To edit the plugin's config file, you must first shut down the server using <code>/stop</code>. The config file can be found at <code>&lt;root&gt;/config/crowdcontrol.conf</code>.</li>
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/sponge/join?v=${version}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide the server's IP address and the password used in the config file (default: <code>crowdcontrol</code>) to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>
  </div>
</template>