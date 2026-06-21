<template>
  <div class="app-shell min-h-screen px-4 py-5 md:px-8">
    <div class="travel-backdrop" aria-hidden="true">
      <span v-for="item in decor" :key="item.text" class="travel-float" :style="item.style">{{ item.text }}</span>
    </div>

    <header class="relative z-2 mx-auto mb-5 flex max-w-6xl flex-wrap items-center justify-between gap-3">
      <router-link to="/app" class="brand-mark text-2xl font-800 tracking-tight">
        <span class="brand-orbit">✈</span>
        TravelMind AI
      </router-link>
      <nav class="liquid app-nav flex flex-wrap items-center gap-1 rounded-3xl p-1">
        <router-link
          v-for="item in items"
          :key="item.path"
          :to="item.path"
          class="nav-chip rounded-2xl px-4 py-2 text-sm transition"
          :class="isActive(item.path)
            ? 'bg-white/58 text-[var(--text)] shadow-sm dark:bg-white/12'
            : 'text-[var(--muted)]'"
        >
          <span class="mr-1">{{ item.icon }}</span>{{ item.name }}
        </router-link>
        <button class="theme-chip rounded-2xl px-3 py-2 text-sm" @click="theme.toggle()">{{ theme.dark ? '浅色' : '深色' }}</button>
      </nav>
    </header>

    <main class="app-main relative z-1 mx-auto max-w-6xl">
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
  { name: 'AI规划', path: '/app', icon: '✦' },
  { name: '景点', path: '/app/attractions', icon: '⌖' },
  { name: '酒店', path: '/app/hotels', icon: '▣' },
  { name: '车票', path: '/app/tickets', icon: '→' },
  { name: '游记', path: '/app/notes', icon: '◌' },
  { name: 'AI工具', path: '/app/tools', icon: '⌁' },
  { name: '行程', path: '/app/plans', icon: '◇' },
  { name: '我的', path: '/app/profile', icon: '◎' },
]

const decor = [
  { text: '✈', style: 'left:7%; top:13%; --delay:0s; --size:54px; --rot:-12deg;' },
  { text: '▧', style: 'left:87%; top:16%; --delay:1.4s; --size:46px; --rot:14deg;' },
  { text: '⌁', style: 'left:11%; top:72%; --delay:.8s; --size:58px; --rot:8deg;' },
  { text: '☁', style: 'left:78%; top:75%; --delay:2s; --size:60px; --rot:-8deg;' },
  { text: '⌖', style: 'left:47%; top:8%; --delay:2.7s; --size:42px; --rot:10deg;' },
  { text: '▱', style: 'left:92%; top:48%; --delay:3.2s; --size:50px; --rot:-18deg;' },
]

function isActive(path: string) {
  return path === '/app' ? route.path === '/app' : route.path === path || route.path.startsWith(`${path}/`)
}
</script>
