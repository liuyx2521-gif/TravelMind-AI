import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from './stores/user'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('./views/Landing.vue') },
    { path: '/login', component: () => import('./views/Login.vue') },
    { path: '/register', component: () => import('./views/Register.vue') },
    {
      path: '/app',
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
        { path: 'plans', component: () => import('./views/Plans.vue') },
        { path: 'plans/:id', component: () => import('./views/Plans.vue') },
        { path: 'profile', component: () => import('./views/Profile.vue') },
      ],
    },
    { path: '/attractions', redirect: '/app/attractions' },
    { path: '/attractions/:id', redirect: to => `/app/attractions/${to.params.id}` },
    { path: '/hotels', redirect: '/app/hotels' },
    { path: '/hotels/:id', redirect: to => `/app/hotels/${to.params.id}` },
    { path: '/notes', redirect: '/app/notes' },
    { path: '/notes/:id', redirect: to => `/app/notes/${to.params.id}` },
    { path: '/tools', redirect: '/app/tools' },
    { path: '/plans', redirect: '/app/plans' },
    { path: '/plans/:id', redirect: to => `/app/plans/${to.params.id}` },
    { path: '/profile', redirect: '/app/profile' },
  ],
})

router.beforeEach(to => {
  const user = useUserStore()
  return to.meta.auth && !user.token ? `/login?redirect=${to.fullPath}` : true
})
