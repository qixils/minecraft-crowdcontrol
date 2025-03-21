<script lang="ts" setup>
  import type { VersionData } from '~/composables/useVersion';
  
  defineProps<{
    versionData: Pick<VersionData, 'version' | 'updateAvailable' | 'latestVersion'>,
  }>()
</script>

<template>
  <p class="alert alert-warning" v-if="!versionData.version.supported">
    The selected Minecraft version is no longer receiving mod updates on this modloader.
    <template v-if="versionData.updateAvailable.value">Please consider updating to {{ versionData.latestVersion.id }}.</template>
  </p>
</template>