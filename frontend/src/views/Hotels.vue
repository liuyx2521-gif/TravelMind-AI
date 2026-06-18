<template>
  <div class="space-y-5">
    <div class="liquid flex flex-wrap gap-3 rounded-[28px] p-4">
      <n-input v-model:value="city" class="max-w-xs" placeholder="城市，例如三亚" clearable @keydown.enter="loadOnline" />
      <n-button type="primary" round :loading="onlineLoading" @click="loadOnline">联网搜索</n-button>
    </div>

    <div class="rounded-2xl bg-white/55 px-4 py-3 text-sm text-[var(--muted)] dark:bg-white/8">
      {{ tip }}
    </div>

    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <article v-for="item in list" :key="item.id" class="liquid overflow-hidden rounded-[28px] transition hover:-translate-y-1">
        <img :src="item.cover" class="h-44 w-full cursor-pointer object-cover" @click="goDetail(item)" @error="fallbackImage($event, item)" />
        <div class="p-4">
          <h2 class="m-0 cursor-pointer text-xl" @click="goDetail(item)">{{ item.name }}</h2>
          <p class="line-clamp-2 text-sm text-[var(--muted)]">{{ item.address }}</p>
          <div class="mt-3 flex justify-between">
            <span>{{ item.price ? `￥${item.price}` : '价格待查询' }}</span>
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
import { fallbackPlaceImage, placeImagePlaceholder } from '../imageFallback'
import { saveOnlineHotel } from '../onlineDetail'

const city = ref('杭州')
const list = ref<Hotel[]>([])
const onlineLoading = ref(false)
const tip = ref('酒店数据来自高德实时联网 POI，本地酒店库不参与展示。')
const toast = useMessage()
const router = useRouter()

async function loadOnline() {
  onlineLoading.value = true
  tip.value = '正在从高德实时搜索酒店...'
  try {
    try {
      list.value = await searchOnlineByBackend()
    } catch (error) {
      if (!String((error as Error).message).includes('USERKEY_PLAT_NOMATCH')) throw error
      list.value = await searchOnlineByJs()
    }
    tip.value = list.value.length ? `高德实时找到 ${list.value.length} 家酒店。` : '没有搜到在线酒店。'
  } catch (error) {
    toast.error((error as Error).message)
    tip.value = '高德联网搜索失败，请检查高德 Key 或网络。'
  } finally {
    onlineLoading.value = false
  }
}

async function searchOnlineByBackend(): Promise<Hotel[]> {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) throw new Error('缺少 VITE_AMAP_KEY')
  return (await http.get<Hotel[]>('/api/hotels/online', {
    params: { city: city.value, keyword: city.value ? `${city.value}酒店` : '酒店', key, limit: 24 },
  })).map(item => ({ ...item, source: 'online' }))
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
    pageSize: 24,
    pageIndex: 1,
  })
  const result = await new Promise<any[]>((resolve, reject) => {
    placeSearch.search(city.value ? `${city.value}酒店` : '酒店', (status: string, res: any) => {
      if (status === 'complete' && res?.poiList?.pois) resolve(res.poiList.pois)
      else reject(new Error(res?.info || '高德酒店搜索失败'))
    })
  })
  return result.map((poi, index) => ({
    id: stablePoiId(poi, index, 'hotel'),
    name: poi.name,
    city: poi.cityname || city.value || '',
    address: poi.address || poi.type || '',
    price: Number(poi.biz_ext?.cost || 0),
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
  const keyword = encodeURIComponent(`${item.city} ${item.name}`)
  window.open(`https://hotels.ctrip.com/hotels/list?keyword=${keyword}`, '_blank', 'noreferrer')
}

function fallbackImage(e: Event, item: Hotel) {
  item.cover = placeImagePlaceholder(item)
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
