import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'ServiceListPage',
    component: () => import('../views/ServiceListPage.vue')
  },
  {
    path: '/profile-select/:id',
    name: 'ProfileSelectPage',
    component: () => import('../views/ProfileSelectPage.vue')
  },
  {
    path: '/settings/:id',
    name: 'SettingsPage',
    component: () => import('../views/SettingsPage.vue')
  },
  {
    path: '/controller/:id',
    name: 'ControllerPage',
    component: () => import('../views/ControllerPage.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
