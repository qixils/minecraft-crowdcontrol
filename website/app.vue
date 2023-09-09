<script setup lang="ts">
useHead({
  link: [
    {
      rel: 'icon',
      type: 'image/png',
      href: '/favicon-32.png',
      sizes: '32x32'
    },
    {
      rel: 'icon',
      type: 'image/png',
      href: '/favicon-192.png',
      sizes: '192x192'
    }
  ],
})
useServerSeoMeta({
  ogTitle: 'Minecraft Crowd Control',
  ogImage: '/logo-2048.png',
  ogImageAlt: 'Crowd Control logo',
  twitterCard: 'summary',
})
</script>

<template>
  <div>
    <NuxtLayout>
      <NuxtLoadingIndicator />
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<style>
  html {
    --bg-color: #151129;
    --fg-color: white;
    --accent-color: #ffec3b;
    --accent-text-color: black;
    --code-bg-color: rgba(100, 100, 100, 0.1);
    --code-fg-color: #F06478;
    --font-size-base: 1.125em;
    --font-base: Poppins,-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol;
    --leading-base: 1.65;
    --leading-tight: 1.1;

    background-color: var(--bg-color);
    color: var(--fg-color);
    font-family: var(--font-base);
    font-size: var(--font-size-base);
    line-height: var(--leading-base);
  }

  main {
    margin: 0 auto;
    max-width: 42em;
    padding: 0 1em;
  }

  code {
    background-color: var(--code-bg-color);
    border-radius: 0.75em;
    color: var(--code-fg-color);
    font-family: monospace;
    display: inline-block;
    line-height: var(--leading-tight);
    padding: 0.25em 0.5em;
  }

  a {
    color: var(--accent-color);
    text-decoration: none;
  }

  header, h1 {
    text-align: center;
  }

  .alert {
    background-color: var(--alert-bg-color, black);
    color: var(--alert-fg-color, white);
    padding: 0.75rem 1.25rem;
    margin: 1rem 0;
    border-radius: 0.25rem;
    box-shadow: var(--alert-bg-color, black) 0 0 0.4rem;
  }

  .alert-warning {
    --alert-bg-color: #fbbd23;
    --alert-fg-color: #382800;
  }

  .alert-danger {
    --alert-bg-color: #f87272;
    --alert-fg-color: #470000;
  }

  .alert-warning a, .alert-danger a {
    color: #ad1818;
  }

  .underline {
    text-decoration: underline;
  }

  @media (prefers-color-scheme: light) {
    html {
      --bg-color: #f5f5f5;
      --fg-color: #0d0a1a;
      --accent-color: #5d01c0;
      --accent-text-color: white;
      --code-bg-color: rgba(120, 120, 120, 0.1);
      --code-fg-color: #F61737;
    }
  }
</style>