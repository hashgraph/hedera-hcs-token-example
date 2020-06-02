import Vue from 'vue'
import Router from 'vue-router'
import Token from '../components/Token'
import Operate from '../components/Operate'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Token',
      component: Token
    },
    {
      path: '/operate',
      name: 'Operate',
      component: Operate
    }
  ]
})
