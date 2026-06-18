<template>
  <div class="space-y-5">
    <section class="liquid flex flex-wrap items-center gap-5 rounded-[28px] p-5">
      <img :src="avatarSrc" class="h-20 w-20 rounded-full object-cover ring-4 ring-white/40" @error="fallbackAvatar" />
      <div class="min-w-0 flex-1">
        <h1 class="m-0 text-2xl">个人中心</h1>
        <p class="m-0 mt-1 text-[var(--muted)]">{{ user.user?.username || '未加载' }}</p>
      </div>
      <div class="flex flex-wrap gap-3">
        <n-upload :custom-request="uploadAvatar" :show-file-list="false">
          <n-button round>上传头像</n-button>
        </n-upload>
        <n-button round @click="logout">退出登录</n-button>
      </div>
    </section>

    <section class="liquid rounded-[28px] p-5">
      <div class="mb-4 flex items-end justify-between">
        <div>
          <h2 class="m-0 text-2xl">收藏</h2>
          <p class="m-0 mt-1 text-sm text-[var(--muted)]">{{ favoriteCards.length }} 个收藏</p>
        </div>
      </div>
      <div v-if="favoriteCards.length" class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        <article v-for="item in favoriteCards" :key="item.key" class="group cursor-pointer overflow-hidden rounded-[18px] bg-white/45 transition hover:-translate-y-1 dark:bg-white/6" @click="openCard(item)">
          <div class="aspect-[3/4] overflow-hidden bg-black/8">
            <img :src="item.cover" :alt="item.title" class="h-full w-full object-cover transition duration-300 group-hover:scale-105" @error="fallbackCardImage" />
          </div>
          <div class="p-2.5">
            <h3 class="m-0 line-clamp-2 text-sm font-700 leading-5">{{ item.title }}</h3>
            <div class="mt-1 flex items-center justify-between gap-2 text-xs text-[var(--muted)]">
              <span>{{ typeLabel(item.type) }}</span>
              <span v-if="item.meta">{{ item.meta }}</span>
            </div>
          </div>
        </article>
      </div>
      <div v-else class="rounded-2xl bg-white/40 p-5 text-center text-[var(--muted)] dark:bg-white/6">还没有收藏</div>
    </section>

    <section class="liquid rounded-[28px] p-5">
      <div class="mb-4 flex items-end justify-between">
        <div>
          <h2 class="m-0 text-2xl">浏览历史</h2>
          <p class="m-0 mt-1 text-sm text-[var(--muted)]">{{ historyCards.length }} 条记录</p>
        </div>
        <n-button v-if="historyCards.length" size="small" round @click="clearHistory">清空</n-button>
      </div>
      <div v-if="historyCards.length" class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6">
        <article v-for="item in historyCards" :key="item.key" class="group cursor-pointer overflow-hidden rounded-[18px] bg-white/45 transition hover:-translate-y-1 dark:bg-white/6" @click="openCard(item)">
          <div class="aspect-square overflow-hidden bg-black/8">
            <img :src="item.cover" :alt="item.title" class="h-full w-full object-cover transition duration-300 group-hover:scale-105" @error="fallbackCardImage" />
          </div>
          <div class="p-2.5">
            <h3 class="m-0 line-clamp-2 text-sm font-700 leading-5">{{ item.title }}</h3>
            <p class="m-0 mt-1 text-xs text-[var(--muted)]">{{ typeLabel(item.type) }}</p>
          </div>
        </article>
      </div>
      <div v-else class="rounded-2xl bg-white/40 p-5 text-center text-[var(--muted)] dark:bg-white/6">暂无浏览历史</div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { http } from '../api'
import { useUserStore } from '../stores/user'
import { placeImagePlaceholder } from '../imageFallback'
import { getFavoriteCard } from '../favoriteCache'

const user = useUserStore()
const router = useRouter()
const toast = useMessage()
const favorites = ref<any[]>([])
const history = ref<any[]>([])
const favoriteCards = ref<CardItem[]>([])
const historyCards = ref<CardItem[]>([])
const avatarVersion = ref(Date.now())
const cardFallback = placeImagePlaceholder({ title: 'TravelMind' })
const hotelFallback = placeImagePlaceholder({ title: 'Hotel' })
const avatarFallback = placeImagePlaceholder({ title: 'Avatar' })
const avatarSrc = computed(() => {
  if (!user.user?.avatar) return avatarFallback
  return `${user.user.avatar}${user.user.avatar.includes('?') ? '&' : '?'}t=${avatarVersion.value}`
})

type CardItem = {
  key: string
  id: number
  type: string
  title: string
  cover: string
  meta?: string
  path?: string
}

function logout() {
  user.logout()
  router.push('/')
}

async function uploadAvatar({ file, onFinish, onError }: any) {
  const form = new FormData()
  form.append('file', file.file)
  try {
    const data = await http.post<{ url: string }>('/api/files/upload', form)
    await http.put('/api/users/me', { avatar: data.url })
    await user.fetchMe()
    avatarVersion.value = Date.now()
    onFinish()
    toast.success('头像已更新')
  } catch (e) {
    onError()
    toast.error((e as Error).message)
  }
}

async function loadFavorites() {
  favorites.value = await http.get('/api/favorites')
  const cards = await Promise.all(favorites.value.map(toFavoriteCard))
  favoriteCards.value = uniqueCards(cards.filter(Boolean) as CardItem[])
}

async function toFavoriteCard(item: any): Promise<CardItem | undefined> {
  const cached = getFavoriteCard(item.targetType, Number(item.targetId))
  if (item.title || item.cover || item.path || cached) {
    return card(
      item,
      item.title || cached?.title || `${typeLabel(item.targetType)} #${item.targetId}`,
      item.cover || cached?.cover,
      '',
      item.path || cached?.path || pathByType(item.targetType, item.targetId),
    )
  }
  try {
    if (item.targetType === 'ATTRACTION') {
      const data = await http.get<any>(`/api/attractions/${item.targetId}`)
      return card(item, data.name, data.coverImage, `${data.city} · ${data.score}分`, `/attractions/${data.id}`)
    }
    if (item.targetType === 'HOTEL') {
      const data = await http.get<any>(`/api/hotels/${item.targetId}`)
      return card(item, data.name, data.cover || hotelFallback, `￥${data.price} · ${data.score}分`, `/hotels/${data.id}`)
    }
    if (item.targetType === 'NOTE') {
      const data = await http.get<any>(`/api/notes/${item.targetId}`)
      return card(item, data.title, data.cover, `${data.likeCount || 0}赞`, '/notes')
    }
  } catch {}
}

function loadHistoryCards() {
  historyCards.value = uniqueCards(history.value.map(item => ({
    key: `history-${item.id}`,
    id: item.targetId,
    type: item.targetType,
    title: item.title || `${typeLabel(item.targetType)} #${item.targetId}`,
    cover: item.cover || fallbackByType(item.targetType),
    path: item.path || pathByType(item.targetType, item.targetId),
  })))
}

function uniqueCards(cards: CardItem[]) {
  const seen = new Set<string>()
  return cards.filter(item => {
    const key = `${item.type}-${item.title || item.id}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

function card(item: any, title: string, cover: string, meta: string, path: string) {
  return {
    key: `favorite-${item.id}`,
    id: item.targetId,
    type: item.targetType,
    title,
    cover: cover || fallbackByType(item.targetType),
    meta,
    path,
  }
}

function typeLabel(type: string) {
  return ({ ATTRACTION: '景点', HOTEL: '酒店', NOTE: '游记' } as Record<string, string>)[type] || type
}

function fallbackByType(type: string) {
  return type === 'HOTEL' ? hotelFallback : cardFallback
}

function pathByType(type: string, id: number) {
  if (type === 'ATTRACTION') return `/attractions/${id}`
  if (type === 'HOTEL') return `/hotels/${id}`
  if (type === 'NOTE') return '/notes'
  return undefined
}

function openCard(item: CardItem) {
  if (item.path) router.push(item.path)
}

function fallbackCardImage(e: Event) {
  ;(e.target as HTMLImageElement).src = cardFallback
}

function fallbackAvatar(e: Event) {
  ;(e.target as HTMLImageElement).src = avatarFallback
}

async function clearHistory() {
  await http.delete('/api/history')
  history.value = []
  historyCards.value = []
}

onMounted(async () => {
  await user.fetchMe()
  avatarVersion.value = Date.now()
  await loadFavorites()
  history.value = await http.get('/api/history')
  loadHistoryCards()
})
</script>
