<template>
  <div>
    <ProgressDialog :show="inProgress"></ProgressDialog>
    <v-row
      justify="center"
      align="start"
    >
      <v-col cols="12">

        <v-card
          class="mx-auto"
          max-width="344"
          raised
          title="Accounts"
        >
          <v-list-item>
            <v-list-item-content>
              <v-row no-gutters>
                <v-col md="auto" class="text-center">
                  {{ accountsTitle }}
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>

          <v-list-item>
            <v-list-item-content>
              <v-row align="center">
                <v-col cols="4">
                  <v-card
                    class="pa-4 text-center justify-center"
                    outlined
                  >Cash</v-card>
                </v-col>
                <v-col md="auto" class="text-left">
                  <span align="center" class="font-weight-bold">$1,399.00</span>
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>
          <v-divider></v-divider>
          <v-list-item @click.stop="operate()">
            <v-list-item-content>
              <v-row align="center">
                <v-col cols="4">
                  <v-card
                    class="pa-4 text-center justify-center"
                    outlined
                  >STABL</v-card>
                </v-col>
                <v-col md="auto" class="text-left">
                  <span align="center" class="font-weight-bold">${{ balance }}</span>
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>
          <v-divider></v-divider>
          <v-list-item>
            <v-list-item-content>
              <v-row align="center">
                <v-col cols="4">
                  <v-card
                    outlined
                  >
                    <v-card-actions class="justify-center">
                      <v-img src="static/mock-credit-card.jpg" max-height="51" max-width="84" contain></v-img>
                    </v-card-actions>
                  </v-card>
                </v-col>
                <v-col md="auto" class="text-left">
                  <span align="center" class="font-weight-bold">VISA ***** 1111</span>
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script>
  import router from '../router'
  import Utils from '../utils'
  import ProgressDialog from './ProgressDialog'
  import { bus } from '../main'
  import Cookie from 'js-cookie'

  export default {
    name: 'Accounts',
    components: {
      ProgressDialog
    },
    data () {
      return {
        inProgress: false,
        accountsTitle: 'Your accounts',
        balance: 0,
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT
      }
    },
    mounted () {
      Utils.getBalance()
        .then(balance => {
          this.balance = balance
        })
      bus.$on('refresh', (message) => {
        Utils.getBalance()
          .then(balance => {
            this.balance = balance
          })
      })
      this.accountsTitle = Cookie.get('userName') + '\'s accounts'
    },
    methods: {
      operate: function () {
        router.replace('/operate')
      }
    }
  }
</script>

<style scoped>
</style>
