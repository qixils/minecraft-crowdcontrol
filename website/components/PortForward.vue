<script lang="ts" setup>
  defineProps<{
    optional?: boolean,
    legacy?: boolean,
  }>()
</script>

<template>
  <li>
    <template v-if="optional">(Optional)</template>
    To allow friends to connect to your server, you will need to port forward.
    This involves modifying settings in your router's firewall, which requires administrative access to the device and whose exact steps vary from device to device.
    Please consult Google if you need assistance on this step as we are not able to help with the specific process.
    <ul>
      <li>Open the port <strong>25565</strong> to allow players to join the Minecraft server.</li>
      <li v-if="legacy">Open the port <strong>58431</strong> to allow players to connect their Crowd Control app to the server.</li>
    </ul>
  </li>
</template>
