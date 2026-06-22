<template>
  <div class="space-y-5">
    <section class="liquid rounded-[28px] p-5 md:p-7">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 class="m-0 text-3xl">车票预订</h1>
        </div>
        <n-button type="primary" round @click="open12306()">按当前路线打开12306</n-button>
      </div>
    </section>

    <section class="liquid rounded-[28px] p-4 md:p-5">
      <div class="grid gap-4 md:grid-cols-5">
        <Field label="出发地">
          <n-input v-model:value="form.origin" placeholder="杭州" />
        </Field>
        <Field label="目的地">
          <n-input v-model:value="form.destination" placeholder="上海" />
        </Field>
        <Field label="出发日期">
          <n-date-picker v-model:value="form.date" class="w-full" type="date" clearable />
        </Field>
        <Field label="乘车人">
          <n-input-number v-model:value="form.people" class="w-full" :min="1" :max="9" />
        </Field>
        <Field label="席别">
          <n-select v-model:value="form.seat" :options="seatOptions" />
        </Field>
      </div>
    </section>

    <section class="space-y-3">
      <article
        v-for="ticket in filteredTickets"
        :key="ticket.id"
        class="liquid rounded-[28px] p-4 transition hover:-translate-y-1 md:p-5"
      >
        <div class="grid gap-4 md:grid-cols-[120px_1fr_160px_160px] md:items-center">
          <div>
            <h2 class="m-0 text-2xl">{{ ticket.trainNo }}</h2>
            <p class="m-0 mt-1 text-sm text-[var(--muted)]">{{ ticket.type }}</p>
          </div>
          <div class="flex items-center justify-between gap-4">
            <div>
              <div class="text-3xl font-800">{{ ticket.depart }}</div>
              <div class="mt-1 text-sm text-[var(--muted)]">{{ form.origin }}</div>
            </div>
            <div class="min-w-[120px] text-center text-sm text-[var(--muted)]">
              <div>{{ ticket.duration }}</div>
              <div class="my-2 h-px bg-[var(--muted)]/35"></div>
              <div>{{ ticket.stop }}</div>
            </div>
            <div class="text-right">
              <div class="text-3xl font-800">{{ ticket.arrive }}</div>
              <div class="mt-1 text-sm text-[var(--muted)]">{{ form.destination }}</div>
            </div>
          </div>
          <div class="grid gap-1 text-sm">
            <div v-for="seat in ticket.seats" :key="seat.name" class="flex justify-between rounded-xl bg-white/40 px-3 py-1.5 dark:bg-white/6">
              <span>{{ seat.name }}</span>
              <span>￥{{ seat.price }}</span>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <div class="text-right text-sm text-[var(--muted)]">合计约</div>
            <div class="text-right text-2xl font-800 text-[var(--button-primary-bg)]">￥{{ ticketTotal(ticket) }}</div>
            <n-button type="primary" round @click="open12306(ticket)">按当前车次预订</n-button>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, reactive } from 'vue'
import { open12306Url } from '../externalLinks'

type Seat = { name: string; price: number }
type Ticket = {
  id: string
  trainNo: string
  type: string
  depart: string
  arrive: string
  duration: string
  stop: string
  seats: Seat[]
}

const Field = defineComponent({
  props: { label: String },
  setup(props, { slots }) {
    return () => h('label', { class: 'grid gap-1.5' }, [
      h('span', { class: 'text-xs font-700 text-[var(--muted)]' }, props.label),
      slots.default?.(),
    ])
  },
})

const form = reactive({
  origin: '杭州',
  destination: '上海',
  date: Date.now() + 1000 * 60 * 60 * 24 * 7,
  people: 1,
  seat: '二等座',
})
const seatOptions = [
  { label: '二等座', value: '二等座' },
  { label: '一等座', value: '一等座' },
  { label: '商务座', value: '商务座' },
  { label: '硬卧/动卧', value: '硬卧/动卧' },
]
const cityCoords: Record<string, [number, number]> = {
  北京: [116.4074, 39.9042],
  上海: [121.4737, 31.2304],
  广州: [113.2644, 23.1291],
  深圳: [114.0579, 22.5431],
  杭州: [120.1551, 30.2741],
  南京: [118.7969, 32.0603],
  成都: [104.0665, 30.5728],
  重庆: [106.5516, 29.563],
  西安: [108.9398, 34.3416],
  武汉: [114.3054, 30.5928],
  长沙: [112.9388, 28.2282],
  厦门: [118.0894, 24.4798],
  青岛: [120.3826, 36.0671],
  三亚: [109.5119, 18.2528],
  昆明: [102.8329, 24.8801],
}
const stationCodes: Record<string, string> = {
  北京: 'BJP',
  上海: 'SHH',
  杭州: 'HZH',
  广州: 'GZQ',
  深圳: 'SZQ',
  南京: 'NJH',
  成都: 'CDW',
  重庆: 'CQW',
  西安: 'XAY',
  武汉: 'WHN',
  长沙: 'CSQ',
  厦门: 'XMS',
  青岛: 'QDK',
  三亚: 'SEQ',
  昆明: 'KMM',
}
const distance = computed(() => cityDistance(form.origin.trim(), form.destination.trim()) || 800)
const tickets = computed<Ticket[]>(() => {
  const second = Math.max(60, Math.round(distance.value * 0.42 + 45))
  return [
    makeTicket('G', '高速动车', '08:06', 300, second, '直达'),
    makeTicket('D', '动车组', '10:32', 235, Math.round(second * 0.86), '经停少'),
    makeTicket('G', '高速动车', '14:18', 280, Math.round(second * 1.08), '直达'),
    makeTicket('Z', '直达特快', '20:12', 120, Math.round(second * 0.62), '夜间'),
  ]
})
const filteredTickets = computed(() => tickets.value.filter(ticket => ticket.seats.some(seat => seat.name === form.seat)))

function makeTicket(prefix: string, type: string, depart: string, speed: number, secondSeat: number, stop: string): Ticket {
  const minutes = Math.max(35, Math.round(distance.value / speed * 60))
  const [hour, minute] = depart.split(':').map(Number)
  const arriveDate = new Date(2026, 0, 1, hour, minute + minutes)
  return {
    id: `${prefix}-${depart}`,
    trainNo: `${prefix}${cityCode(form.origin) + cityCode(form.destination)}`,
    type,
    depart,
    arrive: `${String(arriveDate.getHours()).padStart(2, '0')}:${String(arriveDate.getMinutes()).padStart(2, '0')}${arriveDate.getDate() > 1 ? '+1' : ''}`,
    duration: `${Math.floor(minutes / 60)}时${minutes % 60}分`,
    stop,
    seats: [
      { name: '二等座', price: secondSeat },
      { name: '一等座', price: Math.round(secondSeat * 1.45) },
      { name: '商务座', price: Math.round(secondSeat * 3.1) },
      { name: '硬卧/动卧', price: Math.round(secondSeat * 0.78) },
    ],
  }
}

function ticketTotal(ticket: Ticket) {
  const seat = ticket.seats.find(item => item.name === form.seat) || ticket.seats[0]
  return seat.price * Math.max(1, form.people || 1)
}

function open12306(ticket?: Ticket) {
  const fromCode = stationCodes[form.origin.trim()]
  const toCode = stationCodes[form.destination.trim()]
  const date = dateText(form.date)
  window.open(open12306Url({ origin: form.origin, destination: form.destination, date, trainNo: ticket?.trainNo, fromCode, toCode }), '_blank', 'noreferrer')
}

function dateText(value: number | null) {
  const date = value ? new Date(value) : new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function cityDistance(origin: string, destination: string) {
  const start = cityCoords[origin]
  const end = cityCoords[destination]
  if (!start || !end) return 0
  const rad = Math.PI / 180
  const dLat = (end[1] - start[1]) * rad
  const dLng = (end[0] - start[0]) * rad
  const lat1 = start[1] * rad
  const lat2 = end[1] * rad
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLng / 2) ** 2
  return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

function cityCode(city: string) {
  let hash = 0
  for (const char of city || 'TM') hash = Math.imul(31, hash) + char.charCodeAt(0) | 0
  return Math.abs(hash % 900) + 100
}
</script>
