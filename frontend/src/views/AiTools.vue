<template>
  <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_370px]">
    <section class="space-y-5">
      <div class="liquid rounded-[28px] p-5 md:p-7">
        <div class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h1 class="m-0 text-3xl">旅行预算工作台</h1>
          </div>
          <div class="flex gap-2">
            <n-button round @click="resetPlanner">重新规划</n-button>
          </div>
        </div>
      </div>

      <div class="liquid rounded-[28px] p-4">
        <div class="flex flex-wrap items-center gap-2">
          <template v-for="(step, index) in steps" :key="step.key">
            <button
              class="rounded-2xl px-4 py-2 text-sm font-700 transition"
              :class="currentStep === index ? 'bg-[var(--button-primary-bg)] text-white shadow-lg' : 'bg-white/45 text-[var(--muted)] hover:bg-white/70 hover:text-[var(--text)] dark:bg-white/8 dark:hover:bg-white/14'"
              @click="currentStep = index"
            >
              <span class="mr-1">{{ step.icon }}</span>{{ step.title }}
            </button>
            <span v-if="index < steps.length - 1" class="text-[var(--muted)]">→</span>
          </template>
        </div>
      </div>

      <section v-if="currentStep === 0" class="liquid rounded-[28px] p-5">
        <div class="grid gap-4 md:grid-cols-4">
          <Field label="出发地">
            <n-input v-model:value="trip.origin" placeholder="杭州" />
          </Field>
          <Field label="目的地">
            <n-input v-model:value="trip.destination" placeholder="三亚" />
          </Field>
          <Field label="出发日期">
            <n-date-picker v-model:value="trip.date" class="w-full" type="date" clearable />
          </Field>
          <Field label="人数">
            <n-input-number v-model:value="trip.people" class="w-full" :min="1" :max="9" />
          </Field>
        </div>

        <div class="mt-5 grid gap-3 lg:grid-cols-3">
          <article
            v-for="ticket in ticketOptions"
            :key="ticket.id"
            class="cursor-pointer rounded-[24px] border p-4 transition hover:-translate-y-1"
            :class="selectedTicketId === ticket.id ? 'border-[var(--button-primary-bg)] bg-white/65 shadow-xl dark:bg-white/10' : 'border-white/20 bg-white/40 dark:bg-white/6'"
            @click="selectedTicketId = ticket.id"
          >
            <div class="flex items-center justify-between gap-3">
              <h3 class="m-0 text-2xl">{{ ticket.trainNo }}</h3>
              <span class="rounded-full bg-white/60 px-3 py-1 text-xs dark:bg-white/10">{{ ticket.seat }}</span>
            </div>
            <div class="mt-4 flex items-center justify-between gap-3">
              <div>
                <div class="text-2xl font-800">{{ ticket.depart }}</div>
                <div class="text-sm text-[var(--muted)]">{{ trip.origin }}</div>
              </div>
              <div class="text-center text-sm text-[var(--muted)]">
                <div>{{ ticket.duration }}</div>
                <div class="mt-1 h-px w-20 bg-[var(--muted)]/35"></div>
                <div class="mt-1">单程</div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-800">{{ ticket.arrive }}</div>
                <div class="text-sm text-[var(--muted)]">{{ trip.destination }}</div>
              </div>
            </div>
            <div class="mt-4 flex items-center justify-between">
              <span class="text-2xl font-800 text-[var(--button-primary-bg)]">￥{{ ticket.price }}</span>
              <n-button size="small" round @click.stop="open12306(ticket)">按当前路线订票</n-button>
            </div>
          </article>
        </div>

        <div class="mt-5 flex justify-end">
          <n-button type="primary" round @click="currentStep = 1">下一步：天数与日程</n-button>
        </div>
      </section>

      <section v-else-if="currentStep === 1" class="liquid rounded-[28px] p-5">
        <div class="grid gap-4 md:grid-cols-3">
          <Field label="出行天数">
            <n-input-number v-model:value="trip.days" class="w-full" :min="1" :max="15" />
          </Field>
          <Field label="每日餐饮预算">
            <n-input-number v-model:value="trip.mealPerPersonDay" class="w-full" :min="0" :step="20">
              <template #prefix>￥</template>
            </n-input-number>
          </Field>
          <Field label="市内交通/日">
            <n-input-number v-model:value="trip.localTransportDay" class="w-full" :min="0" :step="20">
              <template #prefix>￥</template>
            </n-input-number>
          </Field>
        </div>

        <div class="mt-5 space-y-3">
          <article v-for="day in itinerary" :key="day.day" class="rounded-[24px] bg-white/42 p-4 dark:bg-white/6">
            <div class="grid gap-3 md:grid-cols-[80px_1fr]">
              <div class="font-800">Day {{ day.day }}</div>
              <n-input v-model:value="day.title" placeholder="例如：海边拍照 + 夜市美食" />
            </div>
          </article>
        </div>

        <div class="mt-5 flex justify-between">
          <n-button round @click="currentStep = 0">上一步</n-button>
          <n-button type="primary" round @click="currentStep = 2">下一步：选择酒店</n-button>
        </div>
      </section>

      <section v-else-if="currentStep === 2" class="liquid rounded-[28px] p-5">
        <div class="grid gap-4 lg:grid-cols-[1fr_1fr_150px_120px]">
          <Field label="酒店搜索">
            <n-input v-model:value="hotelKeyword" placeholder="海景 / 市中心 / 亲子 / 地铁" @keydown.enter="recommendHotels" />
          </Field>
          <Field label="目的地">
            <n-input v-model:value="trip.destination" />
          </Field>
          <Field label="酒店档次">
            <n-select v-model:value="hotelLevel" :options="hotelLevelOptions" />
          </Field>
          <Field label="房间数">
            <n-input-number v-model:value="trip.rooms" class="w-full" :min="1" :max="6" />
          </Field>
        </div>

        <div class="mt-4 flex flex-wrap items-center gap-2">
          <n-button type="primary" round :loading="hotelLoading" @click="recommendHotels">推荐酒店</n-button>
          <span class="text-sm text-[var(--muted)]">{{ hotelSource }}</span>
        </div>

        <div class="mt-5 grid gap-4 lg:grid-cols-3">
          <article
            v-for="hotel in hotelOptions"
            :key="hotel.id"
            class="cursor-pointer overflow-hidden rounded-[24px] border transition hover:-translate-y-1"
            :class="selectedHotelId === hotel.id ? 'border-[var(--button-primary-bg)] bg-white/65 shadow-xl dark:bg-white/10' : 'border-white/20 bg-white/40 dark:bg-white/6'"
            @click="selectedHotelId = hotel.id"
          >
            <img
              :key="`${hotel.id}-${hotel.level}-${hotel.cover}`"
              class="h-40 w-full object-cover"
              :src="hotelImage(hotel)"
              :alt="hotel.name"
              @load="ensureHotelImage($event, hotel)"
              @error="fallbackHotelImage($event, hotel)"
            />
            <div class="p-4">
              <div class="flex items-start justify-between gap-3">
                <h3 class="m-0 text-xl">{{ hotel.name }}</h3>
                <span class="rounded-full bg-white/60 px-2 py-1 text-xs dark:bg-white/10">{{ hotel.score }}分</span>
              </div>
              <p class="mt-2 line-clamp-2 text-sm leading-6 text-[var(--muted)]">{{ hotel.reason }}</p>
              <div class="mt-3 grid gap-1 text-sm text-[var(--muted)]">
                <div>{{ hotel.nearestStation }} · 约{{ hotel.stationDistance }}km</div>
                <div>{{ hotel.route }}</div>
              </div>
              <div class="mt-3 flex items-end justify-between gap-3">
                <div>
                  <div class="text-xs text-[var(--muted)]">平台价格</div>
                  <div class="text-2xl font-800 text-[var(--button-primary-bg)]">{{ hotelPriceText(hotel) }}</div>
                </div>
                <div class="flex gap-2">
                  <n-button size="small" round @click.stop="openHotelDetail(hotel)">详情</n-button>
                  <n-button size="small" round @click.stop="openHotelBooking(hotel)">预订</n-button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <div class="mt-5 flex justify-between">
          <n-button round @click="currentStep = 1">上一步</n-button>
          <n-button type="primary" round @click="currentStep = 3">下一步：准备行李</n-button>
        </div>
      </section>

      <section v-else-if="currentStep === 3" class="liquid rounded-[28px] p-5">
        <div>
          <h2 class="m-0 text-2xl">随身必备</h2>
          <div class="mt-3 grid gap-3 md:grid-cols-2">
            <label
              v-for="item in packingItems"
              :key="item.name"
              class="flex items-center gap-3 rounded-2xl bg-white/42 p-3 dark:bg-white/6"
            >
              <n-checkbox v-model:checked="item.checked" />
              <span>{{ item.name }}</span>
            </label>
          </div>

          <div class="mt-4 grid gap-3 md:grid-cols-[1fr_90px]">
            <n-input v-model:value="customPacking.name" placeholder="添加随身必备，例如耳机、眼罩" />
            <n-button round @click="addPackingItem">添加</n-button>
          </div>
        </div>

        <div class="mt-6 border-t border-white/30 pt-5">
          <h2 class="m-0 text-2xl">本次需要购买</h2>
          <div class="mt-3 grid gap-3 md:grid-cols-2">
            <label
              v-for="item in purchaseItems"
              :key="item.name"
              class="flex items-center justify-between gap-3 rounded-2xl bg-white/42 p-3 dark:bg-white/6"
            >
              <div class="flex items-center gap-3">
                <n-checkbox v-model:checked="item.checked" />
                <span>{{ item.name }}</span>
              </div>
              <n-input-number v-model:value="item.price" class="max-w-[130px]" size="small" :min="0" :step="20">
                <template #prefix>￥</template>
              </n-input-number>
            </label>
          </div>

          <div class="mt-4 grid gap-3 md:grid-cols-[1fr_140px_90px]">
            <n-input v-model:value="customPurchase.name" placeholder="添加需要购买的物品" />
            <n-input-number v-model:value="customPurchase.price" class="w-full" :min="0" :step="20">
              <template #prefix>￥</template>
            </n-input-number>
            <n-button round @click="addPurchaseItem">添加</n-button>
          </div>
        </div>

        <div class="mt-5 flex justify-between">
          <n-button round @click="currentStep = 2">上一步</n-button>
          <n-button type="primary" round @click="currentStep = 4">查看最终行程</n-button>
        </div>
      </section>

      <section v-else class="liquid rounded-[28px] p-5">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h2 class="m-0 text-2xl">{{ trip.origin }} → {{ trip.destination }} {{ trip.days }}天{{ nights }}晚</h2>
          </div>
          <div class="rounded-2xl bg-[var(--button-primary-bg)] px-4 py-2 text-white">总预算 ￥{{ budget.total }}</div>
        </div>

        <div class="mt-5 grid gap-4 md:grid-cols-2">
          <PreviewCard title="车票" :main="`${selectedTicket?.trainNo} ${selectedTicket?.seat}`" :sub="`${trip.origin} ${selectedTicket?.depart} → ${trip.destination} ${selectedTicket?.arrive}`" />
          <PreviewCard title="酒店" :main="selectedHotel?.name || ''" :sub="`${nights}晚 · ${selectedHotel?.nearestStation || ''} · ${selectedHotel ? hotelPriceText(selectedHotel) : ''}/晚`" />
        </div>

        <div class="mt-5 space-y-3">
          <article v-for="day in itinerary" :key="day.day" class="rounded-2xl bg-white/42 p-4 dark:bg-white/6">
            <div class="flex items-center gap-3">
              <div class="rounded-2xl bg-[var(--button-primary-bg)] px-3 py-1 text-sm text-white">Day {{ day.day }}</div>
              <div class="font-700">{{ day.title }}</div>
            </div>
          </article>
        </div>

        <div class="mt-5 rounded-[24px] bg-white/42 p-4 dark:bg-white/6">
          <h3 class="m-0 mb-3 text-xl">行李清单</h3>
          <div class="space-y-3">
            <div class="flex flex-wrap gap-2">
              <span v-for="item in selectedPackingItems" :key="item.name" class="rounded-2xl bg-white/60 px-3 py-1.5 text-sm dark:bg-white/10">
                {{ item.name }}
              </span>
            </div>
            <div v-if="selectedPurchaseItems.length" class="flex flex-wrap gap-2 border-t border-white/30 pt-3">
              <span v-for="item in selectedPurchaseItems" :key="item.name" class="rounded-2xl bg-white/60 px-3 py-1.5 text-sm dark:bg-white/10">
                {{ item.name }}{{ item.price ? ` · ￥${item.price}` : '' }}
              </span>
            </div>
          </div>
        </div>

        <div class="mt-5 flex flex-wrap justify-between gap-2">
          <n-button round @click="currentStep = 3">上一步</n-button>
          <div class="flex gap-2">
            <n-button round @click="open12306(selectedTicket)">订车票</n-button>
            <n-button type="primary" round @click="openHotelBooking(selectedHotel)">订酒店</n-button>
            <n-button type="primary" round :loading="savingPlan" @click="saveToolPlan">保存行程</n-button>
          </div>
        </div>
      </section>
    </section>

    <aside class="liquid sticky top-5 h-fit rounded-[28px] p-5">
      <div class="flex items-start justify-between gap-3">
        <div>
          <h2 class="m-0 text-2xl">实时预算</h2>
          <p class="m-0 mt-1 text-sm text-[var(--muted)]">{{ trip.origin }} → {{ trip.destination }} · {{ trip.days }}天{{ nights }}晚</p>
        </div>
        <div class="rounded-2xl bg-[var(--button-primary-bg)] px-3 py-2 text-white">￥{{ budget.total }}</div>
      </div>

      <div class="mt-5 space-y-3">
        <BudgetRow v-for="item in budget.items" :key="item.name" :item="item" :total="budget.total" />
      </div>

      <div class="mt-5 grid grid-cols-2 gap-3">
        <Metric title="人均" :value="budget.perPerson" />
        <Metric title="日均" :value="budget.perDay" />
      </div>

      <div class="mt-5 grid gap-2">
        <n-button type="primary" round block @click="open12306(selectedTicket)">确认车票</n-button>
        <n-button round block @click="openHotelBooking(selectedHotel)">确认酒店价格</n-button>
      </div>
    </aside>

    <n-modal v-model:show="hotelDetailVisible" preset="card" class="max-w-[920px]" :bordered="false">
      <div v-if="detailHotel" class="grid gap-5 md:grid-cols-[1.15fr_.85fr]">
        <img class="h-[360px] w-full rounded-[24px] object-cover" :src="hotelImage(detailHotel)" :alt="detailHotel.name" @error="fallbackHotelImage($event, detailHotel)" />
        <div>
          <div class="text-sm font-700 text-[var(--muted)]">{{ detailHotel.city }} · {{ detailHotel.levelLabel }} · {{ detailHotel.score }}分</div>
          <h2 class="m-0 mt-2 text-3xl">{{ detailHotel.name }}</h2>
          <p class="mt-4 text-base leading-8 text-[var(--muted)]">{{ detailHotel.reason }}</p>
          <div class="mt-4 grid gap-3 text-sm">
            <div class="rounded-2xl bg-white/45 p-3 dark:bg-white/6">最近交通：{{ detailHotel.nearestStation }}，约{{ detailHotel.stationDistance }}km</div>
            <div class="rounded-2xl bg-white/45 p-3 dark:bg-white/6">到达方式：{{ detailHotel.route }}</div>
            <div class="rounded-2xl bg-white/45 p-3 dark:bg-white/6">地址：{{ detailHotel.address || detailHotel.desc }}</div>
          </div>
          <div class="mt-5 flex flex-wrap gap-2">
            <n-button type="primary" round @click="openHotelBooking(detailHotel)">预订酒店</n-button>
            <n-button round @click="openHotelNavigation(detailHotel)">打开导航</n-button>
          </div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import AMapLoader from '@amap/amap-jsapi-loader'
import { http, type Hotel } from '../api'
import type { AmapPoi } from '../amapPoi'
import { hotelBookingUrl } from '../hotelPrice'
import { hotelSearchKeywords, sortRegularHotelPois } from '../hotelSearch'
import {
  ensureHotelImage as checkHotelImage,
  fallbackHotels as createFallbackHotels,
  hotelFromApi,
  hotelFromPoi,
  hotelImage as resolveHotelImage,
  hotelLevelOptions,
  hotelPriceText as formatHotelPrice,
  repairHotelImage,
  type HotelOption,
} from '../hotelPlanner'
import {
  buildPlanContent as createPlanContent,
  defaultItinerary,
  defaultPackingItems,
  defaultPurchaseItems,
  defaultTrip,
  emptyTrip,
  syncItineraryDays,
  ticketOptions as createTicketOptions,
  tripBudget,
  planTitle as createPlanTitle,
  type PackingItem,
} from '../tripPlanner'
import { cityCoords, cityDistance, dateText, levelText, stationCodes, transitAnchors } from '../travelMath'
import { useUserStore } from '../stores/user'
import { amapNavigationUrl, open12306Url } from '../externalLinks'

type AmapWindow = Window & { _AMapSecurityConfig?: { securityJsCode: string } }
type AmapSearchResponse = { poiList?: { pois?: AmapPoi[] }; info?: string }

const toast = useMessage()
const router = useRouter()
const user = useUserStore()

const Field = defineComponent({
  props: { label: String },
  setup(props, { slots }) {
    return () => h('label', { class: 'grid gap-1.5' }, [
      h('span', { class: 'text-xs font-700 text-[var(--muted)]' }, props.label),
      slots.default?.(),
    ])
  },
})

const BudgetRow = defineComponent({
  props: { item: { type: Object, required: true }, total: { type: Number, required: true } },
  setup(props) {
    return () => {
      const item = props.item as { name: string; value: number }
      const percent = props.total ? Math.round(item.value * 100 / props.total) : 0
      return h('div', { class: 'rounded-2xl bg-white/42 p-3 dark:bg-white/6' }, [
        h('div', { class: 'flex justify-between text-sm' }, [
          h('span', item.name),
          h('span', `￥${item.value}`),
        ]),
        h('div', { class: 'mt-2 h-2 overflow-hidden rounded-full bg-white/60 dark:bg-white/10' }, [
          h('div', { class: 'h-full rounded-full bg-[var(--button-primary-bg)]', style: { width: `${percent}%` } }),
        ]),
      ])
    }
  },
})

const Metric = defineComponent({
  props: { title: String, value: Number },
  setup(props) {
    return () => h('div', { class: 'rounded-2xl bg-white/42 p-3 text-center dark:bg-white/6' }, [
      h('div', { class: 'text-xs text-[var(--muted)]' }, props.title),
      h('div', { class: 'mt-1 text-xl font-800' }, `￥${props.value}`),
    ])
  },
})

const PreviewCard = defineComponent({
  props: { title: String, main: String, sub: String },
  setup(props) {
    return () => h('article', { class: 'rounded-[24px] bg-white/42 p-4 dark:bg-white/6' }, [
      h('div', { class: 'text-sm text-[var(--muted)]' }, props.title),
      h('h3', { class: 'm-0 mt-2 text-xl' }, props.main),
      h('p', { class: 'm-0 mt-2 text-sm leading-6 text-[var(--muted)]' }, props.sub),
    ])
  },
})

const steps = [
  { key: 'ticket', title: '车票', icon: '↗' },
  { key: 'days', title: '日程', icon: '○' },
  { key: 'hotel', title: '酒店', icon: '◇' },
  { key: 'packing', title: '行李', icon: '□' },
  { key: 'preview', title: '预览', icon: '✓' },
]

const currentStep = ref(0)
const hotelKeyword = ref('海景')
const hotelLevel = ref('comfort')
const hotelLoading = ref(false)
const hotelSource = ref('填写目的地后，可按偏好推荐酒店。')
const hotelDetailVisible = ref(false)
const detailHotel = ref<HotelOption>()
const savingPlan = ref(false)
const selectedTicketId = ref('g')
const selectedHotelId = ref('hotel-1')
const customPacking = reactive({ name: '' })
const customPurchase = reactive({ name: '', price: 0 })
const trip = reactive(defaultTrip())
const itinerary = ref(defaultItinerary())
const packingItems = reactive<PackingItem[]>(defaultPackingItems())
const purchaseItems = reactive<PackingItem[]>(defaultPurchaseItems())
const hotelOptions = ref<HotelOption[]>(fallbackHotels())
const nights = computed(() => Math.max(1, (trip.days || 1) - 1))
const distance = computed(() => cityDistance(trip.origin.trim(), trip.destination.trim()) || 1200)
const selectedPackingItems = computed(() => packingItems.filter(item => item.checked))
const selectedPurchaseItems = computed(() => purchaseItems.filter(item => item.checked))
const ticketOptions = computed(() => createTicketOptions(trip.origin, trip.destination, distance.value))
const selectedTicket = computed(() => ticketOptions.value.find(item => item.id === selectedTicketId.value) || ticketOptions.value[0])
const selectedHotel = computed(() => hotelOptions.value.find(item => item.id === selectedHotelId.value) || hotelOptions.value[0])
const budget = computed(() => tripBudget(trip, nights.value, selectedTicket.value, selectedHotel.value, selectedPurchaseItems.value))

watch(() => trip.days, days => {
  itinerary.value = syncItineraryDays(itinerary.value, days || 1)
})

watch(() => trip.people, people => {
  trip.rooms = Math.max(1, Math.ceil((people || 1) / 2))
})

watch(() => trip.destination, () => {
  hotelOptions.value = trip.destination ? fallbackHotels() : []
  selectedHotelId.value = hotelOptions.value[0]?.id || ''
})

watch(hotelLevel, () => {
  hotelOptions.value = trip.destination ? fallbackHotels() : []
  selectedHotelId.value = hotelOptions.value[0]?.id || ''
  if (trip.destination) recommendHotels()
})

onMounted(recommendHotels)

function resetPlanner() {
  Object.assign(trip, emptyTrip())
  itinerary.value = [{ day: 1, title: '' }]
  currentStep.value = 0
  selectedTicketId.value = ''
  selectedHotelId.value = ''
  hotelKeyword.value = ''
  hotelLevel.value = 'comfort'
  hotelOptions.value = []
  hotelSource.value = '填写目的地后，可按偏好推荐酒店。'
  resetPackingItems()
  customPacking.name = ''
  customPurchase.name = ''
  customPurchase.price = 0
}

async function recommendHotels() {
  hotelLoading.value = true
  hotelSource.value = '正在查找合适的酒店'
  try {
    const mapped = await searchOnlineHotels()
    if (mapped.length) {
      hotelOptions.value = mapped
      selectedHotelId.value = mapped[0].id
      hotelSource.value = `已找到 ${mapped.length} 家可选酒店`
      return
    }
    throw new Error('暂未找到在线酒店')
  } catch (error) {
    hotelOptions.value = fallbackHotels()
    selectedHotelId.value = hotelOptions.value[0]?.id || ''
    hotelSource.value = '暂未获取在线结果，已保留可选推荐'
    toast.warning(hotelSource.value)
  } finally {
    hotelLoading.value = false
  }
}

async function searchOnlineHotels() {
  try {
    const rows = await searchOnlineHotelsByBackend()
    if (rows.length) return rows
  } catch {
    // Try browser-side AMap SDK next. This works when backend is not available.
  }
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) throw new Error('缺少 VITE_AMAP_KEY')
  return searchOnlineHotelsByJs(key)
}

async function searchOnlineHotelsByBackend() {
  const keyword = `${trip.destination}${levelText(hotelLevel.value)}正规酒店 ${hotelKeyword.value}`.trim()
  const rows = await http.get<Hotel[]>('/api/hotels/online', {
    params: { city: trip.destination, keyword, limit: 18 },
  })
  return rows
    .filter(row => row.name)
    .slice(0, 3)
    .map((row, index) => hotelFromApi(row, index, trip.destination, hotelLevel.value, hotelKeyword.value))
}

async function searchOnlineHotelsByJs(key: string) {
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
  if (securityJsCode) (window as AmapWindow)._AMapSecurityConfig = { securityJsCode }
  const AMap = await AMapLoader.load({ key, version: '2.0', plugins: ['AMap.PlaceSearch'] })
  const placeSearch = new AMap.PlaceSearch({
    city: trip.destination || undefined,
    citylimit: !!trip.destination,
    type: '住宿服务',
    extensions: 'all',
    pageSize: 18,
    pageIndex: 1,
  })
  const keyword = `${trip.destination}${levelText(hotelLevel.value)}正规酒店 ${hotelKeyword.value}`.trim()
  const groups = await Promise.allSettled(hotelSearchKeywords(trip.destination, keyword).map(word => new Promise<AmapPoi[]>((resolve, reject) => {
    placeSearch.search(word, (status: string, res: AmapSearchResponse) => {
      if (status === 'complete' && res?.poiList?.pois?.length) resolve(res.poiList.pois)
      else reject(new Error(res?.info || '高德 JS 酒店搜索失败'))
    })
  })))
  const pois = sortRegularHotelPois(groups.flatMap(group => group.status === 'fulfilled' ? group.value : []))
  return pois.slice(0, 3).map((poi, index) => hotelFromPoi(poi, index, trip.destination, hotelLevel.value, hotelKeyword.value))
}

function fallbackHotels() {
  return createFallbackHotels(trip.destination, hotelLevel.value)
}

function addPackingItem() {
  const name = customPacking.name.trim()
  if (!name) return
  packingItems.push({ name, checked: true })
  customPacking.name = ''
}

function addPurchaseItem() {
  const name = customPurchase.name.trim()
  if (!name) return
  purchaseItems.push({ name, checked: true, price: Math.max(0, Math.round(customPurchase.price || 0)) })
  customPurchase.name = ''
  customPurchase.price = 0
}

async function saveToolPlan() {
  if (!user.token) {
    toast.warning('登录后可保存行程')
    router.push('/login?redirect=/app/tools')
    return
  }
  if (!trip.origin.trim() || !trip.destination.trim()) {
    toast.warning('请先填写出发地和目的地')
    currentStep.value = 0
    return
  }
  savingPlan.value = true
  try {
    await http.post('/api/plans', {
      title: planTitle(),
      destination: `${trip.origin} → ${trip.destination}`,
      budget: budget.value.total,
      days: trip.days,
      season: '已整理',
      content: buildPlanContent(),
    })
    toast.success('已保存到行程')
    router.push('/app/plans')
  } catch (e) {
    toast.error((e as Error).message || '保存失败，请稍后重试')
  } finally {
    savingPlan.value = false
  }
}

function planTitle() {
  return createPlanTitle(trip)
}

function buildPlanContent() {
  return createPlanContent({
    trip,
    nights: nights.value,
    budgetTotal: budget.value.total,
    ticket: selectedTicket.value,
    hotel: selectedHotel.value,
    hotelPriceText: selectedHotel.value ? hotelPriceText(selectedHotel.value) : '未选择',
    itinerary: itinerary.value,
    packingItems: selectedPackingItems.value,
    purchaseItems: selectedPurchaseItems.value,
  })
}

function resetPackingItems() {
  packingItems.splice(0, packingItems.length, ...defaultPackingItems())
  purchaseItems.splice(0, purchaseItems.length, ...defaultPurchaseItems())
}

function openHotelDetail(hotel: HotelOption) {
  detailHotel.value = hotel
  hotelDetailVisible.value = true
}

function open12306(ticket = selectedTicket.value) {
  const fromCode = stationCodes[trip.origin.trim()]
  const toCode = stationCodes[trip.destination.trim()]
  const date = dateText(trip.date)
  window.open(open12306Url({ origin: trip.origin, destination: trip.destination, date, trainNo: ticket?.trainNo, fromCode, toCode }), '_blank', 'noreferrer')
}

function openHotelBooking(hotel = selectedHotel.value) {
  if (!hotel) return
  window.open(hotelBookingUrl(hotel, trip.destination), '_blank', 'noreferrer')
}

function openHotelNavigation(hotel: HotelOption) {
  const anchor = transitAnchors[hotel.city] || { name: trip.destination, longitude: cityCoords[trip.destination]?.[0] || 0, latitude: cityCoords[trip.destination]?.[1] || 0 }
  window.open(amapNavigationUrl(anchor, hotel), '_blank', 'noreferrer')
}

function hotelImage(hotel: HotelOption) {
  return resolveHotelImage(hotel, hotelLevel.value)
}

function ensureHotelImage(e: Event, hotel: HotelOption) {
  checkHotelImage(e, hotel, hotelLevel.value)
}

function fallbackHotelImage(e: Event, hotel: HotelOption) {
  repairHotelImage(e, hotel, hotelLevel.value)
}

function hotelPriceText(hotel: Pick<HotelOption, 'name' | 'city' | 'price'>) {
  return formatHotelPrice(hotel, trip.destination)
}

</script>
