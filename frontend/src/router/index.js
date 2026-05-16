import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import ContractList from '../views/ContractList.vue'
import ContractDetail from '../views/ContractDetail.vue'
import Performance from '../views/Performance.vue'
import Risk from '../views/Risk.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/contracts',
    name: 'ContractList',
    component: ContractList
  },
  {
    path: '/contract/:id',
    name: 'ContractDetail',
    component: ContractDetail
  },
  {
    path: '/performance',
    name: 'Performance',
    component: Performance
  },
  {
    path: '/risk',
    name: 'Risk',
    component: Risk
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
