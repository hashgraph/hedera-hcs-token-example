<template>
  <v-app id="app">
    <router-view></router-view>
    <v-snackbar
      v-model="snackbar"
      :bottom=true
      :color="snackAlert"
      :multi-line=false
      :timeout=5000
      :vertical=true
    >
      {{ snackMessage }}
      <v-btn
        dark
        text
        @click="snackbar = false"
      >
        Close
      </v-btn>
    </v-snackbar>
  </v-app>
</template>

<script>
// import router from './router'
// import Cookie from 'js-cookie'
// import utils from './utils'
import { bus } from './main'

export default {
  name: 'app',
  data () {
    return {
      snackbar: false,
      snackMessage: '',
      snackAlert: 'alert'
    }
  },
  methods: {
  },
  mounted () {
    bus.$on('showSuccess', (message) => {
      this.snackbar = true
      this.snackAlert = 'success'
      this.snackMessage = message
    })
    bus.$on('showError', (message) => {
      this.snackbar = true
      this.snackAlert = 'error'
      this.snackMessage = message
    })
    bus.$on('hideSnackbar', () => {
      this.snackbar = false
      this.snackMessage = ''
    })
  }
}
</script>

<style>
body {
  margin: 0;
}

#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

main {
  text-align: center;
  margin-top: 40px;
}

header {
  margin: 0;
  height: 56px;
  padding: 0 16px 0 24px;
  background-color: #35495E;
  color: #ffffff;
}

header span {
  display: block;
  position: relative;
  font-size: 20px;
  line-height: 1;
  letter-spacing: .02em;
  font-weight: 400;
  box-sizing: border-box;
  padding-top: 16px;
}
</style>
