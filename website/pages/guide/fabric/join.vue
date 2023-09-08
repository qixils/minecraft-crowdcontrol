<script setup lang="ts">
</script>

<template>
  <div>
    <h1>Fabric: Joining an External Server</h1>

    <p>The following steps are for streamers who are connecting to another individual's server.</p>

    <ol>
      <li>Download and install the <a href="https://crowdcontrol.live/">Crowd Control app</a>.</li>
      <li>In the <strong>Game Selection</strong> tab, select <strong>Minecraft</strong>.</li>
      <li>Select <strong>Configure Minecraft</strong>.</li>
      <li>Enter your Minecraft username and click next.</li>
      <li>Select <strong>Fabric</strong> and click next.</li>
      <li>Select <strong>Remote</strong>.</li>
      <li>Enter the IP address of the server (usually the same as the one you connect to in Minecraft) and click next.</li>
      <li>If you were provided a secret passphrase by the server administrator, enter it here. Otherwise, leave the default value of <code>crowdcontrol</code>. Click next.</li>
      <li>The checklist should now all have green checks.
        <ul>
          <li>If you are the server owner and are seeing Awaiting Connector or Connector Error, make sure the port 58431 is open on the server's firewall. If you are assigned a random port, make sure to specify that port in the mod's config file.</li>
        </ul>
      </li>
      <li>(Optional) For extra effects and the best experience, install the client mod using the <NuxtLink to="/guide/fabric/client">Client Setup</NuxtLink> guide.</li>
      <li>Join the Minecraft server in your game (usually using the same IP as the one you entered in step 7).</li>
      <li>(Optional) If you're sharing effects with another streamer, run the command <code>/account link INSERT_USERNAME</code> to receive their effects. The username should come from what's displayed in the top left corner of their Crowd Control app.</li>
      <li>Select <strong>Start Session</strong> in the Crowd Control app.
        <ul>
          <li>If you are the server owner and the button is greyed out, make sure the server is using the latest version of the mod.</li>
        </ul>
      </li>
    </ol>
  </div>
</template>