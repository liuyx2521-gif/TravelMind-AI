<template>
  <div class="space-y-5">
    <div class="liquid flex flex-wrap gap-3 rounded-[28px] p-4">
      <n-input v-model:value="keyword" class="max-w-xs" placeholder="搜索景点" clearable @keydown.enter="loadOnline" />
      <n-input v-model:value="city" class="max-w-xs" placeholder="城市，可不填" clearable @keydown.enter="loadOnline" />
      <n-button type="primary" round :loading="onlineLoading" @click="loadOnline">联网搜索</n-button>
    </div>

    <div class="rounded-2xl bg-white/55 px-4 py-3 text-sm text-[var(--muted)] dark:bg-white/8">
      {{ modeTip }}
    </div>

    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <article v-for="item in list" :key="item.id" class="liquid overflow-hidden rounded-[28px] transition hover:-translate-y-1">
        <img :src="realAttractionImage(item)" class="h-44 w-full cursor-pointer object-cover" @click="goDetail(item)" @error="fallbackImage($event, item)" />
        <div class="p-4">
          <div class="flex cursor-pointer items-center justify-between" @click="goDetail(item)">
            <h2 class="m-0 text-xl">{{ item.name }}</h2>
            <span class="text-amber-500">{{ item.score || '实时' }}</span>
          </div>
          <p class="line-clamp-2 text-sm text-[var(--muted)]">{{ item.description }}</p>
          <div class="mt-3 text-sm">
            {{ item.city || '高德 POI' }}<span v-if="item.tags"> · {{ item.tags }}</span>
            <span class="ml-2 rounded-full bg-emerald-500/15 px-2 py-0.5 text-xs text-emerald-600">联网</span>
          </div>
          <div class="mt-3 flex gap-2">
            <n-button size="small" round @click="select(item)">地图</n-button>
            <n-button size="small" round @click="goDetail(item)">详情</n-button>
            <n-button size="small" round disabled>实时数据</n-button>
          </div>
        </div>
      </article>
    </div>

    <n-modal v-model:show="mapVisible" preset="card" class="max-w-4xl" :title="selected?.name || '地图'">
      <AmapView
        v-if="selected"
        :key="selected.id"
        :longitude="selected.longitude"
        :latitude="selected.latitude"
        :title="selected.name"
      />
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import AMapLoader from '@amap/amap-jsapi-loader'
import AmapView from '../components/AmapView.vue'
import { http, type Attraction } from '../api'
import { realAttractionImage } from '../realAttractionImages'
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'
import { saveOnlineAttraction } from '../onlineDetail'
import { seasonalAttractionKeyword, seasonalAttractionQueries, seasonalAttractionTip } from '../seasonalTravel'

const keyword = ref(seasonalAttractionKeyword())
const city = ref('')
const list = ref<Attraction[]>([])
const selected = ref<Attraction>()
const mapVisible = ref(false)
const onlineLoading = ref(false)
const modeTip = ref(`${seasonalAttractionTip()} 数据来自高德实时联网 POI。`)
const toast = useMessage()
const router = useRouter()

async function loadOnline() {
  onlineLoading.value = true
  modeTip.value = `正在从高德实时搜索：${keyword.value || seasonalAttractionKeyword()}`
  try {
    list.value = isDefaultSeasonalSearch() ? await searchSeasonalRecommendations() : await searchOnline()
    modeTip.value = list.value.length
      ? `${seasonalAttractionTip()} 高德实时找到 ${list.value.length} 条景点结果。`
      : '没有搜到在线景点，可以换一个城市或关键词。'
  } catch (error) {
    modeTip.value = '高德联网搜索失败，请检查高德 Key 或网络。'
    toast.error((error as Error).message)
  } finally {
    onlineLoading.value = false
  }
}

async function searchOnline() {
  try {
    return await searchOnlineByBackend()
  } catch (error) {
    if (!String((error as Error).message).includes('USERKEY_PLAT_NOMATCH')) throw error
    return await searchOnlineByJs()
  }
}

async function searchSeasonalRecommendations() {
  const groups = await Promise.allSettled(
    seasonalAttractionQueries().map(query => searchOnlineByJs(query.keyword, query.city, 6))
  )
  const seen = new Set<string>()
  return groups.flatMap(group => group.status === 'fulfilled' ? group.value : [])
    .filter(item => {
      const key = `${item.name}-${item.city}`
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
    .slice(0, 24)
}

async function searchOnlineByBackend(): Promise<Attraction[]> {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) throw new Error('缺少 VITE_AMAP_KEY')
  const results = await http.get<Attraction[]>('/api/attractions/online', {
    params: { keyword: keyword.value || seasonalAttractionKeyword(), city: city.value, key, limit: 24 },
  })
  return results.map(item => ({ ...item, source: 'online' }))
}

async function searchOnlineByJs(searchKeyword = keyword.value || seasonalAttractionKeyword(), searchCity = city.value, pageSize = 24): Promise<Attraction[]> {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) throw new Error('缺少 VITE_AMAP_KEY')
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
  if (securityJsCode) (window as any)._AMapSecurityConfig = { securityJsCode }
  const AMap = await AMapLoader.load({ key, version: '2.0', plugins: ['AMap.PlaceSearch'] })
  const placeSearch = new AMap.PlaceSearch({
    city: searchCity || undefined,
    citylimit: !!searchCity,
    extensions: 'all',
    pageSize,
    pageIndex: 1,
  })
  const result = await new Promise<any[]>((resolve, reject) => {
    placeSearch.search(searchKeyword, (status: string, res: any) => {
      if (status === 'complete' && res?.poiList?.pois) resolve(res.poiList.pois)
      else reject(new Error(res?.info || '高德在线搜索失败'))
    })
  })
  return result.map((poi, index) => ({
    id: stablePoiId(poi, index, 'attraction'),
    name: poi.name,
    city: poi.cityname || searchCity || '',
    province: poi.pname || '',
    description: poi.address || poi.type || '高德实时景点',
    coverImage: poiPhoto(poi) || staticMap(poi.location, key),
    price: 0,
    bestSeason: '实时',
    openTime: '请以景区当天公告为准',
    score: Number(poi.biz_ext?.rating || 0),
    tags: poi.type || '高德 POI',
    longitude: poiLongitude(poi.location),
    latitude: poiLatitude(poi.location),
    source: 'online',
  }))
}

function isDefaultSeasonalSearch() {
  return !city.value.trim() && (!keyword.value.trim() || keyword.value.trim() === seasonalAttractionKeyword())
}

function select(item: Attraction) {
  selected.value = item
  mapVisible.value = true
}

function goDetail(item: Attraction) {
  item.coverImage = realAttractionImage(item)
  saveOnlineAttraction(item)
  router.push(`/app/attractions/online-${item.id}`)
}

function fallbackImage(e: Event, item: Attraction) {
  item.coverImage = placeImagePlaceholder(item)
  fallbackPlaceImage(e, item)
}

function poiPhoto(poi: any) {
  const photos = Array.isArray(poi.photos) ? poi.photos : []
  return photos.map((x: any) => x?.url || x).find((x: string) => typeof x === 'string' && x.startsWith('http')) || ''
}

function staticMap(location: any, key: string) {
  const text = poiLocationText(location)
  return text ? `https://restapi.amap.com/v3/staticmap?location=${text}&zoom=13&size=600*320&markers=mid,,A:${text}&key=${key}` : ''
}

function poiLocationText(location: any) {
  const longitude = poiLongitude(location)
  const latitude = poiLatitude(location)
  return longitude && latitude ? `${longitude},${latitude}` : ''
}

function poiLongitude(location: any) {
  return poiLocationParts(location).longitude
}

function poiLatitude(location: any) {
  return poiLocationParts(location).latitude
}

function poiLocationParts(location: any) {
  if (!location) return { longitude: 0, latitude: 0 }
  if (typeof location === 'string') {
    const [longitude, latitude] = location.split(',')
    return { longitude: Number(longitude || 0), latitude: Number(latitude || 0) }
  }
  return {
    longitude: Number(location.lng ?? location.getLng?.() ?? location.lnglat?.lng ?? location.lnglat?.getLng?.() ?? location[0] ?? 0),
    latitude: Number(location.lat ?? location.getLat?.() ?? location.lnglat?.lat ?? location.lnglat?.getLat?.() ?? location[1] ?? 0),
  }
}

function stablePoiId(poi: any, index: number, type: string) {
  const source = `${type}-${poi.id || poi.name || index}-${poi.cityname || city.value}-${poi.address || ''}`
  let hash = 0
  for (let i = 0; i < source.length; i++) hash = Math.imul(31, hash) + source.charCodeAt(i) | 0
  return Math.abs(hash)
}

onMounted(loadOnline)
</script>
