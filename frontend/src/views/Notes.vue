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
      <div class="max-h-[520px] space-y-3 overflow-y-auto pr-1">
        <article
          v-for="item in rootComments"
          :key="item.id"
          class="rounded-2xl bg-white/45 p-3 dark:bg-white/8"
          @contextmenu.prevent="requestDeleteComment(item)"
        >
          <div class="flex items-start gap-3">
            <n-avatar round :size="38" :src="commentAvatar(item)">
              {{ avatarText(item) }}
            </n-avatar>
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-2">
                <span class="font-700">{{ commentName(item) }}</span>
                <span class="text-xs text-[var(--muted)]">{{ formatTime(item.createTime) }}</span>
                <div class="ml-auto flex gap-1">
                  <n-button size="tiny" quaternary @click="replyTo(item)">回复</n-button>
                </div>
              </div>
              <p class="m-0 mt-1 whitespace-pre-wrap break-words leading-6">{{ item.content }}</p>
            </div>
          </div>
          <div v-if="threadReplies(item).length" class="mt-3 space-y-2 border-l border-white/35 pl-4 dark:border-white/10">
            <article
              v-for="reply in threadReplies(item)"
              :key="reply.id"
              class="rounded-2xl bg-white/40 p-3 dark:bg-white/6"
              :style="{ marginLeft: `${Math.min(reply.level, 2) * 14}px` }"
              @contextmenu.prevent="requestDeleteComment(reply)"
            >
              <div class="flex items-start gap-3">
                <n-avatar round :size="30" :src="commentAvatar(reply)">
                  {{ avatarText(reply) }}
                </n-avatar>
                <div class="min-w-0 flex-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="font-700">{{ commentName(reply) }}</span>
                    <span class="text-xs text-[var(--muted)]">{{ formatTime(reply.createTime) }}</span>
                    <div class="ml-auto flex gap-1">
                      <n-button size="tiny" quaternary @click="replyTo(reply)">回复</n-button>
                    </div>
                  </div>
                  <div v-if="reply.replyToUserId" class="mt-1 text-xs text-[var(--muted)]">回复 @{{ replyName(reply) }}</div>
                  <p class="m-0 mt-1 whitespace-pre-wrap break-words leading-6">{{ reply.content }}</p>
                </div>
              </div>
            </article>
          </div>
        </article>
        <div v-if="!comments.length" class="rounded-2xl bg-white/35 p-4 text-center text-[var(--muted)] dark:bg-white/6">还没有评论</div>
      </div>
      <div v-if="replyTarget" class="mt-4 flex items-center justify-between gap-3 rounded-2xl bg-white/45 px-3 py-2 text-sm dark:bg-white/8">
        <span class="text-[var(--muted)]">回复 @{{ commentName(replyTarget) }}</span>
        <n-button size="tiny" quaternary @click="clearReply">取消</n-button>
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
type ThreadComment = Comment & { level: number }

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
const deletingCommentId = ref<number>()
const avatarVersion = ref(Date.now())
const replyTarget = ref<Comment>()
const form = reactive({ title: '', content: '', images: [] as string[] })
const detailMode = computed(() => !!route.params.id)
const keyword = ref('')
const rootComments = computed(() => comments.value.filter(item => !item.parentId).sort(sortNewest))
const repliesByParent = computed(() => comments.value.reduce<Record<number, Comment[]>>((map, item) => {
  if (!item.parentId) return map
  map[item.parentId] = [...(map[item.parentId] || []), item].sort(sortOldest)
  return map
}, {}))

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

function avatarText(item: Comment) {
  return commentName(item).slice(0, 1).toUpperCase()
}

function commentName(item: Comment) {
  const name = (item.username || '').trim()
  return name && !/^用户\s*#?\s*\d+$/i.test(name) ? name : `ID ${item.userId || '-'}`
}

function replyName(item: Comment) {
  const name = (item.replyToUsername || '').trim()
  return name && !/^用户\s*#?\s*\d+$/i.test(name) ? name : `ID ${item.replyToUserId || '-'}`
}

function commentAvatar(item: Comment) {
  const url = Number(item.userId) === Number(user.user?.id) ? user.user?.avatar || item.avatar : item.avatar
  if (!url) return undefined
  const version = Number(item.userId) === Number(user.user?.id) ? avatarVersion.value : 1
  return `${url}${url.includes('?') ? '&' : '?'}t=${version}`
}

function formatTime(value?: string) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value.replace('T', ' ').slice(0, 16)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function sortNewest(a: Comment, b: Comment) {
  return timeValue(b.createTime) - timeValue(a.createTime)
}

function sortOldest(a: Comment, b: Comment) {
  return timeValue(a.createTime) - timeValue(b.createTime)
}

function timeValue(value?: string) {
  return value ? new Date(value).getTime() || 0 : 0
}

function threadReplies(root: Comment) {
  const result: ThreadComment[] = []
  const walk = (parentId: number, level: number) => {
    for (const item of repliesByParent.value[parentId] || []) {
      result.push({ ...item, level })
      walk(item.id, level + 1)
    }
  }
  walk(root.id, 1)
  return result
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

function isMyComment(item: Comment) {
  return !!user.user?.id && Number(item.userId) === Number(user.user.id)
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

function replyTo(item: Comment) {
  if (!user.token) {
    toast.warning('登录后才可以回复')
    return
  }
  replyTarget.value = item
}

function clearReply() {
  replyTarget.value = undefined
}

async function requestDeleteComment(item: Comment) {
  if (!isMyComment(item)) return
  if (!window.confirm('删除这条评论？')) return
  await deleteComment(item)
}

async function deleteComment(item: Comment) {
  if (!current.value) return
  deletingCommentId.value = item.id
  try {
    await http.delete(`/api/notes/${current.value.id}/comments/${item.id}`)
    const ids = new Set([item.id, ...threadReplies(item).map(reply => reply.id)])
    comments.value = comments.value.filter(comment => !ids.has(comment.id))
  } catch (e) {
    toast.error((e as Error).message || '评论删除失败')
  } finally {
    deletingCommentId.value = undefined
  }
}

async function sendComment() {
  if (!user.token) {
    toast.warning('登录后才可以评论')
    return
  }
  if (!user.user) await user.fetchMe()
  if (!current.value || !comment.value.trim()) {
    toast.warning('评论不能为空')
    return
  }
  commenting.value = true
  try {
    const saved = await http.post<Comment>(`/api/notes/${current.value.id}/comments`, {
      content: comment.value.trim(),
      parentId: replyTarget.value?.id,
    })
    comment.value = ''
    replyTarget.value = undefined
    comments.value = [saved, ...comments.value]
  } catch (e) {
    toast.error((e as Error).message || '请先登录后评论')
  } finally {
    commenting.value = false
  }
}

watch(() => route.params.id, loadDetail)
watch(() => user.user?.avatar, () => {
  avatarVersion.value = Date.now()
})
onMounted(async () => {
  if (user.token) {
    await user.fetchMe()
    avatarVersion.value = Date.now()
  }
  await loadDetail()
})
</script>
