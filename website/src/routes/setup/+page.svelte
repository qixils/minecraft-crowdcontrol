<script lang="ts">
    import {allVersions} from "$lib";

    let question: number = 0;
    let singleplayer: boolean | undefined;
    let version_index: number | undefined;
    let modloader_index: number | undefined;

    let version: string | undefined;
    $: if (version_index !== undefined) { version = allVersions[version_index]; }

    let modloaders: string[] = ["Unsure", "Paper", "Fabric", "Sponge", "Forge"];
    let modloader: string | undefined;
    $: if (modloader_index !== undefined) { modloader = modloaders[modloader_index]; }

    function set(value: any) {
        switch (question) {
            case 0:
                singleplayer = value;
                break;
            case 1:
                version_index = value;
                break;
            case 2:
                modloader_index = value;
                break;
        }
        question++;
    }
</script>

<h1>Dynamic Setup Guide</h1>

<p>
    This is the dynamic setup guide for Minecraft Crowd Control. If you're not sure what that is, you should
    <a href="/">read this first</a>.
</p>

<hr>

{#if question === 0}
    <p>Are you intending to play singleplayer or multiplayer?</p>
    <div class="buttons">
        <button on:click={() => set(true)}>Singleplayer</button>
        <button on:click={() => set(false)}>Multiplayer</button>
    </div>
{:else if question === 1}
    <p>What version of Minecraft are you playing?</p>
    <div class="buttons">
        {#each allVersions as v, i}
            <button on:click={() => set(i)}>
                {#if i === 0}
                    Latest ({v})
                {:else}
                    {v}
                {/if}
            </button>
        {/each}
    </div>
{:else if question === 2}
    <p>What modloader are you using?</p>
    <div class="buttons">
        {#each modloaders as m, i}
            <button on:click={() => set(i)}>{m}</button>
        {/each}
    </div>
{:else}
    <p>
        <a href="/setup?singleplayer={singleplayer}&version={version}&modloader={modloader}">
            Click here to go to the setup page.
        </a>
    </p>
{/if}