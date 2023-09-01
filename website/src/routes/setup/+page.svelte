<script lang="ts">
    let singleplayer: boolean | undefined;
</script>

<h1>Dynamic Setup Guide</h1>

<p>
    This is the dynamic setup guide for Minecraft Crowd Control. If you're not sure what that is, you should go
    <a href="/">here</a>.
</p>

<hr>

{#if singleplayer === undefined}
    <p>
        Are you intending to play singleplayer or multiplayer?
    </p>
    <button on:click={() => singleplayer = true}>Singleplayer</button>
    <button on:click={() => singleplayer = false}>Multiplayer</button>
{:else if singleplayer}

{:else}

{/if}