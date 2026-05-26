<script setup lang="ts">
import JoinGuide from '~/components/JoinGuide.vue'
import { useVersion } from '~/composables/useVersion'
import * as versions from '~/utils/versions'

const versionData = useVersion(versions.unsureML)
const { version } = versionData

useSeoMeta({
  title: `${version.id} Join Guide · Minecraft Crowd Control`,
  description: `Tutorial on joining a Minecraft ${version.id} Crowd Control server`,
  ogDescription: `Tutorial on joining a Minecraft ${version.id} Crowd Control server`,
})
</script>

<template>
  <JoinGuide />
</template>
