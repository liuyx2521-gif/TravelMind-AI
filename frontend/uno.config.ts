import { defineConfig, presetAttributify, presetIcons, presetUno } from 'unocss'

export default defineConfig({
  presets: [presetUno(), presetAttributify(), presetIcons()],
  shortcuts: {
    'glass': 'border border-white/45 bg-white/58 dark:bg-white/8 shadow-xl backdrop-blur-2xl',
    'btn': 'rounded-2xl px-4 py-2 transition active:scale-98',
  },
})
