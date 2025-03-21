// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  //devtools: { enabled: true },
  app: {
    head: {
      link: [
        {
          rel: 'preconnect',
          href: 'https://fonts.googleapis.com'
        },
        {
          rel: 'preconnect',
          href: 'https://fonts.gstatic.com',
          crossorigin: ''
        },
        {
          rel: 'stylesheet',
          href: 'https://fonts.googleapis.com/css2?family=Poppins:wght@400;700&display=swap'
        },
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
      meta: [
        {
          name: 'theme-color',
          content: '#F5E339'
        }
      ]
    },
  },

  routeRules: {
    '/setup': { ssr: false },
  },

  modules: ['@nuxt/ui'],
  compatibilityDate: '2024-08-27',
})