<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import type { VersionData } from '~/composables/useVersion';
  
  const props = defineProps<{
    versionData: Pick<VersionData, 'version' | 'updateAvailable' | 'latestVersion'>,
  }>()

  const latestUrl = ref('')

  onMounted(() => {
    watch(() => props.versionData.latestVersion.id, (versionId) => {
      // Technically this can point to a non-recommended guide
      // (like, a manual setup for latest version)
      // But I don't think this is a big deal
      const url = new URL(window.location.href)
      url.searchParams.set('v', versionId)
      latestUrl.value = url.toString()
    }, { immediate: true })
  })
</script>

<template>
  <p class="alert" :class="versionData.version.legacy ? 'alert-danger' : 'alert-warning'" v-if="!versionData.version.supported">
    The selected Minecraft version is no longer receiving mod updates on this modloader<template v-if="versionData.version.legacy">
    and uses an old method of connecting to Crowd Control that is no longer maintained</template>.
    <template v-if="versionData.updateAvailable.value">
      Please consider updating to <component :is="latestUrl ? 'a' : 'span'" :href="latestUrl">{{ versionData.latestVersion.id }}</component>.
    </template>
  </p>
</template>