<script setup lang="ts">
useServerSeoMeta({
  title: 'Setup · Minecraft Crowd Control',
  description: 'Dynamic setup guide for Minecraft Crowd Control',
  ogDescription: 'Dynamic setup guide for Minecraft Crowd Control',
})

import {computed} from 'vue'
import {fabricML} from "~/utils/versions";

// TODO: improve suggested articles for sponge

const question = useState('question', () => "gamemode")
const gamemode_index = useState<number | undefined>('gamemode_index', () => undefined)
const modloader_index = useState<number | undefined>('modloader_index', () => undefined)
const version_index = useState<number | undefined>('version_index', () => undefined)
const experience = useState<boolean | undefined>('experience', () => undefined)

const gamemodes = ["Singleplayer", "Multiplayer on a local server", "Multiplayer on a remote server"]
const gamemode = computed(() => {
  if (gamemode_index.value === undefined) return undefined
  return gamemodes[gamemode_index.value]
})

const modloader = computed(() => {
  if (modloader_index.value === undefined) return undefined
  return modloaders[modloader_index.value]
})

const version = computed(() => {
  if (version_index.value === undefined) return undefined
  return modloader.value!.versions[version_index.value]
})

function set(value: any) {
  switch (question.value) {
    case "gamemode":
      gamemode_index.value = value;
      question.value = "modloader";
      break;
    case "modloader":
      modloader_index.value = value;
      question.value = "version";
      break;
    case "version":
      version_index.value = value;
      question.value = (((modloader.value?.id === "unsure" && fabricVersions.includes(allVersions[value])) || modloader.value?.id === "fabric") && gamemode.value === "Singleplayer") ? "experience" : "done";
      break;
    case "experience":
      experience.value = value;
      question.value = "done";
      break;
  }
}

const guide = computed(() => {
  let gmval = gamemode.value
  let verval = version.value
  let mlval = modloader.value
  let expval = experience.value

  if (gmval === undefined) return "ERR! Missing field"
  if (verval === undefined) return "ERR! Missing field"
  if (mlval === undefined) return "ERR! Missing field"

  // pick modloader
  if (mlval.id === "unsure") {
    if (fabricVersions.includes(verval) && gmval === "Singleplayer" && expval === true) {
      mlval = fabricML;
    } else {
      // just pick the first supported one
      for (const ml of modloaders) {
        if (ml.id === "unsure") continue;
        if (ml.versions.includes(verval)) {
          mlval = ml;
          break;
        }
      }
    }

    if (mlval.id === "unsure") {
      return "ERR! Unable to pick a modloader for your version of Minecraft. Please pick one manually.";
    }
  }

  // pick page
  let page: string;
  if (gmval === "Multiplayer on a remote server") {
    page = "server/remote";
  } else if (mlval.id === "fabric" && gmval === "Singleplayer" && expval === true) {
    page = "client";
  } else if (verval === mlval.versions[0]) {
    page = "automatic";
  } else {
    page = "server/local";
  }

  return `/guide/${mlval.id}/${page}?v=${verval}`;
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
    <div v-if="question === 'gamemode'">
      <p>
        Are you intending to play alone, with friends on a server hosted on your PC, or with friends on a remotely hosted server?
      </p>
      <div class="buttons">
        <button v-for="(value, index) in gamemodes" :key="index" @click="set(index)">
          {{ value }}
        </button>
      </div>
    </div>
    <div v-else-if="question === 'modloader'">
      <p>What modloader are you using?</p>
      <div class="buttons">
        <button v-for="(value, index) in modloaders" :key="index" @click="set(index)">
          {{ value.name }}
        </button>
      </div>
    </div>
    <div v-else-if="question === 'version'">
      <p>What version of Minecraft are you playing?</p>
      <div class="buttons">
        <button v-for="(value, index) in modloader!.versions" :key="index" @click="set(index)">
          <span v-if="value === allVersions[0]">Latest ({{ value }})</span>
          <span v-else>{{ value }}</span>
        </button>
      </div>
    </div>
    <div v-else-if="question === 'experience'">
      <p>Would you consider yourself experienced at managing and installing Minecraft mods?</p>
      <div class="buttons">
        <button @click="set(true)">Yes</button>
        <button @click="set(false)">No</button>
      </div>
    </div>
    <div v-else-if="guide.startsWith('ERR! ')">
      <p>
        Error: {{ guide.substring(5) }}
      </p>
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
@media (max-width: 40em) {
  .buttons {
    flex-flow: column nowrap;
  }
  .buttons button {
    width: 100%;
  }
}
</style>
