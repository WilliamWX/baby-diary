import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/',
    component: () => import('../components/layout/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'diary/create', name: 'DiaryCreate', component: () => import('../views/DiaryCreate.vue') },
      { path: 'diary/:id', name: 'DiaryDetail', component: () => import('../views/DiaryDetail.vue') },
      { path: 'baby/:id', name: 'BabyHome', component: () => import('../views/BabyHome.vue') },
      { path: 'growth/:babyId', name: 'Growth', component: () => import('../views/Growth.vue') },
      { path: 'posts', name: 'Posts', component: () => import('../views/Posts.vue') },
      { path: 'post/create', name: 'PostCreate', component: () => import('../views/PostCreate.vue') },
      { path: 'post/:id', name: 'PostDetail', component: () => import('../views/PostDetail.vue') },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/UserProfile.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') },
      { path: 'search', name: 'Search', component: () => import('../views/Search.vue') },
      { path: 'ai-doctor', name: 'AiDoctor', component: () => import('../views/AiDoctor.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.meta.guest && token) {
    return next('/')
  }
  if (!to.meta.guest && !token) {
    return next('/login')
  }
  next()
})

export default router
