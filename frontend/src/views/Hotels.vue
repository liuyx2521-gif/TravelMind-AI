<template>
  <div class="space-y-5">
    <div class="liquid flex flex-wrap gap-3 rounded-[28px] p-4">
      <n-input v-model:value="city" class="max-w-xs" placeholder="城市，例如三亚" clearable @keydown.enter="loadOnline" />
      <n-input v-model:value="keyword" class="max-w-xs" placeholder="关键词，例如海景 / 亲子 / 市中心" clearable @keydown.enter="loadOnline" />
      <n-button type="primary" round :loading="onlineLoading" @click="loadOnline">联网搜索</n-button>
    </div>

    <div class="rounded-2xl bg-white/55 px-4 py-3 text-sm text-[var(--muted)] dark:bg-white/8">
      {{ tip }}
    </div>

    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <article v-for="item in list" :key="item.id" class="liquid overflow-hidden rounded-[28px] transition hover:-translate-y-1">
        <img :src="hotelImage(item)" class="h-44 w-full cursor-pointer object-cover" @click="goDetail(item)" @load="ensureImage($event, item)" @error="fallbackImage($event, item)" />
        <div class="p-4">
          <h2 class="m-0 cursor-pointer text-xl" @click="goDetail(item)">{{ item.name }}</h2>
          <p class="line-clamp-2 text-sm text-[var(--muted)]">{{ item.address }}</p>
          <div class="mt-3 flex justify-between">
            <span>{{ priceText(item) }}</span>
            <span>{{ item.score || '暂无' }} 分</span>
          </div>
          <div class="mt-3 flex gap-2">
            <n-button size="small" round @click="goDetail(item)">详情</n-button>
            <n-button size="small" round @click="book(item)">预订</n-button>
            <n-button size="small" round disabled>实时数据</n-button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import AMapLoader from '@amap/amap-jsapi-loader'
import { http, type Hotel } from '../api'
import { poiLatitude, poiLongitude, poiPhoto, staticMap, type AmapPoi } from '../amapPoi'
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'
import { hotelBookingUrl, hotelPriceText, normalizeHotelPrice } from '../hotelPrice'
import { hotelPoiStableId, hotelSearchKeywords, sortRegularHotelPois } from '../hotelSearch'
import { saveOnlineHotel } from '../onlineDetail'

const city = ref('杭州')
const keyword = ref('')
const list = ref<Hotel[]>([])
const onlineLoading = ref(false)
const tip = ref('在线酒店')
const toast = useMessage()
const router = useRouter()
let searchController: AbortController | undefined
let searchSeq = 0

async function loadOnline() {
  searchController?.abort()
  const seq = ++searchSeq
  searchController = new AbortController()
  onlineLoading.value = true
  tip.value = '正在从高德实时搜索酒店...'
  try {
    let results: Hotel[]
    try {
      results = await searchOnlineByBackend(searchController.signal)
    } catch (error) {
      if ((error as Error).name === 'CanceledError' || (error as Error).name === 'AbortError') throw error
      if (!String((error as Error).message).includes('USERKEY_PLAT_NOMATCH')) throw error
      results = await searchOnlineByJs()
    }
    if (seq !== searchSeq) return
    list.value = results
    tip.value = results.length ? `高德实时找到 ${results.length} 家酒店。` : '没有搜到在线酒店。'
  } catch (error) {
    if ((error as Error).name === 'CanceledError' || (error as Error).name === 'AbortError') return
    toast.error((error as Error).message)
    tip.value = '高德联网搜索失败，请检查高德 Key 或网络。'
  } finally {
    if (seq === searchSeq) onlineLoading.value = false
  }
}

async function searchOnlineByBackend(signal?: AbortSignal): Promise<Hotel[]> {
  return (await http.get<Hotel[]>('/api/hotels/online', {
    params: { city: city.value, keyword: `${city.value}正规酒店 ${keyword.value}`.trim() || '正规酒店', limit: 48 },
    signal,
  })).map((item, index) => ({ ...item, price: normalizeHotelPrice(item.price, item, city.value, index), source: 'online' }))
}

async function searchOnlineByJs(): Promise<Hotel[]> {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) throw new Error('缺少 VITE_AMAP_KEY')
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
  if (securityJsCode) (window as any)._AMapSecurityConfig = { securityJsCode }
  const AMap = await AMapLoader.load({ key, version: '2.0', plugins: ['AMap.PlaceSearch'] })
  const placeSearch = new AMap.PlaceSearch({
    city: city.value || undefined,
    citylimit: !!city.value,
    type: '住宿服务',
    extensions: 'all',
    pageSize: 20,
    pageIndex: 1,
  })
  const hotelKeyword = `${city.value}正规酒店 ${keyword.value}`.trim() || '正规酒店'
  const results = await Promise.allSettled(hotelSearchKeywords(city.value, hotelKeyword).map(word => new Promise<AmapPoi[]>((resolve, reject) => {
    placeSearch.search(word, (status: string, res: any) => {
      if (status === 'complete' && res?.poiList?.pois) resolve(res.poiList.pois)
      else reject(new Error(res?.info || '高德酒店搜索失败'))
    })
  })))
  const result = sortRegularHotelPois(results.flatMap(item => item.status === 'fulfilled' ? item.value : [])).slice(0, 48)
  return result.map((poi, index) => ({
    id: hotelPoiStableId(poi, index, city.value),
    name: poi.name,
    city: poi.cityname || city.value || '',
    address: poi.address || poi.type || '',
    price: normalizeHotelPrice(poi.biz_ext?.cost, { name: poi.name, city: poi.cityname || city.value || '', price: 0 }, city.value, index),
    score: Number(poi.biz_ext?.rating || 0),
    cover: poiPhoto(poi) || staticMap(poi.location, key),
    longitude: poiLongitude(poi.location),
    latitude: poiLatitude(poi.location),
    source: 'online',
  }))
}

function goDetail(item: Hotel) {
  saveOnlineHotel(item)
  router.push(`/app/hotels/online-${item.id}`)
}

function book(item: Hotel) {
  window.open(hotelBookingUrl(item, city.value), '_blank', 'noreferrer')
}

function fallbackImage(e: Event, item: Hotel) {
  item.cover = placeImagePlaceholder(item)
  fallbackPlaceImage(e, item)
}

function ensureImage(e: Event, item: Hotel) {
  const img = e.target as HTMLImageElement
  if (img.naturalWidth < 8 || img.naturalHeight < 8) fallbackImage(e, item)
}

function hotelImage(item: Hotel) {
  return validImage(item.cover) || placeImagePlaceholder(item)
}

function priceText(item: Hotel) {
  return hotelPriceText(item, city.value)
}

function validImage(url?: string) {
  if (!url || !url.trim()) return ''
  if (url.startsWith('data:image/')) return url
  if (!/^https?:\/\//i.test(url)) return ''
  if (url.includes('/v3/staticmap')) return ''
  return url
}

onMounted(loadOnline)
</script>
