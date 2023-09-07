<script setup lang="ts">
import {computed} from 'vue'

const question = useState('question', () => 0)
const singleplayer = useState<boolean | undefined>('singleplayer', () => undefined)
const version_index = useState<number | undefined>('version_index', () => undefined)
const modloader_index = useState<number | undefined>('modloader_index', () => undefined)

const version = computed(() => {
  if (version_index.value === undefined) return undefined
  return allVersions[version_index.value]
})

const modloaders: string[] = ["Unsure", "Paper", "Fabric", "Sponge", "Forge"]
const modloader = computed(() => {
  if (modloader_index.value === undefined) return undefined
  return modloaders[modloader_index.value]
})

function set(value: any) {
  switch (question.value) {
    case 0:
      singleplayer.value = value;
      break;
    case 1:
      version_index.value = value;
      break;
    case 2:
      modloader_index.value = value;
      break;
  }
  question.value++;
}
</script>

<template>
  <main>
    <h1>Dynamic Setup Guide</h1>
    <p>
      This is the dynamic setup guide for Minecraft Crowd Control. If you're not sure what that is, you should
      <NuxtLink to="/">read this first</NuxtLink>.
    </p>
    <hr>
    <div v-if="question === 0">
      <p>Are you intending to play singleplayer or multiplayer?</p>
      <div class="buttons">
        <button @click="set(true)">Singleplayer</button>
        <button @click="set(false)">Multiplayer</button>
      </div>
    </div>
    <div v-else-if="question === 1">
      <p>What version of Minecraft are you playing?</p>
      <div class="buttons">
        <button v-for="(version, index) in allVersions" :key="index" @click="set(index)">
          <span v-if="index === 0">Latest ({{ version }})</span>
          <span v-else>{{ version }}</span>
        </button>
      </div>
    </div>
    <div v-else-if="question === 2">
      <p>What modloader are you using?</p>
      <div class="buttons">
        <button v-for="(modloader, index) in modloaders" :key="index" @click="set(index)">
          {{ modloader }}
        </button>
      </div>
    </div>
    <div v-else>
      <!-- TODO -->
      <p>
        <NuxtLink :to="`/setup?singleplayer=${singleplayer}&version=${version}&modloader=${modloader}`">
          Click here to go to the setup page.
        </NuxtLink>
      </p>
    </div>
    <!-- TODO -->
  </main>
</template>
