<template>
  <div v-if="item" class="space-y-5">
    <section class="liquid overflow-hidden rounded-[28px]">
      <img :src="item.cover" class="h-[360px] w-full object-cover" @error="fallbackImage($event, item)" />
      <div class="p-6">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 class="m-0 text-3xl">{{ item.name }}</h1>
            <p class="mt-2 text-[var(--muted)]">{{ item.city }} · {{ item.address }}</p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <n-button round @click="toggleFavorite">{{ favorited ? '已收藏' : '收藏' }}</n-button>
            <div class="rounded-2xl bg-white/50 px-4 py-2 dark:bg-white/10">{{ item.score }} 分</div>
          </div>
        </div>
        <div class="mt-5 grid gap-3 md:grid-cols-3">
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">价格：{{ priceText(item) }}</div>
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">城市：{{ item.city }}</div>
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">评分：{{ item.score }}</div>
        </div>
        <div class="mt-5 flex flex-wrap gap-3">
          <a
            v-for="link in bookingLinks"
            :key="link.name"
            :href="link.url"
            target="_blank"
            rel="noreferrer"
            @click="copyBookingKeyword(link)"
            class="rounded-2xl bg-[var(--button-primary-bg)] px-4 py-2 text-white shadow-lg transition hover:bg-[var(--button-primary-bg-hover)]"
          >
            {{ link.name }}
          </a>
        </div>
        <div class="mt-5">
          <h2 class="m-0 mb-4 text-2xl">酒店位置</h2>
          <AmapView :longitude="item.longitude" :latitude="item.latitude" :title="item.name" />
        </div>
      </div>
    </section>

    <SocialRecommendations
      title="入住体验与周边攻略"
      :place="item.name"
      :city="item.city"
      scene="hotel"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import AmapView from '../components/AmapView.vue'
import SocialRecommendations from '../components/SocialRecommendations.vue'
import { http, type Hotel } from '../api'
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'
import { ctripHotelUrl, fliggyHotelUrl, hotelBookingUrl, hotelPriceText, hotelSearchKeyword } from '../hotelPrice'
import { loadOnlineHotel } from '../onlineDetail'
import { removeFavoriteCard, saveFavoriteCard } from '../favoriteCache'

type BookingLink = {
  name: string
  url: string
  copyKeyword?: boolean
}

const route = useRoute()
const toast = useMessage()
const item = ref<Hotel>()
const favorited = ref(false)
const bookingLinks = computed<BookingLink[]>(() => {
  if (!item.value) return []
  return [
    { name: '当前酒店实时价', url: hotelBookingUrl(item.value) },
    { name: '携程搜索', url: ctripHotelUrl(item.value) },
    { name: '飞猪搜索', url: fliggyHotelUrl(item.value), copyKeyword: true },
  ]
})

onMounted(async () => {
  const id = String(route.params.id)
  if (id.startsWith('online-')) {
    item.value = loadOnlineHotel(id.replace('online-', ''))
    await afterLoaded()
    return
  }
  item.value = await http.get<Hotel>(`/api/hotels/${id}`)
  await afterLoaded()
})

async function afterLoaded() {
  if (!item.value) return
  await recordHistory()
  await loadFavoriteState()
}

async function recordHistory() {
  if (!item.value || !localStorage.getItem('token')) return
  try {
    await http.post('/api/history', cardPayload())
  } catch {}
}

async function loadFavoriteState() {
  if (!item.value || !localStorage.getItem('token')) return
  try {
    const rows = await http.get<any[]>('/api/favorites', { params: { type: 'HOTEL' } })
    favorited.value = rows.some(row => Number(row.targetId) === Number(item.value?.id))
  } catch {}
}

async function toggleFavorite() {
  if (!item.value) return
  if (!localStorage.getItem('token')) {
    toast.warning('登录后可以收藏酒店')
    return
  }
  try {
    if (favorited.value) {
      await http.delete('/api/favorites', { params: { targetId: item.value.id, targetType: 'HOTEL' } })
      removeFavoriteCard('HOTEL', item.value.id)
      favorited.value = false
      toast.success('已取消收藏')
    } else {
      const payload = cardPayload()
      await http.post('/api/favorites', payload)
      saveFavoriteCard(payload)
      favorited.value = true
      toast.success('已收藏')
    }
  } catch (e) {
    toast.error((e as Error).message)
  }
}

async function copyBookingKeyword(link: BookingLink) {
  if (!link.copyKeyword || !item.value || !navigator.clipboard) return
  try {
    await navigator.clipboard.writeText(hotelSearchKeyword(item.value))
    toast.success('已复制酒店名称，打开平台后可直接粘贴搜索')
  } catch {}
}

function cardPayload() {
  const path = String(route.params.id).startsWith('online-') ? `/app/hotels/online-${item.value!.id}` : `/app/hotels/${item.value!.id}`
  return {
    targetId: item.value!.id,
    targetType: 'HOTEL',
    title: item.value!.name,
    cover: item.value!.cover,
    path,
  }
}

function fallbackImage(e: Event, item: Hotel) {
  item.cover = placeImagePlaceholder(item)
  fallbackPlaceImage(e, item)
  void recordHistory()
}

function priceText(item: Hotel) {
  return hotelPriceText(item)
}
</script>
