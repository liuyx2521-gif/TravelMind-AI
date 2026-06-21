import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from './stores/user'

const appChildren: RouteRecordRaw[] = [
  { path: '', name: 'home', component: () => import('./views/Home.vue'), meta: { title: 'AI规划' } },
  { path: 'attractions', name: 'attractions', component: () => import('./views/Attractions.vue'), meta: { title: '景点' } },
  { path: 'attractions/:id', name: 'attraction-detail', component: () => import('./views/AttractionDetail.vue'), meta: { title: '景点详情' } },
  { path: 'hotels', name: 'hotels', component: () => import('./views/Hotels.vue'), meta: { title: '酒店' } },
  { path: 'hotels/:id', name: 'hotel-detail', component: () => import('./views/HotelDetail.vue'), meta: { title: '酒店详情' } },
  { path: 'tickets', name: 'tickets', component: () => import('./views/Tickets.vue'), meta: { title: '车票' } },
  { path: 'notes', name: 'notes', component: () => import('./views/Notes.vue'), meta: { title: '游记' } },
  { path: 'notes/:id', name: 'note-detail', component: () => import('./views/Notes.vue'), meta: { title: '游记详情' } },
  { path: 'tools', name: 'tools', component: () => import('./views/AiTools.vue'), meta: { title: 'AI工具' } },
  { path: 'plans', name: 'plans', component: () => import('./views/Plans.vue'), meta: { title: '行程' } },
  { path: 'plans/:id', name: 'plan-detail', component: () => import('./views/Plans.vue'), meta: { title: '行程详情' } },
  { path: 'profile', name: 'profile', component: () => import('./views/Profile.vue'), meta: { title: '我的' } },
]

const legacyRedirects: RouteRecordRaw[] = [
  { path: '/attractions', redirect: '/app/attractions' },
  { path: '/attractions/:id', redirect: to => `/app/attractions/${to.params.id}` },
  { path: '/hotels', redirect: '/app/hotels' },
  { path: '/hotels/:id', redirect: to => `/app/hotels/${to.params.id}` },
  { path: '/tickets', redirect: '/app/tickets' },
  { path: '/notes', redirect: '/app/notes' },
  { path: '/notes/:id', redirect: to => `/app/notes/${to.params.id}` },
  { path: '/tools', redirect: '/app/tools' },
  { path: '/plans', redirect: '/app/plans' },
  { path: '/plans/:id', redirect: to => `/app/plans/${to.params.id}` },
  { path: '/profile', redirect: '/app/profile' },
]

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'landing', component: () => import('./views/Landing.vue'), meta: { title: 'TravelMind AI' } },
    { path: '/login', name: 'login', component: () => import('./views/Login.vue'), meta: { title: '登录' } },
    { path: '/register', name: 'register', component: () => import('./views/Register.vue'), meta: { title: '注册' } },
    { path: '/app', component: () => import('./views/Layout.vue'), children: appChildren },
    ...legacyRedirects,
  ],
})

router.beforeEach(to => {
  const user = useUserStore()
  document.title = `${String(to.meta.title || 'TravelMind AI')} | TravelMind AI`
  return to.meta.auth && !user.token ? `/login?redirect=${to.fullPath}` : true
})
