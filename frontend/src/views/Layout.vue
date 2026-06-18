<template>
  <div class="min-h-screen px-4 py-5 md:px-8">
    <header class="mx-auto mb-6 flex max-w-6xl items-center justify-between">
      <router-link to="/" class="text-2xl font-700 tracking-tight">TravelMind AI</router-link>
      <nav class="liquid flex items-center gap-1 rounded-3xl p-1">
        <router-link
          v-for="item in items"
          :key="item.path"
          :to="item.path"
          class="rounded-2xl px-4 py-2 text-sm transition hover:bg-white/50 hover:text-[var(--text)] dark:hover:bg-white/12"
          :class="isActive(item.path)
            ? 'bg-white/50 text-[var(--text)] shadow-sm dark:bg-white/12'
            : 'text-[var(--muted)]'"
        >
          {{ item.name }}
        </router-link>
        <button class="rounded-2xl px-3 py-2 text-sm" @click="theme.toggle()">{{ theme.dark ? '浅色' : '深色' }}</button>
      </nav>
    </header>
    <main class="mx-auto max-w-6xl">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useThemeStore } from '../stores/theme'

const theme = useThemeStore()
const route = useRoute()
document.documentElement.classList.toggle('dark', theme.dark)

const items = [
  { name: 'AI规划', path: '/' },
  { name: '景点', path: '/attractions' },
  { name: '酒店', path: '/hotels' },
  { name: '游记', path: '/notes' },
  { name: 'AI工具', path: '/tools' },
  { name: '行程', path: '/plans' },
  { name: '我的', path: '/profile' },
]

function isActive(path: string) {
  return path === '/' ? route.path === '/' : route.path === path || route.path.startsWith(`${path}/`)
}
</script>
