<template>
  <section class="grid gap-5 max-lg:min-h-[calc(100vh-150px)] lg:h-[calc(100vh-180px)] lg:grid-cols-[minmax(0,1fr)_minmax(340px,380px)] lg:overflow-hidden">
    <div class="liquid flex min-h-[calc(100vh-150px)] flex-col overflow-hidden rounded-[28px] lg:h-full lg:min-h-0">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-white/20 p-4 md:p-5">
        <div>
          <h1 class="m-0 text-2xl font-800 md:text-3xl">TravelMind AI</h1>
        </div>
        <div class="flex gap-2">
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
            <div v-if="item.role === 'assistant' && item.content" class="mt-2 flex flex-wrap gap-2">
              <n-button size="tiny" round @click="copyAnswer(item.content)">复制</n-button>
              <n-button v-if="isLastAssistant(item)" size="tiny" round @click="regenerateAnswer">重新生成</n-button>
            </div>
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
          <n-button v-if="loading" round @click="stopGeneration">停止</n-button>
          <n-button v-else type="primary" round :disabled="!message.trim()" @click="ask">发送</n-button>
        </div>
      </div>
    </div>

    <aside class="min-w-0 space-y-5 lg:h-full lg:overflow-y-auto lg:pr-1">
      <div class="liquid rounded-[28px] p-4">
        <div class="mb-3 flex items-start justify-between gap-3">
          <div>
            <h2 class="m-0 text-xl">预算示意</h2>
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
        <div class="mb-3 flex items-center justify-between gap-3">
          <h2 class="m-0 text-xl">历史会话</h2>
          <n-button size="small" round @click="newChat">新会话</n-button>
        </div>
        <div v-if="!user.token" class="rounded-2xl bg-white/42 p-3 text-sm text-[var(--muted)] dark:bg-white/6">
          登录后可同步保存会话。
        </div>
        <div v-else class="max-h-[280px] space-y-2 overflow-y-auto pr-1">
          <div
            v-for="item in conversations"
            :key="item.id"
            class="flex w-full items-center gap-2 rounded-2xl px-3 py-2 text-left text-sm transition"
            :class="conversationId === item.id ? 'bg-[var(--button-primary-bg)] text-white' : 'bg-white/42 hover:bg-white/65 dark:bg-white/6 dark:hover:bg-white/12'"
            @click="openConversation(item.id)"
          >
            <div class="min-w-0 flex-1">
              <div class="line-clamp-1 font-700">{{ item.title || '新的旅行咨询' }}</div>
              <div class="mt-1 text-xs opacity-75">{{ item.updateTime || item.createTime || '' }}</div>
            </div>
            <n-popconfirm @positive-click="deleteConversation(item.id)">
              <template #trigger>
                <button
                  class="rounded-full px-2 py-1 text-xs opacity-70 transition hover:bg-white/40 hover:opacity-100 dark:hover:bg-white/10"
                  :disabled="deletingConversationId === item.id"
                  @click.stop
                >
                  删除
                </button>
              </template>
              删除这条历史会话？
            </n-popconfirm>
          </div>
          <div v-if="!conversations.length" class="rounded-2xl bg-white/42 p-3 text-sm text-[var(--muted)] dark:bg-white/6">
            暂无历史会话
          </div>
        </div>
      </div>

      <div class="liquid rounded-[28px] p-5">
        <div class="mb-3 flex items-center justify-between gap-3">
          <h2 class="m-0 text-xl">目的地天气</h2>
          <span class="rounded-2xl bg-white/55 px-3 py-1 text-xs font-700 dark:bg-white/10">{{ weatherCity }}</span>
        </div>
        <div class="mb-3 flex gap-2">
          <n-input v-model:value="weatherQuery" size="small" placeholder="查询城市天气" clearable @keydown.enter="loadWeather(weatherQuery)" />
          <n-button size="small" round :loading="weatherLoading" @click="loadWeather(weatherQuery)">查询</n-button>
        </div>
        <div v-if="weatherLoading" class="text-sm text-[var(--muted)]">正在更新天气...</div>
        <div v-else-if="weather" class="grid gap-3">
          <div class="flex items-end justify-between gap-3">
            <div class="text-4xl font-900">{{ weather.temperature }}℃</div>
            <div class="text-right">
              <div class="font-800">{{ weather.weather }}</div>
              <div class="mt-1 text-xs text-[var(--muted)]">{{ weather.province }}{{ weather.city }}</div>
            </div>
          </div>
          <div class="grid grid-cols-2 gap-2 text-xs text-[var(--muted)]">
            <div class="rounded-2xl bg-white/45 px-3 py-2 dark:bg-white/6">湿度 {{ weather.humidity }}%</div>
            <div class="rounded-2xl bg-white/45 px-3 py-2 dark:bg-white/6">{{ weather.windDirection }}风 {{ weather.windPower }}级</div>
          </div>
          <div v-if="weather.forecasts?.length" class="grid gap-2">
            <div
              v-for="item in weather.forecasts.slice(0, 3)"
              :key="item.date"
              class="flex items-center justify-between rounded-2xl bg-white/42 px-3 py-2 text-xs dark:bg-white/6"
            >
              <span>{{ item.date }}</span>
              <span>{{ item.dayWeather }}</span>
              <span>{{ item.nightTemp }}-{{ item.dayTemp }}℃</span>
            </div>
          </div>
          <div class="text-xs text-[var(--muted)]">更新于 {{ weather.reportTime }}</div>
        </div>
        <div v-else class="text-sm text-[var(--muted)]">{{ weatherError || '暂未获取到天气' }}</div>
      </div>
    </aside>
  </section>

  <section class="mt-5 liquid rounded-[28px] p-5 md:p-7">
    <div class="mb-4 flex items-end justify-between gap-4">
      <div>
        <h2 class="m-0 text-2xl">当前季节适合去</h2>
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
import { http, type AiConversation, type AiMessage, type Attraction, type WeatherInfo } from '../api'
import { poiLatitude, poiLongitude, poiPhoto, staticMap, type AmapPoi } from '../amapPoi'
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
const conversations = ref<AiConversation[]>([])
const deletingConversationId = ref<number>()
const socialVisible = ref(false)
const showExamples = ref(true)
const messages = ref<ChatMessage[]>(defaultMessages())
const examples = [
  '上海去成都玩4天，预算4000，喜欢美食和夜景',
  '北京出发，想看雪景，玩5天，预算6000',
  '广州出发亲子游3天，预算3500，想轻松一点',
]
const knownCities = [
  '北京', '上海', '广州', '深圳', '杭州', '成都', '重庆', '西安', '南京', '苏州', '厦门', '青岛', '三亚',
  '大理', '丽江', '桂林', '长沙', '武汉', '宁波', '舟山', '台州', '福州', '天津', '哈尔滨', '长春',
  '沈阳', '大连', '昆明', '贵阳', '拉萨', '乌鲁木齐', '呼伦贝尔', '张家界', '香港', '澳门', '东京',
  '大阪', '京都', '首尔', '曼谷', '新加坡', '巴黎', '伦敦', '罗马', '纽约', '洛杉矶',
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
const socialCity = computed(() => destinationCity(latestTravelText.value) || extractCity(latestTravelText.value))
const socialPlace = computed(() => extractSocialPlace(latestTravelText.value) || socialCity.value || '旅行攻略')
const weatherCity = computed(() => destinationCity(latestTravelText.value) || extractSocialPlace(latestTravelText.value) || socialCity.value || '三亚')
const weather = ref<WeatherInfo>()
const weatherLoading = ref(false)
const weatherError = ref('')
const weatherQuery = ref(weatherCity.value)
let chatAbortController: AbortController | undefined

async function ask() {
  await askText(message.value)
}

async function askText(rawText: string, appendUser = true) {
  const text = rawText.trim()
  if (!text || loading.value) return
  if (!user.token) {
    toast.warning('登录后可以使用 AI 对话')
    return
  }
  const requestBudget = extractRequestedBudget(text) || 3000
  if (appendUser) messages.value.push({ id: crypto.randomUUID(), role: 'user', content: text })
  const assistant = { id: crypto.randomUUID(), role: 'assistant' as const, content: '' }
  messages.value.push(assistant)
  message.value = ''
  loading.value = true
  setBudgetPlans([fallbackBudgetPlan(requestBudget, '生成中')])
  try {
    const aiMessage = `${text}\n\n请按本次问题重新生成旅行建议，并给出可直接计算的预算明细：交通、酒店、餐饮、景点、机动。每一项都写明确金额，最后写预计总费用。不要沿用上一轮预算金额。`
    await streamChat(aiMessage, assistant)
    setBudgetPlans(parseBudgetPlans(assistant.content, requestBudget))
    await loadConversations()
  } catch (e) {
    if ((e as Error).name === 'AbortError') {
      if (!assistant.content.trim()) messages.value = messages.value.filter(item => item.id !== assistant.id)
      else toast.info('已停止生成')
    } else {
      messages.value = messages.value.filter(item => item.id !== assistant.id)
      toast.error((e as Error).message)
    }
  } finally {
    loading.value = false
    await scrollBottom()
  }
}

function fallbackImage(e: Event, item: Attraction) {
  fallbackPlaceImage(e, item)
}

function stopGeneration() {
  chatAbortController?.abort()
  loading.value = false
}

async function regenerateAnswer() {
  if (loading.value) return
  const lastUser = [...messages.value].reverse().find(item => item.role === 'user')?.content
  if (!lastUser) {
    toast.warning('没有可重新生成的问题')
    return
  }
  const lastAssistantIndex = messages.value.map(item => item.role).lastIndexOf('assistant')
  if (lastAssistantIndex >= 0) messages.value.splice(lastAssistantIndex, 1)
  await askText(lastUser, false)
}

async function copyAnswer(content: string) {
  try {
    await navigator.clipboard.writeText(content)
    toast.success('已复制')
  } catch {
    toast.error('复制失败')
  }
}

function isLastAssistant(item: ChatMessage) {
  return [...messages.value].reverse().find(message => message.role === 'assistant')?.id === item.id
}

function goOnlineAttraction(item: Attraction) {
  saveOnlineAttraction(item)
  router.push(`/app/attractions/online-${item.id}`)
}

function newChat() {
  conversationId.value = undefined
  message.value = ''
  messages.value = defaultMessages('新会话已开始。')
  setBudgetPlans([fallbackBudgetPlan(3000, '默认方案')])
  saveChatCache()
}

async function loadConversations() {
  if (!user.token) {
    conversations.value = []
    return
  }
  try {
    conversations.value = await http.get<AiConversation[]>('/api/ai/conversations')
  } catch {
    conversations.value = []
  }
}

async function deleteConversation(id: number) {
  deletingConversationId.value = id
  try {
    await http.delete(`/api/ai/conversations/${id}`)
    conversations.value = conversations.value.filter(item => item.id !== id)
    if (conversationId.value === id) newChat()
    toast.success('已删除')
  } catch (e) {
    toast.error((e as Error).message || '会话删除失败')
  } finally {
    deletingConversationId.value = undefined
  }
}

async function streamChat(aiMessage: string, assistant: ChatMessage) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  chatAbortController?.abort()
  chatAbortController = new AbortController()
  const response = await fetch(`${baseUrl}/api/ai/chat/stream`, {
    method: 'POST',
    signal: chatAbortController.signal,
    headers: {
      'Content-Type': 'application/json',
      ...(localStorage.getItem('token') ? { Authorization: `Bearer ${localStorage.getItem('token')}` } : {}),
    },
    body: JSON.stringify({ conversationId: conversationId.value, message: aiMessage }),
  })
  if (!response.ok || !response.body) throw new Error(await response.text() || 'AI 回复失败')
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop() || ''
    for (const block of blocks) handleStreamBlock(block, assistant)
    saveChatCache()
    await scrollBottom()
  }
  if (buffer.trim()) handleStreamBlock(buffer, assistant)
  saveChatCache()
  if (!assistant.content.trim()) throw new Error('AI 暂未返回内容')
}

function handleStreamBlock(block: string, assistant: ChatMessage) {
  let event = 'message'
  const data: string[] = []
  for (const line of block.split(/\n/)) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    if (line.startsWith('data:')) data.push(line.slice(5).trimStart())
  }
  const text = data.join('\n')
  if (!text) return
  if (event === 'meta') {
    conversationId.value = Number(text)
    return
  }
  if (event === 'error') throw new Error(text)
  if (event === 'delta' || event === 'message') assistant.content += text
}

async function openConversation(id: number) {
  if (loading.value || conversationId.value === id) return
  try {
    const rows = await http.get<AiMessage[]>(`/api/ai/conversations/${id}/messages`)
    conversationId.value = id
    messages.value = rows
      .filter(item => item.role === 'user' || item.role === 'assistant')
      .map(item => ({ id: String(item.id), role: item.role as 'user' | 'assistant', content: item.content }))
    if (!messages.value.length) messages.value = defaultMessages()
    const latestAssistant = [...messages.value].reverse().find(item => item.role === 'assistant')?.content
    setBudgetPlans(latestAssistant ? parseBudgetPlans(latestAssistant) : [fallbackBudgetPlan(3000, '默认方案')])
    await scrollBottom()
  } catch (e) {
    toast.error((e as Error).message || '会话加载失败')
  }
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

function parseBudgetPlans(text: string, fallbackTotal = extractRequestedBudget(text) || 3000): BudgetPlan[] {
  const sections = splitPlanSections(text)
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
  const value = text.match(/(?:在|从|去|到|目的地[：:]?)\s*([\u4e00-\u9fa5]{2,8})(?:出发|玩|旅游|旅行|打卡|美食|海边|古镇|雪景|$)/)?.[1] || ''
  return normalizeCity(value)
}

function extractSocialPlace(text: string) {
  const direct = text.match(/(?:推荐城市|目的地|推荐目的地|城市)[：:\s]*([\u4e00-\u9fa5]{2,12})/)?.[1]
  if (direct) return normalizeCity(direct)
  return normalizeCity(text.match(/去([\u4e00-\u9fa5]{2,12})(?:玩|旅游|旅行|打卡|吃|看|$)/)?.[1] || '')
}

function destinationCity(text: string) {
  const patterns = [
    /从([^，。,. \n]{2,8})(?:出发)?(?:去|到|前往)([^，。,. \n]{2,8})/,
    /([^，。,. \n]{2,8})(?:出发)?(?:去|到|前往)([^，。,. \n]{2,8})/,
    /(?:目的地|推荐城市|推荐目的地)[：:\s]*([^，。,. \n]{2,8})/,
  ]
  for (const pattern of patterns) {
    const match = text.match(pattern)
    const value = match?.[match.length - 1]
    const city = normalizeCity(value || '')
    if (city) return city
  }
  return ''
}

function normalizeCity(value: string) {
  if (!value) return ''
  return knownCities.find(city => value.includes(city)) || ''
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

watch(weatherCity, city => {
  weatherQuery.value = city
  loadWeather(city)
})

onMounted(async () => {
  if (user.token && !user.user) await user.fetchMe()
  await loadConversations()
  await restoreChatCache()
  await scrollBottom()
  await loadWeather(weatherCity.value)
  seasonal.value = await loadOnlineSeasonalAttractions()
  await nextTick()
  renderBudgetChart()
  window.addEventListener('resize', resizeBudgetChart)
  if (chartRef.value) {
    resizeObserver = new ResizeObserver(resizeBudgetChart)
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => chatOwnerKey(), async () => {
  await loadConversations()
  await restoreChatCache()
})

function defaultMessages(content = '你好，我是 TravelMind AI。') {
  return [{ id: crypto.randomUUID(), role: 'assistant' as const, content }]
}

function chatOwnerKey() {
  if (!user.token) return 'guest'
  return user.user?.id ? `user-${user.user.id}` : `token-${jwtSubject(user.token)}`
}

function chatStorageKey() {
  return `travelmind-chat-${chatOwnerKey()}`
}

function chatFallbackStorageKeys() {
  const keys = [chatStorageKey()]
  if (user.token) keys.push(`travelmind-chat-token-${jwtSubject(user.token)}`)
  return [...new Set(keys)]
}

function saveChatCache() {
  if (restoringChat) return
  const value = JSON.stringify({ conversationId: conversationId.value, messages: messages.value })
  chatFallbackStorageKeys().forEach(key => localStorage.setItem(key, value))
}

async function restoreChatCache() {
  restoringChat = true
  for (const key of chatFallbackStorageKeys()) {
    const cache = localStorage.getItem(key)
    if (!cache) continue
    try {
      const data = JSON.parse(cache)
      if (Array.isArray(data.messages) && data.messages.length) {
        messages.value = data.messages
        conversationId.value = data.conversationId
        const latestAssistant = [...messages.value].reverse().find(item => item.role === 'assistant')?.content
        if (latestAssistant) setBudgetPlans(parseBudgetPlans(latestAssistant))
        restoringChat = false
        saveChatCache()
        return
      }
    } catch {}
  }
  if (user.token && conversations.value.length) {
    restoringChat = false
    await openConversation(conversations.value[0].id)
    saveChatCache()
    return
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

async function loadWeather(city: string) {
  const target = city?.trim() || weatherCity.value
  weatherQuery.value = target
  weather.value = undefined
  weatherError.value = ''
  if (!target) return
  weatherLoading.value = true
  try {
    weather.value = await http.get<WeatherInfo>('/api/weather', { params: { city: target } })
  } catch (e) {
    weatherError.value = (e as Error).message || '天气更新失败'
  } finally {
    weatherLoading.value = false
  }
}

async function loadOnlineSeasonalAttractions() {
  try {
    const results = await http.get<Attraction[]>('/api/attractions/online', {
      params: { keyword: seasonalAttractionKeyword(), limit: 6 },
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
  const result = await new Promise<AmapPoi[]>((resolve, reject) => {
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

</script>
