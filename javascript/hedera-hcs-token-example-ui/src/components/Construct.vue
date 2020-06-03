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
          title="Construct"
        >
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline">Construct</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-form v-model="valid">
            <v-container
              fill-height
              fluid>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="name"
                    :rules="nameRules"
                    :counter="10"
                    label="Token Name"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model="symbol"
                    :rules="symbolRules"
                    :counter="3"
                    label="Token Symbol"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model.number="decimals"
                    :rules="decimalsRules"
                    label="Token Decimals"
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
                Construct
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
  import router from '../router'

  export default {
    name: 'Construct',
    components: {
      ProgressDialog
    },
    data () {
      return {
        valid: false,
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT,
        name: '',
        symbol: '',
        decimals: 0,
        inProgress: false,
        nameRules: [
          v => !!v || 'Name is required',
          v => v.length <= 10 || 'Name must be less than 10 characters'
        ],
        symbolRules: [
          v => !!v || 'Symbol is required',
          v => v.length === 3 || 'Symbol must be 3 characters'
        ],
        decimalsRules: [
          v => v.toString().length > 0 || 'Decimals is required (0 is accepted)',
          v => parseInt(v).toString() === v || 'Decimals must be an integer',
          v => v.valueOf() <= 18 || 'Decimals must be below 18'
        ]
      }
    },
    created () {
      bus.$on('refresh', (message) => {
        router.replace('/')
      })
    },
    methods: {
      postToken: function () {
        const body = {}

        body.name = this.name
        body.symbol = this.symbol
        body.decimals = this.decimals

        this.inProgress = true

        axios.post(this.restAPI.concat('/v1/token/construct'), body)
          .then(response => {
            this.inProgress = false
            console.log(response.data.message)
            if (response.data.status) {
              Cookie.set('userName', '', {expires: 365})
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
      }
    }
  }
</script>

<style scoped>
</style>
