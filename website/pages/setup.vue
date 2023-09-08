<script setup lang="ts">
import {computed} from 'vue'

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

const modloaders: string[][] = [
  ["Unsure", ...allVersions],
  ["Paper", ...paperVersions],
  ["Fabric", ...fabricVersions],
  ["Sponge/Forge", ...spongeVersionsArray]
]
const modloader = computed(() => {
  if (modloader_index.value === undefined) return undefined
  return modloaders[modloader_index.value][0]
})
const modloaderVersions = computed(() => {
  if (modloader_index.value === undefined) return []
  return modloaders[modloader_index.value].slice(1)
})

const version = computed(() => {
  if (version_index.value === undefined) return undefined
  return modloaderVersions.value[version_index.value]
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
      question.value = (modloader.value === "Unsure" && gamemode.value === "Singleplayer" && fabricVersions.includes(modloaderVersions.value[value])) ? "experience" : "done";
      break;
    case "experience":
      experience.value = value;
      question.value = "done";
      break;
  }
}

const guide = computed(() => {
  if (gamemode.value === undefined) return "ERR! Missing field"
  if (version.value === undefined) return "ERR! Missing field"
  if (modloader.value === undefined) return "ERR! Missing field"

  let gmval = gamemode.value
  let verval = version.value
  let mlval = modloader.value
  let mlversval = modloaderVersions.value
  let expval = experience.value

  // pick modloader
  if (mlval === "Unsure") {
    if (fabricVersions.includes(verval) && gmval === "Singleplayer" && expval === true) {
      mlval = "Fabric";
      mlversval = fabricVersions;
    } else {
      // just pick the first supported one
      for (const ml of modloaders) {
        if (ml[0] === "Unsure") continue;
        let versval = ml.slice(1);
        if (versval.includes(verval)) {
          mlval = ml[0];
          mlversval = versval;
          break;
        }
      }
    }

    if (mlval === "Unsure") {
      return "ERR! Unable to pick a modloader for your version of Minecraft. Please pick one manually.";
    }
  }

  // pick page
  let page: string;
  if (gmval === "Multiplayer on a remote server") {
    page = "server/remote";
  } else if (mlval === "Fabric" && gmval === "Singleplayer" && expval === true) {
    page = "client";
  } else if (verval === mlversval[0]) {
    page = "automatic";
  } else {
    page = "server/local";
  }

  return `/guide/${mlval!.toLocaleLowerCase("en-US")}/${page}?v=${verval}`;
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
          {{ value[0] }}
        </button>
      </div>
    </div>
    <div v-else-if="question === 'version'">
      <p>What version of Minecraft are you playing?</p>
      <div class="buttons">
        <button v-for="(value, index) in modloaderVersions" :key="index" @click="set(index)">
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
        Error: {{ guide }}
      </p>
    </div>
    <div v-else>
      Please follow <NuxtLink :to="guide">this guide</NuxtLink> to set up Crowd Control.
    </div>
  </main>
</template>
