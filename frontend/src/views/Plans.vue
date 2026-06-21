<template>
  <div v-if="detailMode && current" class="space-y-5">
    <section class="liquid rounded-[28px] p-5">
      <n-button quaternary class="mb-4" @click="router.push('/app/plans')">返回行程</n-button>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="m-0 text-3xl">{{ current.title }}</h1>
          <p class="m-0 mt-2 text-[var(--muted)]">{{ current.destination }} · {{ current.days }}天 · ￥{{ current.budget }}</p>
        </div>
        <div class="rounded-2xl bg-white/50 px-4 py-2 text-sm dark:bg-white/10">{{ current.season || '已整理' }}</div>
      </div>
    </section>

    <section class="grid gap-5 xl:grid-cols-[1fr_360px]">
      <div class="space-y-5">
        <section v-for="day in structuredDays" :key="day.title" class="liquid rounded-[28px] p-5">
          <h2 class="m-0 mb-4 text-2xl">{{ day.title }}</h2>
          <div class="overflow-x-auto pb-2">
            <div class="flex min-w-[720px] items-stretch gap-3">
              <template v-for="(node, index) in day.nodes" :key="`${day.title}-${node.type}`">
                <button
                  class="flex min-w-[138px] flex-1 flex-col items-center justify-center rounded-[22px] bg-white/55 px-3 py-4 text-center text-[var(--text)] shadow-sm transition hover:-translate-y-0.5 hover:bg-white/75 dark:bg-white/8 dark:hover:bg-white/12"
                  @click="openNode(day, node)"
                >
                  <span class="text-3xl">{{ node.icon }}</span>
                  <span class="mt-2 text-sm font-700">{{ node.label }}</span>
                  <span class="mt-1 line-clamp-2 text-xs text-[var(--muted)]">{{ node.summary }}</span>
                </button>
                <div v-if="index < day.nodes.length - 1" class="flex items-center text-2xl text-[var(--muted)]">→</div>
              </template>
            </div>
          </div>
        </section>
      </div>

      <aside class="space-y-5">
        <section class="liquid rounded-[28px] p-5">
          <h2 class="m-0 mb-3 text-xl">行程组成</h2>
          <div ref="chartRef" class="h-[260px]"></div>
        </section>
        <section class="liquid rounded-[28px] p-5">
          <h2 class="m-0 mb-3 text-xl">完整行程</h2>
          <pre class="max-h-[360px] overflow-auto whitespace-pre-wrap font-sans text-sm leading-7 text-[var(--muted)]">{{ current.content }}</pre>
        </section>
      </aside>
    </section>

    <n-modal v-model:show="nodeVisible" preset="card" class="max-w-2xl" :title="selectedNodeTitle">
      <div v-if="selectedNode" class="space-y-4">
        <p class="m-0 whitespace-pre-wrap leading-8">{{ selectedNode.detail }}</p>
        <div class="grid gap-3 md:grid-cols-2">
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/8">
            <h3 class="m-0 mb-2">避坑贴士</h3>
            <p class="m-0 text-sm leading-6 text-[var(--muted)]">{{ selectedNode.tip }}</p>
          </div>
          <div class="rounded-2xl bg-white/45 p-4 dark:bg-white/8">
            <h3 class="m-0 mb-2">预约方式</h3>
            <p class="m-0 text-sm leading-6 text-[var(--muted)]">{{ selectedNode.booking }}</p>
          </div>
        </div>
      </div>
    </n-modal>
  </div>

  <div v-else-if="!loggedIn" class="liquid rounded-[28px] p-6 text-center">
    <h1 class="m-0 text-2xl">登录后可保存行程</h1>
    <div class="mt-5 flex justify-center gap-3">
      <n-button type="primary" round @click="router.push('/login?redirect=/app/plans')">去登录</n-button>
      <n-button round @click="router.push('/register')">注册账号</n-button>
    </div>
  </div>

  <div v-else class="space-y-4">
    <article
      v-for="plan in plans"
      :key="plan.id"
      class="liquid cursor-pointer rounded-[28px] p-5 transition hover:-translate-y-1"
      @click="router.push(`/app/plans/${plan.id}`)"
    >
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 class="m-0 text-2xl">{{ listTitle(plan) }}</h2>
        </div>
        <div class="rounded-2xl bg-white/50 px-4 py-2 text-sm font-700 dark:bg-white/10">查看详情</div>
      </div>
    </article>
    <n-empty v-if="!plans.length" description="暂无行程" />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { http, type Plan } from '../api'
import { useUserStore } from '../stores/user'

type FlowType = 'attraction' | 'food' | 'hotel'
type FlowNode = {
  type: FlowType
  icon: string
  label: string
  summary: string
  detail: string
  tip: string
  booking: string
}
type DayPlan = { title: string; text: string; nodes: FlowNode[] }

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const plans = ref<Plan[]>([])
const current = ref<Plan>()
const chartRef = ref<HTMLDivElement>()
const nodeVisible = ref(false)
const selectedNode = ref<FlowNode>()
const selectedDay = ref<DayPlan>()
const detailMode = computed(() => !!route.params.id)
const loggedIn = computed(() => !!user.token)
const structuredDays = computed(() => parsePlanDays(current.value))
const selectedNodeTitle = computed(() => selectedDay.value && selectedNode.value ? `${selectedDay.value.title} · ${selectedNode.value.label}` : '详情')
let chart: echarts.ECharts | undefined

async function load() {
  if (!user.token) return
  if (detailMode.value) {
    current.value = await http.get<Plan>(`/api/plans/${route.params.id}`)
    await nextTick()
    renderChart()
    return
  }
  plans.value = await http.get<Plan[]>('/api/plans')
}

function parsePlanDays(plan?: Plan): DayPlan[] {
  const content = plan?.content?.trim() || ''
  if (!content) return []
  const matches = [...content.matchAll(/(?:^|\n)\s*(Day\s*\d+|第[一二三四五六七八九十\d]+天|D\d+)[：:\s-]*/gi)]
  const sections = matches.length
    ? matches.map((match, index) => ({
        title: normalizeDayTitle(match[1], index),
        text: content.slice((match.index || 0) + match[0].length, matches[index + 1]?.index ?? content.length).trim(),
      }))
    : Array.from({ length: Math.max(1, plan?.days || 1) }, (_, index) => ({ title: `Day ${index + 1}`, text: content }))
  const hotel = hotelInfo(plan)
  return sections.map(section => ({ ...section, nodes: buildNodes(section.text, hotel) }))
}

function buildNodes(text: string, hotel: string): FlowNode[] {
  return [
    node('attraction', iconForAttraction(text), extractPlanLine(text, /景点|打卡|游览|海滩|古镇|山|公园|博物馆|景区/), text),
    node('food', '🍜', extractPlanLine(text, /美食|餐|小吃|海鲜|午餐|晚餐|咖啡|夜市/), text),
    node('hotel', '🏨', hotel || extractPlanLine(text, /酒店|住宿|入住|民宿|休息/), text),
  ]
}

function node(type: FlowType, icon: string, summary: string, dayText: string): FlowNode {
  const label = ({ attraction: '景点', food: '美食', hotel: '住宿' } as Record<FlowType, string>)[type]
  const detail = summary || fallbackDetail(type, dayText)
  return {
    type,
    icon,
    label,
    summary: short(detail),
    detail,
    tip: tipFor(type),
    booking: bookingFor(type),
  }
}

function extractPlanLine(text: string, pattern: RegExp) {
  const line = text.split(/\n|。|；|;/).map(x => x.trim()).find(item => pattern.test(item)) || ''
  return line.replace(/^(景点|打卡点|游览|美食|餐饮|午餐|晚餐|住宿|酒店|入住)[：:\s-]*/, '')
}

function hotelInfo(plan?: Plan) {
  const text = plan?.content || ''
  const match = text.match(/(?:^|\n)\s*(?:酒店|住宿)[：:\s-]*([^\n]+)/)
  return match?.[1]?.trim() || ''
}

function fallbackDetail(type: FlowType, text: string) {
  const map = {
    attraction: '按当天安排游览核心景点，注意开放时间和现场客流。',
    food: '按当天安排体验当地美食，避开明显排队过长或评价异常的店。',
    hotel: '按住宿区域安排返程路线，优先考虑交通便利和安全性。',
  }
  return `${map[type]}\n\n完整内容：\n${text}`
}

function iconForAttraction(text: string) {
  if (/海|沙滩|海滩|岛/.test(text)) return '🏖️'
  if (/山|森林|峡谷|草原/.test(text)) return '⛰️'
  return '🏯'
}

function tipFor(type: FlowType) {
  return {
    attraction: '热门景点尽量错峰，先确认开放时间、闭园时间和实名预约规则。',
    food: '优先看近期评价，避免只看单个平台推荐；海鲜、夜市注意明码标价。',
    hotel: '入住前确认取消政策、押金、早餐、停车和到景点的实际通勤时间。',
  }[type]
}

function bookingFor(type: FlowType) {
  return {
    attraction: '景区公众号、官方小程序、携程/美团等平台提前预约或购票。',
    food: '大众点评、美团、小红书/抖音查看近期反馈，热门餐厅提前电话确认。',
    hotel: '携程、飞猪、美团或酒店官方渠道预订，付款前核对房型和入住日期。',
  }[type]
}

function short(text: string) {
  return text.replace(/\s+/g, ' ').slice(0, 34)
}

function normalizeDayTitle(title: string, index: number) {
  const digit = title.match(/\d+/)?.[0]
  return digit ? `Day ${digit}` : title.replace(/\s+/g, '') || `Day ${index + 1}`
}

function inferDestination(plan: Plan) {
  return plan.destination || plan.title.match(/去([\u4e00-\u9fa5]{2,8})/)?.[1] || '旅行目的地'
}

function inferDays(plan: Plan) {
  return plan.days || Number(plan.content?.match(/(\d+)\s*天/)?.[1] || 1)
}

function listTitle(plan: Plan) {
  if (plan.title) return plan.title
  return `${plan.destination || inferDestination(plan)} · ${inferDays(plan)}日游`
}

function openNode(day: DayPlan, node: FlowNode) {
  selectedDay.value = day
  selectedNode.value = node
  nodeVisible.value = true
}

function renderChart() {
  if (!chartRef.value || !current.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const totals = structuredDays.value.flatMap(day => day.nodes).reduce<Record<string, number>>((acc, node) => {
    acc[node.label] = (acc[node.label] || 0) + 1
    return acc
  }, {})
  chart.setOption({
    color: ['#34c759', '#ffb340', '#4f8cff'],
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      data: Object.entries(totals).map(([name, value]) => ({ name, value })),
      label: { formatter: '{b} {d}%' },
    }],
  })
  chart.resize()
}

watch(() => route.params.id, load)
watch(structuredDays, () => nextTick(renderChart))
onMounted(load)
onBeforeUnmount(() => chart?.dispose())
</script>
