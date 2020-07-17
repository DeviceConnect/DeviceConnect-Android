import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'ServiceListPage',
    meta: {title: 'MIDI サービス一覧'},
    component: () => import('../views/ServiceListPage.vue')
  },
  {
    path: '/profile-select/:id',
    name: 'ProfileSelectPage',
    meta: {title: 'プロファイル一覧'},
    component: () => import('../views/ProfileSelectPage.vue')
  },
  {
    path: '/settings/:id',
    name: 'SettingsPage',
    meta: {title: 'UI 設定'},
    component: () => import('../views/SettingsPage.vue')
  },
  {
    path: '/controller/:id',
    name: 'ControllerPage',
    meta: {title: 'コントローラ'},
    component: () => import('../views/ControllerPage.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
