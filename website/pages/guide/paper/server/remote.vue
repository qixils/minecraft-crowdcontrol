<script setup lang="ts">
const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !paperVersions.includes(version.value)) { version.value = paperLatest; }

useSeoMeta({
  title: `Paper ${version.value} Remote Server Setup · Minecraft Crowd Control`,
  description: `Paper ${version.value} Remote Server Setup Guide`,
  ogDescription: `Paper ${version.value} Remote Server Setup Guide`,
})
</script>

<template>
  <div>
    <h1>Paper {{ version }} Remote Server Setup</h1>

    <p>
      The following steps detail how to set up a Minecraft {{ version }} remote Paper server with Crowd Control.
      It is expected that you have already rented a remote server through a service like
      <a href="https://grryno.com/">Grryno</a> or <a href="https://bloom.host/">bloom.host</a>.

      Note that no free hosts that we are aware of, including Aternos,
      support the firewall features necessary to use Crowd Control.
    </p>

    <ol>
      <li>In your admin panel, setup a Paper {{ version }} server. Spigot is not supported.</li>
      <li>In the file upload panel, create a new folder named <code>plugins</code>.</li>
      <li>Download the latest build of <a :href="`https://modrinth.com/mod/crowdcontrol/versions?l=paper&g=${version}`">Crowd Control</a> and upload it to the <code>plugins</code> folder.</li>
      <li>(Optional) Upload any other Paper plugins you want to play with into the <code>plugins</code> folder.
        <ul>
          <li>(Optional) Pre-generating chunks using a plugin like <a :href="`https://modrinth.com/plugin/chunky/versions?g=${version}&l=paper`">Chunky</a> is recommended for optimal performance.</li>
        </ul>
      </li>
      <li>In the firewall/ports panel, open the port 58431 so that users may connect to the Crowd Control server.</li>
      <li>Run the Minecraft server.</li>
      <li>(Optional) To edit the plugin's config file, you must first shut down the server using <code>/stop</code>. The config file can be found at <code>&lt;root&gt;/plugins/CrowdControl/config.yml</code>.</li>
    </ol>

    <p>Users may now connect using the <NuxtLink :to="`/guide/paper/join?v=${version}`"><strong>Joining a Server</strong></NuxtLink> guide. Make sure to provide the server's IP address and the password used in the config file (default: <code>crowdcontrol</code>) to your streamers.</p>

    <p>For extra security, consider enabling a user whitelist using the vanilla <code>/whitelist</code> command. This prevents unknown players from joining the server and potentially griefing your builds.</p>

    <p>You may also be interested in setting up <a href="https://geysermc.org/">GeyserMC</a> to allow Bedrock edition users (i.e. console players) to play.</p>
  </div>
</template>