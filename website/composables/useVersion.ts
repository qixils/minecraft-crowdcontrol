import { reactiveComputed, toReactive, useUrlSearchParams } from '@vueuse/core'
import type { Reactive, UnwrapNestedRefs } from 'vue'
import type { Modloader, Version } from '~/utils/versions'

export interface VersionData<V extends Version = Version> {
  version: UnwrapNestedRefs<V>
  latestVersion: UnwrapNestedRefs<V>
  updateAvailable: Ref<boolean>
}

export const useVersion = <V extends Version>(ml: Modloader<V>): VersionData<V> => {
  const route = useRoute()
  const versionId = computed(() => route.query.v)

  const latestVersion = computed(() => ml.versions.find(v => v.latest) || ml.versions[0])

  const version = computed(() => ml.versions.find(v => v.id === versionId.value) || latestVersion.value)

  const updateAvailable = computed(() => latestVersion.value.id !== version.value.id)

  return {
    version: toReactive(version),
    latestVersion: toReactive(latestVersion),
    updateAvailable,
  }
}
