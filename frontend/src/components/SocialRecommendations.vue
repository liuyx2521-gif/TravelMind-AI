<template>
  <section class="liquid rounded-[28px] p-5">
    <div class="mb-4 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h2 class="m-0 text-2xl">{{ title }}</h2>
        <p class="m-0 mt-1 text-sm text-[var(--muted)]">聚合小红书、抖音和全网搜索，优先展示实时结果。</p>
      </div>
      <n-button round size="small" :loading="loading" @click="load">刷新</n-button>
    </div>

    <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
      <a
        v-for="item in items"
        :key="item.url"
        :href="item.url"
        target="_blank"
        rel="noreferrer"
        class="group rounded-[22px] bg-white/50 p-4 text-[var(--text)] no-underline shadow-sm transition hover:-translate-y-0.5 hover:bg-white/72 hover:shadow-lg dark:bg-white/7 dark:hover:bg-white/12"
      >
        <div class="mb-3 flex items-center justify-between gap-2">
          <span class="rounded-full px-2.5 py-1 text-xs font-700" :class="platformClass(item.platform)">
            {{ item.platform }}
          </span>
          <span class="text-xs text-[var(--muted)]">{{ item.type }}</span>
        </div>
        <h3 class="m-0 line-clamp-2 text-base leading-6">{{ item.title }}</h3>
        <p class="m-0 mt-3 line-clamp-2 text-sm leading-6 text-[var(--muted)]">{{ item.summary }}</p>
      </a>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { http } from '../api'

type SocialItem = {
  platform: string
  type: string
  title: string
  url: string
  summary: string
}

const props = defineProps<{
  title?: string
  place: string
  city?: string
  scene?: 'attraction' | 'hotel' | 'food'
}>()

const loading = ref(false)
const items = ref<SocialItem[]>([])

onMounted(load)
watch(() => [props.place, props.city, props.scene], load)

async function load() {
  if (!props.place) return
  loading.value = true
  try {
    items.value = await http.get<SocialItem[]>('/api/social/search', {
      params: { place: props.place, city: props.city, scene: props.scene || 'attraction' },
    })
  } catch {
    items.value = localFallback()
  } finally {
    loading.value = false
  }
}

function localFallback(): SocialItem[] {
  const keyword = encodeURIComponent(`${props.city || ''} ${props.place} 打卡 攻略 美食`.trim())
  return [
    { platform: '小红书', type: '搜索入口', title: '查看小红书最新图文攻略', url: `https://www.xiaohongshu.com/search_result?keyword=${keyword}`, summary: '跳转后可按热度、最新内容筛选。' },
    { platform: '抖音', type: '搜索入口', title: '查看抖音热门视频/探店', url: `https://www.douyin.com/search/${keyword}`, summary: '跳转后查看点赞高的视频内容。' },
    { platform: '全网', type: '搜索入口', title: '全网搜索旅行攻略', url: `https://www.bing.com/search?q=${keyword}`, summary: '用于补充更多实时信息。' },
  ]
}

function platformClass(platform: string) {
  if (platform === '小红书') return 'bg-red-500/12 text-red-600 dark:text-red-300'
  if (platform === '抖音') return 'bg-cyan-500/12 text-cyan-700 dark:text-cyan-300'
  return 'bg-[var(--button-bg)] text-[var(--button-text)]'
}
</script>
