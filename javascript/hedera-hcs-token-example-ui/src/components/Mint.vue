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
          title="Mint"
        >
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title class="headline">Mint {{ name }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>

          <v-form v-model="valid">
            <v-container
              fill-height
              fluid>
              <v-row>
                <v-col>
                  <v-text-field
                    v-model.number="quantity"
                    :rules="quantityRules"
                    label="Quantity to mint"
                    required
                  ></v-text-field>
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
                Mint
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
  // import * as Proto from '../proto/messages_pb'
  // import * as Utils from '../utils'
  import Cookie from 'js-cookie'
  import ProgressDialog from './ProgressDialog'
  import { bus } from '../main'
  import router from '../router'

  export default {
    name: 'Mint',
    components: {
      ProgressDialog
    },
    data () {
      return {
        valid: false,
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT,
        name: Cookie.get('tokenName'),
        quantity: 0,
        inProgress: false,
        quantityRules: [
          v => v.toString().length > 0 || 'Quantity is required',
          v => parseInt(v).toString() === v || 'Quantity must be an integer',
          v => v.valueOf() >= 1 || 'Quantity must be more than 1'
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

        body.quantity = this.quantity

        this.inProgress = true

        axios.post(this.restAPI.concat('/v1/token/mint'), body)
          .then(response => {
            this.inProgress = false
            console.log(response.data.message)
            if (response.data.status) {
              Cookie.set('userName', '', { expires: 365 })
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
