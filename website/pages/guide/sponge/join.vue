<script setup lang="ts">
import { useVersion } from '~/composables/useVersion'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.spongeML)
const { version } = versionData

useSeoMeta({
  title: `Sponge ${version.id} Join Guide · Minecraft Crowd Control`,
  description: `Sponge ${version.id} Join Guide`,
  ogDescription: `Sponge ${version.id} Join Guide`,
})
</script>

<template>
  <div>
    <h1>Sponge {{ version.api }}: Joining an External Server</h1>

    <p class="alert alert-warning">
      The Sponge plugins have reached end-of-life and will no longer be updated.
      Our ability to provide further support for these versions will be limited.
    </p>

    <p>The following steps are for streamers who are connecting to another individual's server.</p>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Selection</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter the IP address of the server (usually the same as the one you connect to in Minecraft) and click next.</li>
      <li>If you were provided a secret passphrase by the server administrator, enter it here. Otherwise, leave the default value of <code>crowdcontrol</code>. Click next.</li>
      <li>The checklist should now all have green checks.
        <ul>
          <li>If you are the server owner and are seeing Awaiting Connector or Connector Error, make sure the port 58431 is open on the server's firewall. If you are assigned a random port, make sure to specify that port in the mod's config file.</li>
        </ul>
      </li>
      <li>Join the Minecraft {{ version.id }} server in your game (usually using the same IP as the one you entered in step 7).</li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. The username should come from what's displayed in the top left corner of their Crowd Control app.</li>
      <li>Click the <strong>Start Session</strong> button in the Crowd Control app.
        <ul>
          <li>If you are the server owner and the button is greyed out, make sure the server is using the latest version of the mod.</li>
        </ul>
      </li>
      <li>Open the <strong>Effect Manager</strong> in the Crowd Control app to test effects.</li>
    </ol>
  </div>
</template>