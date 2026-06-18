<template>
  <div v-if="item" class="space-y-5">
    <section class="liquid overflow-hidden rounded-[28px]">
      <img :src="realAttractionImage(item)" class="h-[360px] w-full object-cover" @error="fallbackImage($event, item)" />
      <div class="p-6">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 class="m-0 text-3xl">{{ item.name }}</h1>
            <p class="mt-2 text-[var(--muted)]">{{ item.province }} · {{ item.city }} · {{ item.bestSeason }}</p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <n-button round @click="toggleFavorite">{{ favorited ? '已收藏' : '收藏' }}</n-button>
            <div class="rounded-2xl bg-white/50 px-4 py-2 dark:bg-white/10">{{ item.score }} 分</div>
          </div>
        </div>
        <p class="mt-5 leading-8">{{ item.description }}</p>
        <div class="mt-4 grid gap-3 md:grid-cols-3">
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">门票：￥{{ item.price }}</div>
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">开放：{{ item.openTime }}</div>
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/6">标签：{{ item.tags }}</div>
        </div>
      </div>
    </section>

    <section class="liquid rounded-[28px] p-5">
      <h2 class="m-0 mb-4 text-2xl">位置</h2>
      <AmapView :longitude="item.longitude" :latitude="item.latitude" :title="item.name" />
    </section>

    <SocialRecommendations
      title="热门打卡与美食攻略"
      :place="item.name"
      :city="item.city"
      scene="attraction"
    />

    <section class="liquid rounded-[28px] p-5">
      <h2 class="m-0 mb-4 text-2xl">附近酒店推荐</h2>
      <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <article v-for="hotel in hotels" :key="hotel.id" class="overflow-hidden rounded-[24px] bg-white/45 dark:bg-white/6">
          <img :src="hotel.cover" class="h-40 w-full cursor-pointer object-cover" @click="goHotelDetail(hotel)" @error="fallbackHotelImage($event, hotel)" />
          <div class="p-4">
            <h3 class="m-0 cursor-pointer" @click="goHotelDetail(hotel)">{{ hotel.name }}</h3>
            <p class="text-sm text-[var(--muted)]">{{ hotel.address }}</p>
            <div class="mt-2 flex justify-between text-sm"><span>￥{{ hotel.price }}</span><span>{{ hotel.score }} 分</span></div>
            <div class="mt-3 flex gap-2">
              <n-button size="small" round @click="goHotelDetail(hotel)">详情</n-button>
              <n-button size="small" round @click="book(hotel)">预订</n-button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import AmapView from '../components/AmapView.vue'
import SocialRecommendations from '../components/SocialRecommendations.vue'
import { http, type Attraction, type Hotel } from '../api'
import { loadRealAttractionImages, realAttractionImage } from '../realAttractionImages'
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'
import { loadOnlineAttraction, saveOnlineHotel } from '../onlineDetail'
import { removeFavoriteCard, saveFavoriteCard } from '../favoriteCache'

const route = useRoute()
const router = useRouter()
const toast = useMessage()
const item = ref<Attraction>()
const hotels = ref<Hotel[]>([])
const favorited = ref(false)

onMounted(async () => {
  const id = String(route.params.id)
  if (id.startsWith('online-')) {
    item.value = loadOnlineAttraction(id.replace('online-', ''))
    if (!item.value) return
    await afterLoaded()
    hotels.value = await loadOnlineHotels(item.value.city)
    return
  }
  item.value = await http.get<Attraction>(`/api/attractions/${id}`)
  await loadRealAttractionImages([item.value])
  await afterLoaded()
  hotels.value = await loadOnlineHotels(item.value.city)
})

async function afterLoaded() {
  await recordHistory()
  await loadFavoriteState()
}

async function loadOnlineHotels(city?: string) {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) return []
  try {
    const results = await http.get<Hotel[]>('/api/hotels/online', {
      params: { city, keyword: city ? `${city}酒店` : '酒店', key, limit: 6 },
    })
    return results.map(item => ({ ...item, source: 'online' as const }))
  } catch {
    return []
  }
}

function goHotelDetail(hotel: Hotel) {
  saveOnlineHotel(hotel)
  router.push(`/hotels/online-${hotel.id}`)
}

function book(hotel: Hotel) {
  const keyword = encodeURIComponent(hotel.name)
  window.open(`https://hotels.ctrip.com/hotels/list?keyword=${keyword}`, '_blank', 'noreferrer')
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
    const rows = await http.get<any[]>('/api/favorites', { params: { type: 'ATTRACTION' } })
    favorited.value = rows.some(row => Number(row.targetId) === Number(item.value?.id))
  } catch {}
}

async function toggleFavorite() {
  if (!item.value) return
  if (!localStorage.getItem('token')) {
    toast.warning('请先登录后收藏')
    return
  }
  try {
    if (favorited.value) {
      await http.delete('/api/favorites', { params: { targetId: item.value.id, targetType: 'ATTRACTION' } })
      removeFavoriteCard('ATTRACTION', item.value.id)
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

function cardPayload() {
  const path = String(route.params.id).startsWith('online-') ? `/attractions/online-${item.value!.id}` : `/attractions/${item.value!.id}`
  return {
    targetId: item.value!.id,
    targetType: 'ATTRACTION',
    title: item.value!.name,
    cover: realAttractionImage(item.value!),
    path,
  }
}

function fallbackImage(e: Event, item: Attraction) {
  item.coverImage = placeImagePlaceholder(item)
  fallbackPlaceImage(e, item)
  void recordHistory()
}

function fallbackHotelImage(e: Event, hotel: Hotel) {
  fallbackPlaceImage(e, hotel)
}
</script>
