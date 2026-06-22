<template>
  <div class="relative h-80 w-full overflow-hidden rounded-[28px] bg-black/8 dark:bg-white/8">
    <div v-show="mode === 'amap'" ref="el" class="h-full w-full"></div>
    <a
      v-if="mode === 'fallback'"
      class="block h-full w-full"
      :href="fallbackUrl"
      target="_blank"
      rel="noreferrer"
      :title="title"
    >
      <img :src="fallbackImage" :alt="title" class="h-full w-full object-cover" />
      <div class="absolute inset-x-4 bottom-4 rounded-2xl bg-black/55 p-3 text-white backdrop-blur-md">
        <div class="font-700">{{ title }}</div>
        <div class="text-sm opacity-85">
          点击在地图中打开 · {{ latitude }}, {{ longitude }}
          <span v-if="userLocation"> · 已获取当前位置</span>
        </div>
      </div>
    </a>
    <div v-if="mode === 'loading'" class="flex h-full items-center justify-center text-[var(--muted)]">
      地图加载中...
    </div>
    <div class="absolute left-4 top-4 flex flex-wrap items-center gap-2">
      <button
        class="rounded-2xl bg-black/58 px-3 py-2 text-sm text-white backdrop-blur-md transition hover:bg-black/72"
        type="button"
        @click="requestLocation"
      >
        获取我的位置
      </button>
      <div v-if="locationTip" class="rounded-2xl bg-black/55 px-3 py-2 text-sm text-white backdrop-blur-md">
        {{ locationTip }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AMapLoader from '@amap/amap-jsapi-loader'
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { amapMarkerUrl } from '../externalLinks'

const props = defineProps<{
  longitude: number
  latitude: number
  title?: string
}>()

const el = ref<HTMLDivElement>()
const mode = ref<'loading' | 'amap' | 'fallback'>('loading')
const locationTip = ref('')
const userLocation = ref<{ longitude: number; latitude: number }>()
const title = computed(() => props.title || '目的地位置')
const fallbackUrl = computed(() => amapMarkerUrl(props.longitude, props.latitude, title.value))
const fallbackImage = computed(() => `https://staticmap.openstreetmap.de/staticmap.php?center=${props.latitude},${props.longitude}&zoom=13&size=900x420&markers=${props.latitude},${props.longitude},red-pushpin`)
let map: any
let AMapRef: any
let targetMarker: any
let userMarker: any
let geolocation: any

async function requestLocation() {
  const current = map && AMapRef ? await getAmapLocation() : await getBrowserLocation()
  if (current && map && AMapRef) {
    userMarker?.setMap?.(null)
    userMarker = new AMapRef.Marker({
      map,
      position: [current.longitude, current.latitude],
      title: '我的位置',
      label: { content: '我的位置', direction: 'top' },
    })
    map.setFitView([targetMarker, userMarker].filter(Boolean), false, [48, 48, 48, 48])
  }
}

function getAmapLocation() {
  return new Promise<{ longitude: number; latitude: number } | undefined>(resolve => {
    if (!geolocation) {
      resolve(undefined)
      return
    }
    locationTip.value = '正在获取你的位置...'
    geolocation.getCurrentPosition((status: string, result: any) => {
      if (status === 'complete' && result?.position) {
        const position = result.position
        const location = {
          longitude: Number(position.lng ?? position.getLng?.()),
          latitude: Number(position.lat ?? position.getLat?.()),
        }
        userLocation.value = location
        locationTip.value = '已获取你的位置'
        window.setTimeout(() => (locationTip.value = ''), 2200)
        resolve(location)
        return
      }
      locationTip.value = result?.message || '定位失败，请检查浏览器定位权限'
      window.setTimeout(() => (locationTip.value = ''), 3000)
      resolve(undefined)
    })
  })
}

function getBrowserLocation() {
  return new Promise<{ longitude: number; latitude: number } | undefined>(resolve => {
    if (!navigator.geolocation) {
      locationTip.value = '当前浏览器不支持定位'
      resolve(undefined)
      return
    }
    locationTip.value = '正在获取你的位置...'
    navigator.geolocation.getCurrentPosition(
      position => {
        const location = {
          longitude: position.coords.longitude,
          latitude: position.coords.latitude,
        }
        userLocation.value = location
        locationTip.value = '已获取你的位置'
        window.setTimeout(() => (locationTip.value = ''), 2200)
        resolve(location)
      },
      () => {
        locationTip.value = '未获得定位权限，请检查浏览器地址栏权限设置'
        window.setTimeout(() => (locationTip.value = ''), 3000)
        resolve(undefined)
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 },
    )
  })
}

async function initMap() {
  mode.value = 'loading'
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) {
    mode.value = 'fallback'
    locationTip.value = '当前使用备用地图，点击按钮可请求定位'
    return
  }

  try {
    const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
    if (securityJsCode) {
      ;(window as any)._AMapSecurityConfig = { securityJsCode }
    }
    const AMap = await AMapLoader.load({
      key,
      version: '2.0',
      plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.Geolocation'],
    })
    AMapRef = AMap
    mode.value = 'amap'
    await nextTick()
    map?.destroy?.()
    map = new AMap.Map(el.value, {
      zoom: 13,
      center: [props.longitude, props.latitude],
    })
    map.addControl(new AMap.Scale())
    map.addControl(new AMap.ToolBar())
    geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 60000,
      convert: true,
      showButton: false,
      showMarker: false,
      panToLocation: false,
      zoomToAccuracy: false,
    })
    map.addControl(geolocation)
    targetMarker = new AMap.Marker({
      map,
      position: [props.longitude, props.latitude],
      title: title.value,
      label: { content: title.value, direction: 'top' },
    })
    locationTip.value = '点击“获取我的位置”授权定位'
    window.setTimeout(() => (locationTip.value = ''), 3000)
  } catch {
    mode.value = 'fallback'
    locationTip.value = '高德地图加载失败，已切换备用地图'
    window.setTimeout(() => (locationTip.value = ''), 3000)
  }
}

onMounted(initMap)

watch(() => [props.longitude, props.latitude], initMap)
</script>
