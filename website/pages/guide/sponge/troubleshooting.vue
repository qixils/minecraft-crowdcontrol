<script setup lang="ts">
import {computed} from 'vue';

const route = useRoute();
const version = useState('version', () => route.query.v);
if (!version.value || !spongeVersions.has(version.value)) { version.value = spongeLatest; }
const latest = computed(() => version.value === spongeLatest);
const api = computed(() => spongeVersions.get(version.value));
const docs = computed(() => {
  if (api.value === 7) return '7.4.0';
  return 'stable';
})

useSeoMeta({
  title: `Sponge ${version.value} Troubleshooting Guide · Minecraft Crowd Control`,
  description: `Sponge ${version.value} Troubleshooting Guide`,
  ogDescription: `Sponge ${version.value} Troubleshooting Guide`,
})

// TODO: move all the mod data here
</script>

<template>
  <div>
    <h1>Sponge {{ api }} Troubleshooting</h1>

    <p class="alert alert-warning">
      The Sponge plugins have reached end-of-life and will no longer be updated.
      Our ability to provide further support for these versions will be limited.
    </p>

    <h2>"Failed to load a valid ResourcePackInfo"</h2>

    <p>This warning is thrown by Sponge when running in the client but is totally safe to ignore.</p>

    <h2>"CrowdControl is not a valid mod file"</h2>

    <p>If you see this warning on startup but <em>not</em> the ResourcePackInfo warning described above, it means that you do not have <a :href="`https://spongepowered.org/downloads/spongeforge?minecraft=${version}&offset=0`">SpongeForge</a> installed. You must download it and add it to your <code>mods</code> folder.</p>

    <h2 id="incompatible-mods">Incompatible Mods</h2>

    <p>Unfortunately, many modpacks bundle mods that are incompatible with SpongeForge, and thus incompatible with Crowd Control. Many of these incompatible mods are unnecessary or have known fixes that have yet to be added to the modpack. Remedies for some of these mods can be found below.</p>

    <h3>Unnecessary Mods</h3>

    <p>The following incompatible mods provide little functionality or provide functionality that is already provided by Sponge. <strong>If you have any of these in your modpack, you should delete them.</strong></p>

    <ul>
      <li>LazyDFU (performance mod that is bundled in Sponge)</li>
      <li>Observable (profiling mod that is not useful to 99.9% of players)</li>
    </ul>

    <h3>Outdated Mods</h3>

    <p>The following incompatible mods have known fixes that haven't been widely published. <strong>If you have one of these mods in your modpack, you should delete it and replace it with the fixed build.</strong></p>

    <ul>
      <li v-if="version === '1.16.5'">
        Abnormals Core:
        <a href="https://cdn.discordapp.com/attachments/406987481825804290/949798054117122058/abnormals_core-1.16.5-3.3.1.jar">Fixed</a>
        by Sponge developers
        (<a href="https://github.com/team-abnormals/blueprint/commit/df4932960266f2e30a541097811193c17d1bb339">source</a>)
      </li>
      <li v-else><i>(No known mods for the selected version.)</i></li>
    </ul>

    <h3>Mods with No Known Fix</h3>

    <p>The following mods have not had a fix developed for them yet or are closed source and thus unlikely to ever have a fix developed. You will not be able to play Crowd Control with these mods.</p>

    <ul>
      <li>Quests Additions (closed source)</li>
    </ul>

    <h3>Other Mods</h3>

    <p>If you're having issues with a mod not listed here, you may also want to check out Sponge's own list of compatibility issues <a :href="`https://docs.spongepowered.org/${docs}/en/server/spongineer/incompatible.html`">here</a>.</p>
  </div>
</template>