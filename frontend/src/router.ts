import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from './stores/user'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('./views/Login.vue') },
    { path: '/register', component: () => import('./views/Register.vue') },
    {
      path: '/',
      component: () => import('./views/Layout.vue'),
      children: [
        { path: '', component: () => import('./views/Home.vue') },
        { path: 'attractions', component: () => import('./views/Attractions.vue') },
        { path: 'attractions/:id', component: () => import('./views/AttractionDetail.vue') },
        { path: 'hotels', component: () => import('./views/Hotels.vue') },
        { path: 'hotels/:id', component: () => import('./views/HotelDetail.vue') },
        { path: 'notes', component: () => import('./views/Notes.vue') },
        { path: 'notes/:id', component: () => import('./views/Notes.vue') },
        { path: 'tools', component: () => import('./views/AiTools.vue') },
        { path: 'plans', component: () => import('./views/Plans.vue'), meta: { auth: true } },
        { path: 'plans/:id', component: () => import('./views/Plans.vue'), meta: { auth: true } },
        { path: 'profile', component: () => import('./views/Profile.vue'), meta: { auth: true } },
      ],
    },
  ],
})

router.beforeEach(to => {
  const user = useUserStore()
  return to.meta.auth && !user.token ? `/login?redirect=${to.fullPath}` : true
})
