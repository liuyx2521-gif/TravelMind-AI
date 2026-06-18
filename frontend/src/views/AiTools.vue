<template>
  <div class="space-y-5">
    <section class="liquid rounded-[28px] p-5 md:p-7">
      <h1 class="m-0 text-3xl">旅行工具箱</h1>
      <p class="m-0 mt-2 text-[var(--muted)]">把出发前最容易纠结的事交给我，预算、路线、避坑和行李都能一起想清楚。</p>
    </section>

    <n-tabs v-model:value="active" type="segment" animated>
      <n-tab-pane name="budget" tab="预算分析">
        <ToolShell title="预算分析" desc="告诉我从哪出发、去哪、几个人，我帮你把大概要花的钱拆开算清楚。">
          <template #form>
            <Field label="出发地">
              <n-input v-model:value="budget.origin" placeholder="例如杭州" />
            </Field>
            <Field label="目的地">
              <n-input v-model:value="budget.destination" placeholder="例如三亚" />
            </Field>
            <div class="grid grid-cols-2 gap-3">
              <Field label="出行天数">
                <n-input-number v-model:value="budget.days" class="w-full" placeholder="例如3" />
              </Field>
              <Field label="人数">
                <n-input-number v-model:value="budget.people" class="w-full" placeholder="例如2" />
              </Field>
            </div>
            <Field label="酒店档位">
              <n-select v-model:value="budget.hotelLevel" :options="hotelLevels" placeholder="选择酒店档位" />
            </Field>
            <Field label="出行方式">
              <n-select v-model:value="budget.transport" :options="transportOptions" placeholder="选择出行方式" />
            </Field>
            <Field label="餐饮档位">
              <n-select v-model:value="budget.foodLevel" :options="foodLevels" placeholder="选择餐饮档位" />
            </Field>
            <Field label="门票/项目金额">
              <n-input-number v-model:value="budget.ticket" class="w-full" placeholder="例如600" />
            </Field>
            <n-button type="primary" round block @click="runBudget">生成预算</n-button>
          </template>
          <template #result>
            <div class="grid gap-3 sm:grid-cols-3">
              <Metric title="总预算" :value="`￥${budgetResult.total}`" />
              <Metric title="人均" :value="`￥${budgetResult.perPerson}`" />
              <Metric title="日均" :value="`￥${budgetResult.perDay}`" />
            </div>
            <div ref="budgetChartRef" class="mt-4 h-[280px]"></div>
            <div class="mt-4 grid grid-cols-2 gap-3">
              <div v-for="item in budgetResult.items" :key="item.name" class="rounded-2xl bg-white/45 p-3 dark:bg-white/8">
                <div class="text-sm text-[var(--muted)]">{{ item.name }}</div>
                <div class="mt-1 text-xl font-800">￥{{ item.value }}</div>
              </div>
            </div>
            <div class="mt-4 rounded-2xl bg-white/35 p-4 text-sm leading-6 text-[var(--muted)] dark:bg-white/6">
              {{ budgetResult.transportNote }}
            </div>
          </template>
        </ToolShell>
      </n-tab-pane>

      <n-tab-pane name="route" tab="路线对比">
        <ToolShell title="路线对比" desc="不确定怎么走时，我会给你几种不同节奏的方案，方便你直接比较。">
          <template #form>
            <Field label="出发地">
              <n-input v-model:value="route.origin" placeholder="例如杭州" />
            </Field>
            <Field label="目的地">
              <n-input v-model:value="route.destination" placeholder="例如成都" />
            </Field>
            <div class="grid grid-cols-2 gap-3">
              <Field label="出行天数">
                <n-input-number v-model:value="route.days" class="w-full" placeholder="例如4" />
              </Field>
              <Field label="预算金额">
                <n-input-number v-model:value="route.budget" class="w-full" placeholder="例如4000" />
              </Field>
            </div>
            <Field label="旅行偏好">
              <n-input v-model:value="route.preference" type="textarea" :autosize="{ minRows: 3 }" placeholder="例如美食、夜景、少折腾" />
            </Field>
            <n-button type="primary" round block :loading="loading.route" @click="runRoute">生成路线对比</n-button>
          </template>
          <template #result>
            <div class="grid gap-3 xl:grid-cols-3">
              <article v-for="plan in routeCards" :key="plan.name" class="rounded-[24px] bg-white/45 p-4 dark:bg-white/8">
                <div class="flex items-center justify-between gap-3">
                  <h3 class="m-0 text-xl">{{ plan.name }}</h3>
                  <span class="rounded-full bg-[var(--button-bg)] px-3 py-1 text-xs">{{ plan.score }}</span>
                </div>
                <p class="text-sm leading-6 text-[var(--muted)]">{{ plan.desc }}</p>
                <div class="mt-3 grid gap-2 text-sm">
                  <div>交通：{{ plan.transport }}</div>
                  <div>费用：{{ plan.cost }}</div>
                  <div>适合：{{ plan.fit }}</div>
                </div>
              </article>
            </div>
            <ResultText :text="answers.route" />
          </template>
        </ToolShell>
      </n-tab-pane>

      <n-tab-pane name="pitfall" tab="避坑助手">
        <ToolShell title="避坑助手" desc="出发前先看看哪些地方容易踩坑，订票、住宿、吃饭和游玩都少走弯路。">
          <template #form>
            <Field label="目的地">
              <n-input v-model:value="pitfall.destination" placeholder="例如三亚" />
            </Field>
            <Field label="旅行玩法">
              <n-input v-model:value="pitfall.preference" type="textarea" :autosize="{ minRows: 3 }" placeholder="例如海边、亲子、夜市、租车" />
            </Field>
            <n-button type="primary" round block :loading="loading.pitfall" @click="runPitfall">生成避坑清单</n-button>
          </template>
          <template #result>
            <div class="grid gap-3 md:grid-cols-2">
              <article v-for="item in pitfallCards" :key="item.title" class="rounded-[22px] bg-white/45 p-4 dark:bg-white/8">
                <h3 class="m-0">{{ item.icon }} {{ item.title }}</h3>
                <p class="m-0 mt-2 text-sm leading-6 text-[var(--muted)]">{{ item.text }}</p>
              </article>
            </div>
            <ResultText :text="answers.pitfall" />
          </template>
        </ToolShell>
      </n-tab-pane>

      <n-tab-pane name="packing" tab="行李清单">
        <ToolShell title="行李清单" desc="告诉我去哪玩、玩几天、和谁去，我帮你列一份出门前能直接勾选的清单。">
          <template #form>
            <Field label="目的地">
              <n-input v-model:value="packing.destination" placeholder="例如哈尔滨" />
            </Field>
            <div class="grid grid-cols-2 gap-3">
              <Field label="出行天数">
                <n-input-number v-model:value="packing.days" class="w-full" placeholder="例如4" />
              </Field>
              <Field label="出行人群">
                <n-select v-model:value="packing.peopleType" :options="peopleTypes" placeholder="选择人群" />
              </Field>
            </div>
            <Field label="旅行玩法">
              <n-input v-model:value="packing.preference" type="textarea" :autosize="{ minRows: 3 }" placeholder="例如雪景、徒步、亲子、海边" />
            </Field>
            <n-button type="primary" round block :loading="loading.packing" @click="runPacking">生成行李清单</n-button>
          </template>
          <template #result>
            <div class="grid gap-3 md:grid-cols-2">
              <label v-for="item in packingItems" :key="item" class="flex items-center gap-3 rounded-2xl bg-white/45 p-3 dark:bg-white/8">
                <n-checkbox />
                <span>{{ item }}</span>
              </label>
            </div>
            <ResultText :text="answers.packing" />
          </template>
        </ToolShell>
      </n-tab-pane>
    </n-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { http } from '../api'

const ToolShell = defineComponent({
  props: { title: String, desc: String },
  setup(props, { slots }) {
    return () => h('div', { class: 'grid gap-5 lg:grid-cols-[360px_1fr]' }, [
      h('section', { class: 'liquid h-fit rounded-[28px] p-5' }, [
        h('h2', { class: 'm-0 text-2xl' }, props.title),
        h('p', { class: 'm-0 mt-2 mb-5 text-sm text-[var(--muted)]' }, props.desc),
        h('div', { class: 'grid gap-3' }, slots.form?.()),
      ]),
      h('section', { class: 'liquid min-h-[420px] rounded-[28px] p-5' }, slots.result?.()),
    ])
  },
})

const Field = defineComponent({
  props: { label: String },
  setup(props, { slots }) {
    return () => h('label', { class: 'grid gap-1.5' }, [
      h('span', { class: 'text-xs font-700 text-[var(--muted)]' }, props.label),
      slots.default?.(),
    ])
  },
})

const Metric = defineComponent({
  props: { title: String, value: String },
  setup(props) {
    return () => h('div', { class: 'rounded-2xl bg-white/45 p-4 dark:bg-white/8' }, [
      h('div', { class: 'text-sm text-[var(--muted)]' }, props.title),
      h('div', { class: 'mt-1 text-2xl font-800' }, props.value),
    ])
  },
})

const ResultText = defineComponent({
  props: { text: String },
  setup(props) {
    return () => props.text
      ? h('pre', { class: 'mt-4 max-h-[360px] overflow-auto whitespace-pre-wrap rounded-2xl bg-white/35 p-4 font-sans text-sm leading-7 text-[var(--muted)] dark:bg-white/6' }, props.text)
      : h('div', { class: 'mt-4 rounded-2xl bg-white/35 p-4 text-center text-[var(--muted)] dark:bg-white/6' }, '点击左侧按钮生成结果')
  },
})

const active = ref('budget')
const budgetChartRef = ref<HTMLDivElement>()
let budgetChart: echarts.ECharts | undefined

const budget = reactive({ origin: '杭州', destination: '三亚', days: 3, people: 2, hotelLevel: 'comfort', transport: 'train', foodLevel: 'normal', ticket: 600 })
const route = reactive({ origin: '杭州', destination: '成都', days: 4, budget: 4000, preference: '美食 夜景 少折腾' })
const pitfall = reactive({ destination: '三亚', preference: '海边 美食 拍照' })
const packing = reactive({ destination: '哈尔滨', days: 4, peopleType: 'couple', preference: '雪景 城市游 拍照' })
const answers = reactive({ route: '', pitfall: '', packing: '' })
const loading = reactive({ route: false, pitfall: false, packing: false })

const hotelLevels = [
  { label: '经济型', value: 'budget' },
  { label: '舒适型', value: 'comfort' },
  { label: '高端型', value: 'premium' },
]
const transportOptions = [
  { label: '高铁/火车', value: 'train' },
  { label: '飞机', value: 'flight' },
  { label: '自驾/打车', value: 'car' },
]
const foodLevels = [
  { label: '节省', value: 'save' },
  { label: '正常', value: 'normal' },
  { label: '吃好一点', value: 'premium' },
]
const peopleTypes = [
  { label: '情侣', value: 'couple' },
  { label: '亲子', value: 'family' },
  { label: '学生', value: 'student' },
  { label: '老人', value: 'senior' },
]

const cityCoords: Record<string, [number, number]> = {
  北京: [116.4074, 39.9042],
  上海: [121.4737, 31.2304],
  广州: [113.2644, 23.1291],
  深圳: [114.0579, 22.5431],
  杭州: [120.1551, 30.2741],
  南京: [118.7969, 32.0603],
  苏州: [120.5853, 31.2989],
  成都: [104.0665, 30.5728],
  重庆: [106.5516, 29.5630],
  西安: [108.9398, 34.3416],
  武汉: [114.3054, 30.5928],
  长沙: [112.9388, 28.2282],
  厦门: [118.0894, 24.4798],
  福州: [119.2965, 26.0745],
  青岛: [120.3826, 36.0671],
  三亚: [109.5119, 18.2528],
  海口: [110.1983, 20.0440],
  昆明: [102.8329, 24.8801],
  大理: [100.2676, 25.6065],
  丽江: [100.2330, 26.8721],
  桂林: [110.2900, 25.2736],
  哈尔滨: [126.6425, 45.7560],
  沈阳: [123.4315, 41.8057],
  天津: [117.2000, 39.1333],
  郑州: [113.6254, 34.7466],
  合肥: [117.2272, 31.8206],
  济南: [117.1201, 36.6512],
  拉萨: [91.1322, 29.6604],
  乌鲁木齐: [87.6168, 43.8256],
}

const budgetResult = computed(() => {
  const days = Math.max(1, budget.days || 1)
  const people = Math.max(1, budget.people || 1)
  const hotelPerNight = { budget: 260, comfort: 520, premium: 980 }[budget.hotelLevel] || 520
  const transport = estimateTransport(budget.origin, budget.destination, budget.transport, people)
  const foodPerPersonDay = { save: 80, normal: 150, premium: 280 }[budget.foodLevel] || 150
  const items = [
    { name: '酒店', value: Math.round(hotelPerNight * Math.max(1, days - 1)) },
    { name: '交通', value: transport.total },
    { name: '餐饮', value: Math.round(foodPerPersonDay * people * days) },
    { name: '门票', value: Math.round(budget.ticket || 0) },
  ]
  const subtotal = items.reduce((sum, item) => sum + item.value, 0)
  const other = Math.round(subtotal * 0.12)
  items.push({ name: '备用金', value: other })
  const total = subtotal + other
  return { items, total, perPerson: Math.round(total / people), perDay: Math.round(total / days), transportNote: transport.note }
})

function estimateTransport(origin: string, destination: string, mode: string, people: number) {
  const distance = cityDistance(origin.trim(), destination.trim())
  if (!distance) {
    const fallback = { train: 550, flight: 950, car: 420 }[mode] || 550
    return {
      total: Math.round(fallback * people),
      note: `交通估算：未识别到 ${origin || '出发地'} 到 ${destination || '目的地'} 的城市距离，按${transportLabel(mode)}默认单人往返约 ￥${fallback} 估算。`,
    }
  }
  const oneWay = Math.round(distance)
  const singleRoundTrip = mode === 'flight'
    ? Math.max(650, Math.round(distance * 0.75 + 260))
    : mode === 'car'
      ? Math.max(300, Math.round(distance * 1.15 + 180))
      : Math.max(180, Math.round(distance * 0.46 + 120))
  return {
    total: Math.round(singleRoundTrip * people),
    note: `交通估算：${origin} 到 ${destination} 约 ${oneWay} 公里，按${transportLabel(mode)}估算单人往返约 ￥${singleRoundTrip}，${people} 人合计约 ￥${singleRoundTrip * people}。实际价格会受节假日、购票时间和班次影响。`,
  }
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

function transportLabel(mode: string) {
  return ({ train: '高铁/火车', flight: '飞机', car: '自驾/打车' } as Record<string, string>)[mode] || '交通方式'
}

const routeCards = computed(() => [
  { name: '省钱方案', score: '性价比高', transport: budget.transport === 'flight' ? '高铁优先' : '火车/地铁优先', cost: `约￥${Math.round((route.budget || 3000) * 0.82)}`, fit: '学生、预算敏感', desc: '减少换乘和高价项目，把预算留给核心景点和美食。' },
  { name: '舒适方案', score: '推荐', transport: '高铁/飞机 + 市区打车', cost: `约￥${route.budget || 3000}`, fit: '情侣、家庭', desc: '住宿位置更好，节奏适中，适合第一次去目的地。' },
  { name: '高效方案', score: '少折腾', transport: '飞机/直达交通', cost: `约￥${Math.round((route.budget || 3000) * 1.18)}`, fit: '假期短、重体验', desc: '减少路上时间，优先覆盖最值得去的区域。' },
])

const pitfallCards = computed(() => [
  { icon: '🚕', title: '交通', text: `${pitfall.destination} 出行先看实时路况，机场/车站打车优先走正规平台。` },
  { icon: '🏨', title: '住宿', text: '订房前确认位置、取消政策、押金、停车和到核心景点的真实通勤时间。' },
  { icon: '🍜', title: '美食', text: '热门店看近期评价，海鲜和夜市注意明码标价，避免只看单个平台推荐。' },
  { icon: '🎫', title: '景点', text: '热门景区提前预约，注意闭园时间、天气变化和临时限流。' },
])

const packingItems = computed(() => {
  const base = ['身份证/护照', '手机充电器', '充电宝', '常用药', '雨伞或雨衣', '舒适鞋']
  const extra = /雪|冷|哈尔滨/.test(`${packing.destination}${packing.preference}`)
    ? ['羽绒服', '保暖内衣', '手套围巾', '暖宝宝', '防滑鞋']
    : /海|三亚|沙滩/.test(`${packing.destination}${packing.preference}`)
      ? ['防晒霜', '墨镜', '泳衣', '拖鞋', '防水袋']
      : ['轻便外套', '防晒用品', '一次性毛巾']
  return [...base, ...extra]
})

function runBudget() {
  nextTick(renderBudgetChart)
}

async function runRoute() {
  loading.route = true
  try {
    answers.route = await http.post('/api/ai/plan', {
      origin: route.origin,
      destination: `${route.destination}，请对比省钱、舒适、高效三种路线`,
      days: route.days,
      budget: route.budget,
      preference: route.preference,
    })
  } finally {
    loading.route = false
  }
}

async function runPitfall() {
  loading.pitfall = true
  try {
    answers.pitfall = await http.post('/api/ai/chat', {
      message: `请给我${pitfall.destination}旅行避坑清单，玩法：${pitfall.preference}。按交通、住宿、美食、景点、天气、预约提醒输出。`,
    }).then((res: any) => res.answer || res)
  } finally {
    loading.pitfall = false
  }
}

async function runPacking() {
  loading.packing = true
  try {
    answers.packing = await http.post('/api/ai/chat', {
      message: `请生成${packing.destination}${packing.days}天旅行行李清单，人群：${packing.peopleType}，玩法：${packing.preference}。按证件、衣物、药品、电子设备、特殊物品输出。`,
    }).then((res: any) => res.answer || res)
  } finally {
    loading.packing = false
  }
}

function renderBudgetChart() {
  if (!budgetChartRef.value) return
  if (!budgetChart) budgetChart = echarts.init(budgetChartRef.value)
  budgetChart.setOption({
    color: ['#6f8398', '#4f8cff', '#34c759', '#ffb340', '#8b7cf6'],
    tooltip: { trigger: 'item', formatter: '{b}<br/>￥{c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      data: budgetResult.value.items,
      label: { formatter: '{b}\n￥{c}' },
    }],
  })
}

onMounted(runBudget)
onBeforeUnmount(() => budgetChart?.dispose())
</script>
