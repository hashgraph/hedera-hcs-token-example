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
                  Your Accounts
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
  import axios from 'axios'
  import Cookie from 'js-cookie'
  import router from '../router'
  import Utils from '../utils'
  import ProgressDialog from './ProgressDialog'
  import { bus } from '../main'

  const {
    Ed25519PrivateKey
  } = require('@hashgraph/sdk')

  export default {
    name: 'Accounts',
    components: {
      ProgressDialog
    },
    data () {
      return {
        restAPI: '',
        inProgress: false,
        balance: 0
      }
    },
    mounted () {
      this.restAPI = process.env.HOST_PORT
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
    },
    methods: {
      operate: function () {
        router.replace('/operate')
      },
      postToken: function () {
        const body = {}
        // generate a new private key
        Ed25519PrivateKey.generate()
          .then(privateKey => {
            const publicKey = privateKey.publicKey
            body.publicKey = publicKey.toString()

            this.inProgress = true

            axios.post(this.restAPI.concat('/v1/token/join'), body)
              .then(response => {
                this.inProgress = false
                console.log(response.data.message)
                if (response.data.status) {
                  this.userName = Cookie.set('userName', this.forename + ' ' + this.surname, {expires: 365})
                  this.userKey = Cookie.set('userKey', privateKey, {expires: 365})

                  bus.$emit('showSuccess', response.data.message)
                } else {
                  bus.$emit('showError', response.data.message)
                }
              })
              .catch(e => {
                this.inProgress = false
                console.log(e)
                bus.$emit('showError', e)
              })
          })
      }
    }
  }
</script>

<style scoped>
</style>
