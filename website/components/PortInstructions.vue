<script setup lang="ts">
defineProps<{
  localLink?: string
}>()
</script>

<template>
  <div class="mb-1">
    Locate the "Firewall" or "Ports" panel on your server host's dashboard.
    Open a <i>new</i> TCP port, preferrably using the mod's default value 58431
    (see <b>Setup for non-dedicated IPs</b> if unavailable),
    so that users may connect their Crowd Control app to the server.
    <b>Do not</b> select "Make Primary" on the new port.
  </div>
  <UAccordion
    color="primary"
    variant="soft"
    size="sm"
    :items="[{
      label: 'Setup for non-dedicated IPs',
      icon: 'i-heroicons-information-circle',
      slot: 'nondedicated',
    }, {
      label: 'Setup for free server hosts (Aternos)',
      icon: 'i-heroicons-information-circle',
      slot: 'free',
    }, {
      label: 'Still need help?',
      icon: 'i-heroicons-information-circle',
      slot: 'help',
    }]"
  >
    <template #nondedicated>
      If you aren't paying for a dedicated IP address then you will likely will be unable to choose a specific port to open and instead get assigned a random one.
      You will have to update the plugin's config file, as described below, to match your assigned port.
      You should also be sure to use your assigned port in the Crowd Control app at the end of the host field,
      i.e. for IP <code>1.2.3.4</code> and port <code>5678</code>, you would connect to <code>1.2.3.4:5678</code>.
    </template>
    <template #free>
      No free hosts that we are aware of, including Aternos, support the firewall features necessary to use Crowd Control.
      If you need a free host, <a :href="localLink">use your computer</a>.
      Otherwise, you can rent a server from a service like <a href="https://bloom.host">bloom.host</a>.
    </template>
    <template #help>
      Unfortunately we cannot provide support for individual server hosts.
      If you need help, please ask your server host's support team for assistance.
    </template>
  </UAccordion>
</template>