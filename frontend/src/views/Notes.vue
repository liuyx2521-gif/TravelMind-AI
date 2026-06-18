<template>
  <div v-if="detailMode && current" class="space-y-5">
    <section class="liquid overflow-hidden rounded-[28px]">
      <img :src="coverOf(current)" class="h-[360px] w-full object-cover" @error="fallbackNoteImage($event, current)" />
      <div class="p-5 md:p-7">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
          <n-button quaternary @click="router.push('/app/notes')">返回游记</n-button>
          <n-popconfirm v-if="isMine(current)" @positive-click="deleteNote(current)">
            <template #trigger>
              <n-button round type="error" ghost :loading="deletingId === current.id">删除</n-button>
            </template>
            确定删除这篇游记吗？
          </n-popconfirm>
        </div>
        <h1 class="m-0 text-3xl">{{ current.title }}</h1>
        <div class="mt-2 flex flex-wrap gap-3 text-sm text-[var(--muted)]">
          <span>{{ current.viewCount || 0 }} 浏览</span>
          <span>{{ current.likeCount || 0 }} 赞</span>
        </div>
        <p class="whitespace-pre-wrap leading-8 text-[var(--text)]">{{ current.content }}</p>
        <div v-if="imagesOf(current).length" class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          <img
            v-for="url in imagesOf(current)"
            :key="url"
            :src="url"
            class="h-56 w-full rounded-[22px] object-cover"
            @error="fallbackGenericImage($event, current.title)"
          />
        </div>
      </div>
    </section>

    <section class="liquid rounded-[28px] p-5">
      <div class="mb-4 flex items-center justify-between gap-3">
        <h2 class="m-0 text-2xl">评论</h2>
        <n-button round @click="like(current)">点赞 {{ current.likeCount }}</n-button>
      </div>
      <div class="space-y-3">
        <p v-for="item in comments" :key="item.id" class="m-0 rounded-2xl bg-white/45 p-3 dark:bg-white/8">
          {{ item.content }}
        </p>
        <div v-if="!comments.length" class="rounded-2xl bg-white/35 p-4 text-center text-[var(--muted)] dark:bg-white/6">还没有评论</div>
      </div>
      <div class="mt-4 flex items-end gap-3">
        <n-input v-model:value="comment" type="textarea" :autosize="{ minRows: 1, maxRows: 4 }" placeholder="写评论" />
        <n-button type="primary" round :loading="commenting" @click="sendComment">发送</n-button>
      </div>
    </section>
  </div>

  <div v-else class="space-y-5">
    <section class="liquid rounded-[28px] p-5 md:p-6">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 class="m-0 text-3xl">旅行社区</h1>
          <p class="m-0 mt-2 text-[var(--muted)]">看看大家真实分享的路线、照片和避坑经验，也可以发布自己的旅行灵感。</p>
        </div>
        <div class="flex w-full gap-2 md:w-[420px]">
          <n-input v-model:value="keyword" round clearable placeholder="搜索目的地、玩法、美食..." @keyup.enter="loadList" />
          <n-button type="primary" round @click="loadList">搜索</n-button>
        </div>
      </div>
    </section>

    <section class="liquid rounded-[28px] p-5">
      <h2 class="m-0 mb-4">发布游记</h2>
      <div class="grid gap-4 lg:grid-cols-[1fr_1.15fr]">
        <div>
          <n-input v-model:value="form.title" class="mb-3" placeholder="标题，例如：三亚三天两晚，海边和夜市都安排上了" />
          <n-input v-model:value="form.content" type="textarea" :autosize="{ minRows: 5 }" placeholder="写下你的旅行体验、路线、预算、避坑或者推荐理由" />
          <n-button class="mt-4" type="primary" round :loading="publishing" @click="publish">发布到社区</n-button>
        </div>
        <div>
          <n-upload multiple list-type="image-card" :custom-request="uploadPhoto" />
          <div v-if="form.images.length" class="mt-2 text-sm text-[var(--muted)]">已上传 {{ form.images.length }} 张照片</div>
        </div>
      </div>
    </section>

    <section class="columns-1 gap-4 md:columns-2">
      <article
        v-for="note in notes"
        :key="note.id"
        class="liquid mb-4 inline-block w-full cursor-pointer overflow-hidden rounded-[28px] transition hover:-translate-y-1"
        @click="router.push(`/app/notes/${note.id}`)"
      >
        <img :src="coverOf(note)" class="max-h-[420px] min-h-[220px] w-full object-cover" @error="fallbackNoteImage($event, note)" />
        <div class="p-5">
          <h2 class="m-0 text-xl">{{ note.title }}</h2>
          <p class="line-clamp-3 whitespace-pre-wrap text-[var(--muted)]">{{ note.content }}</p>
          <div class="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm text-[var(--muted)]" @click.stop>
            <span>用户 #{{ note.userId || '-' }}</span>
            <span>{{ note.viewCount || 0 }} 浏览 · {{ note.likeCount || 0 }} 赞</span>
          </div>
          <div class="mt-3 flex flex-wrap gap-2" @click.stop>
            <n-button size="small" round @click="like(note)">点赞</n-button>
            <n-button size="small" round @click="router.push(`/app/notes/${note.id}`)">评论</n-button>
            <n-popconfirm v-if="isMine(note)" @positive-click="deleteNote(note)">
              <template #trigger>
                <n-button size="small" round type="error" ghost :loading="deletingId === note.id">删除</n-button>
              </template>
              确定删除这篇游记吗？
            </n-popconfirm>
          </div>
        </div>
      </article>
      <n-empty v-if="!notes.length" description="没有找到相关游记" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { http, type Comment, type Note, type PageResp } from '../api'
import { fallbackGenericImage, placeImagePlaceholder } from '../imageFallback'
import { useUserStore } from '../stores/user'

type UploadResp = { url: string }
type NoteWithImages = Note & { images?: string }

const toast = useMessage()
const route = useRoute()
const router = useRouter()
const user = useUserStore()
const notes = ref<NoteWithImages[]>([])
const comments = ref<Comment[]>([])
const current = ref<NoteWithImages>()
const comment = ref('')
const commenting = ref(false)
const publishing = ref(false)
const deletingId = ref<number>()
const form = reactive({ title: '', content: '', images: [] as string[] })
const detailMode = computed(() => !!route.params.id)
const keyword = ref('')

function notePlaceholder(title: string) {
  return placeImagePlaceholder({ title })
}

function fallbackNoteImage(e: Event, note: NoteWithImages) {
  fallbackGenericImage(e, note.title)
}

function imagesOf(note: NoteWithImages) {
  if (!note.images) return note.cover ? [note.cover] : []
  try {
    const images = JSON.parse(note.images)
    return Array.isArray(images) ? images.filter(x => typeof x === 'string' && x) : []
  } catch {
    return note.images.split(',').map(x => x.trim()).filter(Boolean)
  }
}

function coverOf(note: NoteWithImages) {
  return note.cover || imagesOf(note)[0] || notePlaceholder(note.title)
}

async function loadList() {
  const page = await http.get<PageResp<NoteWithImages>>('/api/notes', {
    params: { keyword: keyword.value.trim() || undefined, size: 30 },
  })
  notes.value = page.records
}

async function loadDetail() {
  const id = route.params.id
  if (!id) {
    current.value = undefined
    comments.value = []
    await loadList()
    return
  }
  current.value = await http.get<NoteWithImages>(`/api/notes/${id}`)
  comments.value = await http.get<Comment[]>(`/api/notes/${id}/comments`)
}

async function uploadPhoto({ file, onFinish, onError }: any) {
  if (!user.token) {
    onError()
    toast.warning('登录后才可以上传照片')
    return
  }
  const body = new FormData()
  body.append('file', file.file)
  try {
    const data = await http.post<UploadResp>('/api/files/upload?bucket=notes', body)
    form.images.push(data.url)
    onFinish()
  } catch (e) {
    onError()
    toast.error((e as Error).message)
  }
}

async function publish() {
  if (!user.token) {
    toast.warning('登录后才可以发布游记')
    return
  }
  if (!form.title.trim() || !form.content.trim()) {
    toast.warning('标题和内容不能为空')
    return
  }
  publishing.value = true
  try {
    await http.post('/api/notes', {
      title: form.title.trim(),
      content: form.content.trim(),
      cover: form.images[0] || '',
      images: JSON.stringify(form.images),
    })
    form.title = ''
    form.content = ''
    form.images = []
    toast.success('已发布')
    await loadList()
  } catch (e) {
    toast.error((e as Error).message || '请先登录后发布')
  } finally {
    publishing.value = false
  }
}

async function like(note: NoteWithImages) {
  if (!user.token) {
    toast.warning('登录后才可以点赞')
    return
  }
  await http.post(`/api/notes/${note.id}/like`)
  note.likeCount = (note.likeCount || 0) + 1
}

function isMine(note: NoteWithImages) {
  return !!user.user?.id && Number(note.userId) === Number(user.user.id)
}

async function deleteNote(note: NoteWithImages) {
  if (!user.token) {
    toast.warning('登录后才可以删除游记')
    return
  }
  deletingId.value = note.id
  try {
    await http.post(`/api/notes/${note.id}/delete`)
    notes.value = notes.value.filter(item => item.id !== note.id)
    toast.success('已删除')
    if (current.value?.id === note.id) {
      current.value = undefined
      await router.push('/app/notes')
      await loadList()
    }
  } catch (e) {
    toast.error((e as Error).message || '删除失败')
  } finally {
    deletingId.value = undefined
  }
}

async function sendComment() {
  if (!user.token) {
    toast.warning('登录后才可以评论')
    return
  }
  if (!current.value || !comment.value.trim()) {
    toast.warning('评论不能为空')
    return
  }
  commenting.value = true
  try {
    await http.post(`/api/notes/${current.value.id}/comments`, { content: comment.value.trim() })
    comment.value = ''
    comments.value = await http.get<Comment[]>(`/api/notes/${current.value.id}/comments`)
  } catch (e) {
    toast.error((e as Error).message || '请先登录后评论')
  } finally {
    commenting.value = false
  }
}

watch(() => route.params.id, loadDetail)
onMounted(async () => {
  if (user.token && !user.user) await user.fetchMe()
  await loadDetail()
})
</script>
