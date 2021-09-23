// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import vuetify from '@/plugins/vuetify' // path to vuetify export
import VueSimpleWebSocket from 'vue-simple-websocket'
Vue.use(VueSimpleWebSocket, 'ws://' + window.location.hostname + ':' + process.env.HOST_PORT + '/ws', {
  reconnectEnabled: true,
  reconnectInterval: 5000
})

Vue.config.productionTip = false

export const bus = new Vue()

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  vuetify,
  template: '<App/>',
  components: { App }
})
