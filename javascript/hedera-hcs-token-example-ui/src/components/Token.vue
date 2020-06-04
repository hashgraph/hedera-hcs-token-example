<template>
  <div>
    <v-toolbar dense>
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>Payment Application</v-toolbar-title>
    </v-toolbar>
    <v-sheet
      height="100%"
    >

      <v-navigation-drawer
        v-model="drawer"
        absolute
        temporary
      >
        <v-list-item>
          <v-list-item-avatar>
            <v-img src="https://randomuser.me/api/portraits/men/78.jpg"></v-img>
          </v-list-item-avatar>

          <v-list-item-content>
            <v-list-item-title>{{ userName }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-divider></v-divider>

        <v-list dense>

          <v-list-item @click.stop="explore()">
            <v-list-item-icon>
              <v-icon>mdi-web</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title> {{ this.topicId }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-divider></v-divider>

          <v-list-item @click.stop="resetCookies()">
            <v-list-item-icon>
              <v-icon>{{ cookiesIcon }}</v-icon>
            </v-list-item-icon>
            <v-list-item-content>
              <v-list-item-title>Reset</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

        </v-list>
      </v-navigation-drawer>
      <construct v-if="state == 'construct'"></construct>
      <register v-if="state == 'register'"></register>
      <accounts v-if="state == 'accounts'"></accounts>
    </v-sheet>

  </div>
</template>

<script>
  import axios from 'axios'
  import Construct from './Construct'
  import Accounts from './Accounts'
  import Mint from './Mint'
  import Register from './Register'
  import Cookie from 'js-cookie'
  import Utils from '../utils'
  import router from '../router'

  export default {
    name: 'Token',
    components: {Mint, Accounts, Construct, Register},
    data () {
      return {
        msg: 'Welcome to Hedera Token',
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT,
        userName: Cookie.get('userName'),
        userKey: '',
        state: '',
        clipped: false,
        drawer: false,
        fixed: false,
        ownerRoleIcon: 'mdi-account-cog',
        userRoleIcon: 'mdi-account',
        cookiesIcon: 'mdi-delete',
        miniVariant: false,
        right: true,
        rightDrawer: false,
        networkStatus: null,
        topicUrl: '',
        topicId: ''
      }
    },
    methods: {
      resetCookies: function () {
        Utils.resetCookies()
        this.drawer = false
        router.go(0)
      },
      explore: function () {
        window.open(this.topicUrl, '_blank')
      }
    },
    mounted () {
      this.userName = Cookie.get('userName')
      if ((typeof (this.userName) !== 'undefined') && (this.userName !== '')) {
        axios.get(this.restAPI.concat('/v1/token/userExists/' + this.userName))
          .then(response => {
            if (response.data.exists === false) {
              // user doesn't exist
              Utils.resetCookies()
            }
          })
          .catch(e => {
            console.log(e)
          })
      }

      axios.get(this.restAPI.concat('/v1/token'))
        .then(response => {
          Cookie.set('tokenName', response.data.name, { expires: 365 })
          this.userName = Cookie.get('userName')
          this.userKey = Cookie.get('userKey')
          this.topicId = 'Explore ' + response.data.topicId
          this.topicUrl = 'https://explorer.kabuto.sh/testnet/id/' + response.data.topicId
          if (response.data.name === '') {
            // token doesn't exist
            this.msg = 'No token'
            this.state = 'construct'
          } else {
            if ((typeof (this.userName) === 'undefined') || (this.userName === '')) {
              // need to create and register a user
              this.state = 'register'
            } else {
              // user exists, start buying
              this.state = 'accounts'
            }
          }
        })
        .catch(e => {
          console.log(e)
        })
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
  h1, h2 {
    font-weight: normal;
  }

  ul {
    list-style-type: none;
    padding: 0;
  }

  li {
    display: inline-block;
    margin: 0 10px;
  }

  a {
    color: #35495E;
  }
</style>
