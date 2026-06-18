<template>
  <section class="grid gap-5 max-lg:min-h-[calc(100vh-150px)] lg:h-[calc(100vh-180px)] lg:grid-cols-[minmax(0,1fr)_minmax(340px,380px)] lg:overflow-hidden">
    <div class="liquid flex min-h-[calc(100vh-150px)] flex-col overflow-hidden rounded-[28px] lg:h-full lg:min-h-0">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-white/20 p-4 md:p-5">
        <div>
          <h1 class="m-0 text-2xl font-800 md:text-3xl">TravelMind AI</h1>
          <p class="m-0 mt-1 text-sm text-[var(--muted)]">你可以一直追问，我会记住前面的想法，陪你把行程聊清楚。</p>
        </div>
        <div class="flex gap-2">
          <n-button round :disabled="messages.length <= 1" @click="savePlan">保存行程</n-button>
          <n-button round @click="newChat">新会话</n-button>
        </div>
      </div>

      <div ref="chatBody" class="min-h-0 flex-1 space-y-4 overflow-y-auto p-4 md:p-6">
        <div v-for="item in messages" :key="item.id" class="flex" :class="item.role === 'user' ? 'justify-end' : 'justify-start'">
          <div
            class="max-w-[86%] rounded-[24px] px-4 py-3 leading-7 shadow-sm md:max-w-[74%]"
            :class="item.role === 'user'
              ? 'bg-[var(--button-bg)] text-[var(--button-text)]'
              : 'bg-white/62 text-[var(--text)] dark:bg-white/8'"
          >
            <div v-if="item.role === 'assistant'" class="mb-1 text-xs font-700 text-[var(--muted)]">TravelMind</div>
            <div class="whitespace-pre-wrap break-words">{{ item.content }}</div>
          </div>
        </div>
        <div v-if="loading" class="flex justify-start">
          <div class="rounded-[24px] bg-white/62 px-4 py-3 text-sm text-[var(--muted)] dark:bg-white/8">正在思考...</div>
        </div>
      </div>

      <div class="border-t border-white/20 p-4 md:p-5">
        <div v-if="showExamples" class="mb-3 rounded-[22px] bg-white/35 p-3 dark:bg-white/6">
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="text-xs font-700 text-[var(--muted)]">你可能想问</span>
            <button class="rounded-full px-2 py-1 text-sm text-[var(--muted)] transition hover:bg-white/60 hover:text-[var(--text)] dark:hover:bg-white/10" title="关闭" @click="showExamples = false">×</button>
          </div>
          <div class="flex flex-wrap gap-2">
            <button v-for="x in examples" :key="x" class="rounded-2xl bg-white/60 px-3 py-2 text-sm transition hover:bg-white/80 dark:bg-white/10 dark:hover:bg-white/16" @click="message = x">{{ x }}</button>
          </div>
        </div>
        <div class="flex items-end gap-3">
          <n-input
            v-model:value="message"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 8 }"
            placeholder="问我：杭州出发，预算3000，想去海边玩3天..."
            @keydown.enter.exact.prevent="ask"
          />
          <n-button type="primary" round :loading="loading" :disabled="!message.trim()" @click="ask">发送</n-button>
        </div>
      </div>
    </div>

    <aside class="min-w-0 space-y-5 lg:h-full lg:overflow-y-auto lg:pr-1">
      <div class="liquid rounded-[28px] p-4">
        <div class="mb-3 flex items-start justify-between gap-3">
          <div>
            <h2 class="m-0 text-xl">预算示意</h2>
            <p class="m-0 mt-1 text-xs text-[var(--muted)]">选中哪个方案，这里就看哪个方案的花费。</p>
          </div>
          <div class="rounded-2xl bg-white/55 px-3 py-1 text-sm font-700 dark:bg-white/10">￥{{ activeBudget.total }}</div>
        </div>
        <div v-if="budgetPlans.length > 1" class="mb-3 flex flex-wrap gap-2">
          <button
            v-for="(plan, index) in budgetPlans"
            :key="plan.title"
            class="rounded-2xl px-3 py-1.5 text-xs transition"
            :class="selectedBudgetIndex === index ? 'bg-[var(--button-primary-bg)] text-white' : 'bg-white/55 text-[var(--text)] hover:bg-white/75 dark:bg-white/10 dark:hover:bg-white/16'"
            @click="selectBudgetPlan(index)"
          >
            {{ plan.title }}
          </button>
        </div>
        <div ref="chartRef" class="h-[260px] min-h-[260px] w-full overflow-visible"></div>
        <div class="mt-3 grid grid-cols-2 gap-2 text-xs">
          <div v-for="(item, index) in activeBudget.items" :key="item.name" class="rounded-2xl bg-white/45 px-3 py-2 dark:bg-white/6">
            <div class="text-[var(--muted)]">{{ item.name }}</div>
            <n-input-number
              class="mt-1 w-full"
              size="small"
              :min="0"
              :step="50"
              :value="item.value"
              @update:value="updateBudgetItem(index, $event)"
            >
              <template #prefix>￥</template>
            </n-input-number>
          </div>
        </div>
        <n-button class="mt-4 w-full" round @click="socialVisible = true">
          小红书 / 抖音攻略
        </n-button>
      </div>

      <div class="liquid rounded-[28px] p-5">
        <h2 class="m-0 mb-3 text-xl">对话记忆</h2>
        <div class="space-y-3 text-sm text-[var(--muted)]">
          <p class="m-0">当前会话：{{ conversationId ? `#${conversationId}` : '新会话' }}</p>
          <p class="m-0">你不用重复前面的条件，直接说“第二天轻松一点”“酒店预算降到500”就行。</p>
        </div>
      </div>
    </aside>
  </section>

  <section class="mt-5 liquid rounded-[28px] p-5 md:p-7">
    <div class="mb-4 flex items-end justify-between gap-4">
      <div>
        <h2 class="m-0 text-2xl">当前季节适合去</h2>
        <p class="m-0 mt-2 text-sm text-[var(--muted)]">{{ seasonalTip }} 我会优先给你找高德实时 POI 里的当季去处。</p>
      </div>
      <router-link to="/app/attractions" class="text-sm text-[var(--button-text)]">查看全部</router-link>
    </div>
    <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      <article v-for="item in seasonal" :key="item.id" class="overflow-hidden rounded-[24px] bg-white/45 dark:bg-white/6" @click="goOnlineAttraction(item)">
        <img :src="realAttractionImage(item)" :alt="item.name" class="h-44 w-full cursor-pointer object-cover" @error="fallbackImage($event, item)" />
        <div class="p-4">
          <div class="flex items-center justify-between gap-3">
            <h3 class="m-0 text-lg">{{ item.name }}</h3>
            <span class="rounded-full bg-white/60 px-2 py-1 text-xs dark:bg-white/10">{{ item.score || '实时' }}</span>
          </div>
          <p class="line-clamp-2 text-sm text-[var(--muted)]">{{ item.description }}</p>
          <div class="mt-2 text-sm">{{ item.city || '高德 POI' }} · {{ item.tags || item.bestSeason }}</div>
        </div>
      </article>
    </div>
  </section>

  <n-modal v-model:show="socialVisible" preset="card" class="max-w-[920px]" :bordered="false">
    <SocialRecommendations
      title="当前方案热门攻略"
      :place="socialPlace"
      :city="socialCity"
      scene="attraction"
    />
  </n-modal>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useMessage } from 'naive-ui'
import AMapLoader from '@amap/amap-jsapi-loader'
import SocialRecommendations from '../components/SocialRecommendations.vue'
import { http, type Attraction } from '../api'
import { realAttractionImage } from '../realAttractionImages'
import { fallbackPlaceImage } from '../imageFallback'
import { saveOnlineAttraction } from '../onlineDetail'
import { seasonalAttractionKeyword, seasonalAttractionQueries, seasonalAttractionTip } from '../seasonalTravel'
import { useThemeStore } from '../stores/theme'
import { useUserStore } from '../stores/user'

const toast = useMessage()
const router = useRouter()
const theme = useThemeStore()
const user = useUserStore()
const message = ref('我在杭州，预算3000元，想去海边玩3天，喜欢美食和拍照')
const loading = ref(false)
const chartRef = ref<HTMLDivElement>()
const chatBody = ref<HTMLDivElement>()
const seasonal = ref<Attraction[]>([])
const seasonalTip = seasonalAttractionTip()
const conversationId = ref<number>()
const socialVisible = ref(false)
const showExamples = ref(true)
const messages = ref<ChatMessage[]>(defaultMessages())
const examples = [
  '上海去成都玩4天，预算4000，喜欢美食和夜景',
  '北京出发，想看雪景，玩5天，预算6000',
  '广州出发亲子游3天，预算3500，想轻松一点',
]

type ChatMessage = {
  id: string
  role: 'user' | 'assistant'
  content: string
}

type ChatResp = {
  conversationId: number
  answer: string
}

type BudgetItem = {
  name: string
  value: number
}

type BudgetPlan = {
  title: string
  total: number
  items: BudgetItem[]
}

let budgetChart: echarts.ECharts | undefined
let resizeObserver: ResizeObserver | undefined
let restoringChat = false
const selectedBudgetIndex = ref(0)
const budgetPlans = ref<BudgetPlan[]>([fallbackBudgetPlan(3000, '默认方案')])
const activeBudget = computed(() => budgetPlans.value[selectedBudgetIndex.value] || budgetPlans.value[0])
const latestTravelText = computed(() => [...messages.value].reverse().map(item => item.content).join('\n'))
const socialCity = computed(() => extractCity(latestTravelText.value))
const socialPlace = computed(() => extractSocialPlace(latestTravelText.value) || socialCity.value || '旅行攻略')

async function ask() {
  const text = message.value.trim()
  if (!text || loading.value) return
  messages.value.push({ id: crypto.randomUUID(), role: 'user', content: text })
  message.value = ''
  loading.value = true
  try {
    const data = await http.post<ChatResp | string>('/api/ai/chat', { conversationId: conversationId.value, message: text })
    const answer = typeof data === 'string' ? data : data.answer
    if (typeof data !== 'string') conversationId.value = data.conversationId
    messages.value.push({ id: crypto.randomUUID(), role: 'assistant', content: answer })
    setBudgetPlans(parseBudgetPlans(`${text}\n${answer}`))
  } catch (e) {
    toast.error((e as Error).message)
  } finally {
    loading.value = false
    await scrollBottom()
  }
}

async function savePlan() {
  if (!localStorage.getItem('token')) {
    toast.warning('登录后才可以保存行程')
    return
  }
  const content = messages.value.map(x => `${x.role === 'user' ? '我' : 'TravelMind'}：${x.content}`).join('\n\n')
  try {
    await http.post('/api/plans', {
      title: messages.value.find(x => x.role === 'user')?.content.slice(0, 30) || 'AI生成旅行计划',
      destination: 'AI推荐目的地',
      budget: activeBudget.value.total,
      days: 3,
      content,
    })
    toast.success('已保存')
  } catch (e) {
    toast.error((e as Error).message || '保存失败')
  }
}

function fallbackImage(e: Event, item: Attraction) {
  fallbackPlaceImage(e, item)
}

function goOnlineAttraction(item: Attraction) {
  saveOnlineAttraction(item)
  router.push(`/app/attractions/online-${item.id}`)
}

function newChat() {
  conversationId.value = undefined
  message.value = ''
  messages.value = defaultMessages('新会话已开始。你可以直接告诉我出发地、预算、天数、偏好。')
  setBudgetPlans([fallbackBudgetPlan(3000, '默认方案')])
  saveChatCache()
}

async function scrollBottom() {
  await nextTick()
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight
}

function selectBudgetPlan(index: number) {
  selectedBudgetIndex.value = index
  nextTick(renderBudgetChart)
}

function setBudgetPlans(plans: BudgetPlan[]) {
  budgetPlans.value = plans.length ? plans : [fallbackBudgetPlan(3000, '默认方案')]
  selectedBudgetIndex.value = 0
  nextTick(renderBudgetChart)
}

function updateBudgetItem(index: number, value: number | null) {
  const plan = activeBudget.value
  if (!plan?.items[index]) return
  plan.items[index].value = Math.max(0, Math.round(value || 0))
  plan.total = plan.items.reduce((sum, item) => sum + item.value, 0)
  nextTick(renderBudgetChart)
}

function parseBudgetPlans(text: string): BudgetPlan[] {
  const sections = splitPlanSections(text)
  const fallbackTotal = extractRequestedBudget(text) || 3000
  return sections.map((section, index) => parseBudgetPlan(section.body, section.title || `方案${index + 1}`, fallbackTotal))
}

function splitPlanSections(text: string) {
  const pattern = /(方案\s*[一二三四五六七八九十\d]+|路线\s*[一二三四五六七八九十\d]+|Plan\s*\d+)/gi
  const matches = [...text.matchAll(pattern)]
  if (!matches.length) return [{ title: '当前方案', body: text }]
  return matches.map((match, index) => ({
    title: normalizePlanTitle(match[1], index),
    body: text.slice(match.index || 0, matches[index + 1]?.index ?? text.length),
  }))
}

function normalizePlanTitle(title: string, index: number) {
  const digit = title.match(/\d+/)?.[0]
  return digit ? `方案${digit}` : title.replace(/\s+/g, '') || `方案${index + 1}`
}

function parseBudgetPlan(text: string, title: string, fallbackTotal: number): BudgetPlan {
  const groups = [
    { name: '交通', aliases: ['交通', '高铁', '机票', '航班', '火车', '打车', '地铁', '往返'] },
    { name: '酒店', aliases: ['酒店', '住宿', '民宿', '客栈', '入住'] },
    { name: '餐饮', aliases: ['餐饮', '美食', '吃饭', '餐费', '海鲜', '小吃'] },
    { name: '景点', aliases: ['景点', '门票', '船票', '项目', '游玩', '景区'] },
    { name: '机动', aliases: ['机动', '其他', '购物', '备用', '剩余', '保险'] },
  ]
  const values = new Map(groups.map(group => [group.name, 0]))
  for (const line of text.split(/\n+/)) {
    const normalized = line.replace(/\s+/g, '')
    const matchedGroups = groups.filter(group => group.aliases.some(alias => normalized.includes(alias)))
    const isTotalLine = /总计|合计|总预算|预计总费用|预算/.test(normalized)
    for (const group of groups) {
      const alias = group.aliases.find(alias => normalized.includes(alias))
      if (!alias) continue
      const amount = extractAliasAmount(normalized, alias)
      if (amount) {
        values.set(group.name, (values.get(group.name) || 0) + amount)
      } else if (matchedGroups.length === 1 && !isTotalLine) {
        values.set(group.name, (values.get(group.name) || 0) + extractLineAmount(normalized))
      }
    }
  }
  const planTotal = extractExplicitTotal(text) || fallbackTotal
  let items = groups.map(group => ({ name: group.name, value: Math.round(values.get(group.name) || 0) }))
    .filter(item => item.value > 0)
  if (items.length < 2) return fallbackBudgetPlan(planTotal, title)
  items = fitBudgetItems(items, planTotal)
  return { title, total: planTotal, items }
}

function fitBudgetItems(items: BudgetItem[], total: number) {
  const sum = items.reduce((value, item) => value + item.value, 0)
  if (sum <= 0) return fallbackBudgetPlan(total, '当前方案').items
  if (sum < total) {
    const rest = total - sum
    const other = items.find(item => item.name === '机动')
    if (other) other.value += rest
    else items.push({ name: '机动', value: rest })
    return items
  }
  if (sum === total) return items
  let scaled = items.map(item => ({ ...item, value: Math.max(1, Math.round(item.value * total / sum)) }))
  const diff = total - scaled.reduce((value, item) => value + item.value, 0)
  if (diff) {
    const target = scaled.find(item => item.name === '机动') || scaled[scaled.length - 1]
    target.value = Math.max(1, target.value + diff)
  }
  return scaled
}

function extractAliasAmount(line: string, alias: string) {
  const escaped = alias.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const after = line.match(new RegExp(`${escaped}[^\\d]{0,8}(?:￥|¥)?(\\d{2,6})(?:\\.\\d+)?(?:元|块)?`))
  if (after) return Number(after[1])
  const before = line.match(new RegExp(`(?:￥|¥)?(\\d{2,6})(?:\\.\\d+)?(?:元|块)?[^\\d]{0,8}${escaped}`))
  return before ? Number(before[1]) : 0
}

function extractLineAmount(line: string) {
  const afterEqual = line.match(/[=＝]\s*(?:￥|¥)?\s*(\d{2,6})/)
  if (afterEqual) return Number(afterEqual[1])
  const values = [...line.matchAll(/(?:￥|¥)?\s*(\d{2,6})(?:\.\d+)?\s*(?:元|块)?/g)]
    .map(match => Number(match[1]))
    .filter(value => value >= 20)
  return values.length ? Math.max(...values) : 0
}

function extractRequestedBudget(text: string) {
  return Number(text.match(/(?:预算|人均|总预算)[^\d]{0,12}(\d{3,6})/)?.[1] || 0)
}

function extractExplicitTotal(text: string) {
  return Number(text.match(/(?:总计|合计|总预算|预计总费用|预算)[^\d]{0,12}(\d{3,6})/)?.[1] || 0)
}

function extractCity(text: string) {
  return text.match(/(?:在|从|去|到|目的地[：:]?)\s*([\u4e00-\u9fa5]{2,8})(?:出发|玩|旅游|旅行|打卡|美食|海边|古镇|雪景|$)/)?.[1] || ''
}

function extractSocialPlace(text: string) {
  const direct = text.match(/(?:推荐城市|目的地|推荐目的地|城市)[：:\s]*([\u4e00-\u9fa5]{2,12})/)?.[1]
  if (direct) return direct
  return text.match(/去([\u4e00-\u9fa5]{2,12})(?:玩|旅游|旅行|打卡|吃|看|$)/)?.[1] || ''
}

function fallbackBudgetPlan(total: number, title: string): BudgetPlan {
  const safeTotal = Math.max(500, Math.round(total || 3000))
  return {
    title,
    total: safeTotal,
    items: [
      { name: '交通', value: Math.round(safeTotal * 0.3) },
      { name: '酒店', value: Math.round(safeTotal * 0.34) },
      { name: '餐饮', value: Math.round(safeTotal * 0.18) },
      { name: '景点', value: Math.round(safeTotal * 0.1) },
      { name: '机动', value: Math.round(safeTotal * 0.08) },
    ],
  }
}

function renderBudgetChart() {
  if (!chartRef.value) return
  if (!budgetChart) budgetChart = echarts.init(chartRef.value)
  const plan = activeBudget.value
  const dark = theme.dark
  const titleColor = dark ? '#f5f7fb' : '#263445'
  const subtextColor = dark ? '#c7d2e0' : '#667085'
  budgetChart.setOption({
    color: ['#4f8cff', '#34c759', '#ffb340', '#ff6b6b', '#8b7cf6'],
    tooltip: { trigger: 'item', formatter: '{b}<br/>￥{c} ({d}%)' },
    legend: {
      bottom: 4,
      left: 'center',
      itemWidth: 9,
      itemHeight: 9,
      textStyle: { color: subtextColor, fontSize: 11 },
    },
    title: {
      text: `￥${plan.total}`,
      subtext: plan.title,
      left: 'center',
      top: '34%',
      textStyle: { fontSize: 22, fontWeight: 800, color: titleColor },
      subtextStyle: { fontSize: 11, color: subtextColor },
    },
    series: [{
      type: 'pie',
      radius: ['42%', '62%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: { show: false },
      labelLine: { show: false },
      itemStyle: { borderRadius: 8, borderColor: 'rgba(255,255,255,.75)', borderWidth: 2 },
      data: plan.items,
    }],
  }, true)
  budgetChart.resize()
}

watch(messages, () => {
  saveChatCache()
  scrollBottom()
}, { deep: true })

watch(conversationId, () => {
  saveChatCache()
})

onMounted(async () => {
  if (user.token && !user.user) await user.fetchMe()
  restoreChatCache()
  await scrollBottom()
  seasonal.value = await loadOnlineSeasonalAttractions()
  await nextTick()
  renderBudgetChart()
  window.addEventListener('resize', resizeBudgetChart)
  if (chartRef.value) {
    resizeObserver = new ResizeObserver(resizeBudgetChart)
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => chatOwnerKey(), () => {
  restoreChatCache()
})

function defaultMessages(content = '你好，我是 TravelMind AI。告诉我出发地、预算、天数和偏好，我会帮你规划目的地、交通、酒店、美食和每日行程。') {
  return [{ id: crypto.randomUUID(), role: 'assistant' as const, content }]
}

function chatOwnerKey() {
  if (!user.token) return ''
  return user.user?.id ? `user-${user.user.id}` : `token-${jwtSubject(user.token)}`
}

function chatStorageKey() {
  const owner = chatOwnerKey()
  return owner ? `travelmind-chat-${owner}` : ''
}

function saveChatCache() {
  if (restoringChat) return
  const key = chatStorageKey()
  if (!key) return
  localStorage.setItem(key, JSON.stringify({ conversationId: conversationId.value, messages: messages.value }))
}

function restoreChatCache() {
  restoringChat = true
  const key = chatStorageKey()
  const cache = key ? localStorage.getItem(key) : ''
  if (cache) {
    try {
      const data = JSON.parse(cache)
      if (Array.isArray(data.messages) && data.messages.length) {
        messages.value = data.messages
        conversationId.value = data.conversationId
        const latestAssistant = [...messages.value].reverse().find(item => item.role === 'assistant')?.content
        if (latestAssistant) setBudgetPlans(parseBudgetPlans(latestAssistant))
        restoringChat = false
        return
      }
    } catch {}
  }
  conversationId.value = undefined
  messages.value = defaultMessages()
  setBudgetPlans([fallbackBudgetPlan(3000, '默认方案')])
  restoringChat = false
}

function jwtSubject(token: string) {
  try {
    return JSON.parse(atob(token.split('.')[1] || '')).sub || token.slice(-12)
  } catch {
    return token.slice(-12)
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeBudgetChart)
  resizeObserver?.disconnect()
  budgetChart?.dispose()
})

function resizeBudgetChart() {
  budgetChart?.resize()
}

watch(() => theme.dark, () => nextTick(renderBudgetChart))

async function loadOnlineSeasonalAttractions() {
  const key = import.meta.env.VITE_AMAP_KEY
  if (!key) return []
  try {
    const results = await http.get<Attraction[]>('/api/attractions/online', {
      params: { keyword: seasonalAttractionKeyword(), key, limit: 6 },
    })
    if (results.length) return results.map(item => ({ ...item, source: 'online' as const }))
  } catch {
  }
  return loadSeasonalAttractionsByJs()
}

async function loadSeasonalAttractionsByJs() {
  const groups = await Promise.allSettled(
    seasonalAttractionQueries().slice(0, 3).map(query => searchAmapPoi(query.keyword, query.city, 3))
  )
  const seen = new Set<string>()
  return groups.flatMap(group => group.status === 'fulfilled' ? group.value : [])
    .filter(item => {
      const key = `${item.name}-${item.city}`
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
    .slice(0, 6)
}

async function searchAmapPoi(keyword: string, city: string, pageSize: number) {
  const key = import.meta.env.VITE_AMAP_KEY
  const securityJsCode = import.meta.env.VITE_AMAP_SECURITY_CODE
  if (securityJsCode) (window as any)._AMapSecurityConfig = { securityJsCode }
  const AMap = await AMapLoader.load({ key, version: '2.0', plugins: ['AMap.PlaceSearch'] })
  const placeSearch = new AMap.PlaceSearch({ city, citylimit: true, extensions: 'all', pageSize, pageIndex: 1 })
  const result = await new Promise<any[]>((resolve, reject) => {
    placeSearch.search(keyword, (status: string, res: any) => {
      if (status === 'complete' && res?.poiList?.pois) resolve(res.poiList.pois)
      else reject(new Error(res?.info || '高德在线搜索失败'))
    })
  })
  return result.map((poi, index) => ({
    id: Number(`${Date.now()}${index}`),
    name: poi.name,
    city: poi.cityname || city,
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
    source: 'online' as const,
  }))
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
</script>
