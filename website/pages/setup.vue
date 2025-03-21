<script setup lang="ts">
useServerSeoMeta({
  title: 'Setup · Minecraft Crowd Control',
  description: 'Dynamic setup guide for Minecraft Crowd Control',
  ogDescription: 'Dynamic setup guide for Minecraft Crowd Control',
})

import { computed } from 'vue'
import { useUrlSearchParams } from '@vueuse/core'
import * as versions from "~/utils/versions"

// TODO: improve suggested articles for sponge

interface Answer {
  slug: string
  title: string
}

interface QuestionMetadata {
  slug: string
  crumb: string
  title: string
  answers: Ref<Answer[]>
  subtext?: string
  hidden?: boolean
}

interface Question extends QuestionMetadata {
  answer: Ref<string | undefined>
}

const questions: Question[] = []
const searchParams = useUrlSearchParams('history', {
  writeMode: 'push',
})

// TODO: can i do this as like, deep reactive or something?
const pushQuestion = (question: QuestionMetadata) => {
  const answer = computed({
    get() {
      const param = searchParams[question.slug]
      return Array.isArray(param) ? param[0] : param
    },
    set(newValue) {
      searchParams[question.slug] = newValue
    },
  })
  const fullQuestion = {...question, answer}
  questions.push(fullQuestion)
  return fullQuestion
}

const questionEnvironment = pushQuestion({
  slug: "environment",
  crumb: "Environment",
  title: "Are you intending to play alone, with friends on a server hosted on your PC (free but complex), or with friends on a remotely hosted server?",
  answers: ref([
    {
      slug: 'solo',
      title: 'Singleplayer',
    },
    {
      slug: 'local',
      title: 'Local Multiplayer',
    },
    {
      slug: 'remote',
      title: 'Remote Multiplayer',
    },
  ]),
})
const questionModloader = pushQuestion({
  slug: "modloader",
  crumb: "Modloader",
  title: "What modloader are you using?",
  answers: ref(versions.modloaders.map(({id, name}) => ({slug: id, title: name}))),
})
const questionVersion = pushQuestion({
  slug: "version",
  crumb: "Version",
  title: "What version of Minecraft are you playing?",
  subtext: "Any version not listed is unsupported.",
  answers: computed(() => {
    const vals = (questionModloader.answer.value && versions.modloadersBySlug[questionModloader.answer.value]?.versions) || versions.allVersions
    return vals.map((version, index) => ({ slug: version.id, title: !index ? `${version.id} (Newest)` : version.id }))
  }),
})
const questionExperience = pushQuestion({
  slug: "experience",
  crumb: "Experience",
  title: "Would you consider yourself experienced at managing and installing Minecraft mods?",
  hidden: true,
  answers: computed(() =>
    questionEnvironment.answer.value === 'solo' && questionModloader.answer.value && ['fabric', 'neoforge'].includes(questionModloader.answer.value)
      ? [{slug: 'yes', title: 'Yes'},{slug: 'no', title: 'No'}]
      : []
  )
})

const question = computed(() => questions.find(item => !item.answer.value && item.answers.value.length))
const guide = computed(() => { // TODO: lazy?
  const env = questionEnvironment.answer.value
  const versionId = questionVersion.answer.value
  const experienced = questionExperience.answer.value === 'yes'
  
  const loader: string = questionModloader.answer.value && questionModloader.answer.value !== 'unsure'
    ? questionModloader.answer.value
    : versions.fabricVersions.some(v => v.id === versionId)
      ? 'fabric'
      : versions.neoForgeVersions.some(v => v.id === versionId)
        ? 'neoforge'
        : versions.paperVersions.some(v => v.id === versionId)
          ? 'paper'
          : versions.modloaders.find(modloader => modloader.id !== 'unsure' && modloader.versions.some(v => v.id === versionId))!.id // (sponge fallback lol)
  
  const loaderVersion = modloadersBySlug[loader] && modloadersBySlug[loader].versions.find(v => v.id === versionId)
  const isLatest = !!loaderVersion?.latest
  const isSupported = !!loaderVersion?.supported
  const isLegacy = !!loaderVersion?.legacy

  const page = experienced
    ? 'client'
    : env === 'remote'
      ? 'server/remote'
      : env === 'local' || !isLatest || !isSupported || isLegacy
        ? 'server/local'
        : 'automatic'

  const url = new URL(`/guide/${loader}/${page}`, window.location.href)
  if (versionId) url.searchParams.set('v', versionId);

  return url.toString()
})
</script>

<template>
  <main>
    <h1>Dynamic Setup Guide</h1>
    <p>
      This is the dynamic setup guide for Minecraft Crowd Control. If you're not sure what that is, you should
      <NuxtLink to="/">read this first</NuxtLink>.
    </p>
    <hr>
    <div v-if="question">
      <p>
        {{ question.title }}
        <span v-if="question.subtext" class="subtext">{{ question.subtext }}</span>
      </p>
      <div class="buttons">
        <button v-for="answer in question.answers.value" @click="question.answer.value = answer.slug">
          {{ answer.title }}
        </button>
      </div>
    </div>
    <div v-else>
      Please follow <NuxtLink :to="guide" class="underline">this guide</NuxtLink> to set up Crowd Control.
    </div>
  </main>
</template>

<style scoped>
.buttons {
  display: flex;
  flex-flow: row wrap;
  gap: 0.5rem;
  justify-content: center;
}
.buttons button {
  background-color: var(--accent-color);
  color: var(--accent-text-color);
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 0.3rem;
  box-shadow: var(--accent-color) 0 0 0.2rem;
  cursor: pointer;
}
.subtext {
  opacity: 50%;
}
@media (max-width: 40em) {
  .buttons {
    flex-flow: column nowrap;
  }
  .buttons button {
    width: 100%;
  }
}
</style>
