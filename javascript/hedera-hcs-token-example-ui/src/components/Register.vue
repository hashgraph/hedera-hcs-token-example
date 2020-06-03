<template>
  <div>
    <ProgressDialog :show="inProgress"></ProgressDialog>
    <v-row
      justify="center"
      align="start"
    >
      <v-col cols="12"
             sm="8"
             md="4">

        <v-card
          class="mx-auto"
          max-width="344"
          raised
          title="Register"
        >
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline">Register</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-form v-model="valid">
            <v-container
              fill-height
              fluid>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="surname"
                    :rules="nameRules"
                    label="Surname"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="forename"
                    :rules="nameRules"
                    label="Forename"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="idProof"
                    :rules="nameRules"
                    label="Proof of Identification"
                  >
                  </v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="userName"
                    :rules="nameRules"
                    label="Username"
                  >
                  </v-text-field>
                </v-col>
              </v-row>
            </v-container>
          </v-form>

          <v-card-actions
          >
            <v-col class="text-right">
              <v-btn
                right
                v-if="valid"
                color="deep-purple lighten-2"
                text
                @click="postToken"
              >
                Register
              </v-btn>
            </v-col>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script>
  import axios from 'axios'
  import Cookie from 'js-cookie'
  import ProgressDialog from './ProgressDialog'
  import { bus } from '../main'
  import Utils from '../utils'
  import router from '../router'

  const {
    Ed25519PrivateKey
  } = require('@hashgraph/sdk')

  export default {
    name: 'Register',
    components: {
      ProgressDialog
    },
    data () {
      return {
        valid: false,
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT,
        forename: Utils.getRandomFirstName(),
        surname: Utils.getRandomLastName(),
        idProof: Utils.getRandomId(), // 'KJUH1232232',
        userName: '',
        inProgress: false,
        nameRules: [
          v => !!v || 'Entry is required'
        ]
      }
    },
    created () {
      bus.$on('refresh', (message) => {
        router.go(0)
      })
    },
    methods: {
      postToken: function () {
        const body = {}
        // generate a new private key
        Ed25519PrivateKey.generate()
          .then(privateKey => {
            const publicKey = privateKey.publicKey
            body.publicKey = publicKey.toString()
            body.userName = this.userName

            this.inProgress = true

            axios.post(this.restAPI.concat('/v1/token/join'), body)
              .then(response => {
                this.inProgress = false
                console.log(response.data.message)
                if (response.data.status) {
                  Cookie.set('friendlyName', this.forename + ' ' + this.surname, {expires: 365})
                  Cookie.set('userName', this.userName, {expires: 365})
                  Cookie.set('userKey', privateKey.toString(), {expires: 365})
                  this.$socketClient.sendObj({userId: Cookie.get('userName')})

                  bus.$emit('showSuccess', response.data.message)
                } else {
                  bus.$emit('showError', response.data.message)
                }
              })
              .catch(e => {
                this.inProgress = false
                bus.$emit('showError', e)
                console.log(e)
              })
          })
      }
    }
  }
</script>

<style scoped>
</style>
