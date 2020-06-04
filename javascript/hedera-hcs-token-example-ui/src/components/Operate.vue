<template>
  <div>
    <ProgressDialog :show="inProgress"></ProgressDialog>

    <v-toolbar dense>
      <v-btn icon @click="home">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title>Payment Application</v-toolbar-title>
    </v-toolbar>
    <v-sheet
      height="100%"
    >

    <v-row
      justify="center"
      dense
    >
      <v-col cols="12">

        <v-card
          class="mx-auto text-center mt-2"
          max-width="344"
          raised
        >
          <v-card-text>
            <p class="display-1 text--primary">
              {{ accountTitle }}
            </p>
            <p class="display-2 text--primary">
              {{ tokenName }}
            </p>
            <p class="display-1 text--primary">
              $ {{ balance }}
            </p>
          </v-card-text>
          <v-card-actions>
            <v-btn
              text
              color="grey lighten-1"
            >
              Request
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              text
              color="deep-purple accent-4"
              @click="sellDialog = true"
            >
              Redeem
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn
              text
              color="deep-purple accent-4"
              @click="sendDialog = true"
            >
              Send
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
      <v-col cols="12">
        <v-card
          class="mx-auto"
          max-width="344"
          raised
        >
          <v-list-item>
            <v-list-item-content>
              <v-row no-gutters align="center">
                <v-col cols="8">
                  Get more {{ tokenName }}
                </v-col>
                <v-spacer></v-spacer>
                <v-col cols="4" align="end">
                  <v-btn
                    text
                    color="deep-purple accent-4"
                    @click="buyDialog = true"
                  >
                    Buy
                  </v-btn>
                </v-col>
              </v-row>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>

      <v-col cols="12">

        <v-card
          class="mx-auto"
          max-width="344"
          raised
        >
          <v-row>
            <v-col cols="12">
              <v-list-item>
                <v-list-item-content>
                  recent activity
                </v-list-item-content>
              </v-list-item>
            </v-col>
          </v-row>
          <v-row dense v-for="operation in operations" :key="operation.id">
            <v-col cols="2">
              <v-list-item two-line>
                <v-list-item-avatar>
                  <v-icon :color=operation.iconColor>{{ operation.icon }}</v-icon>
                </v-list-item-avatar>
              </v-list-item>
            </v-col>
            <v-col cols="5">
              <v-list-item two-line>
                <v-list-item-content>
                  <v-list-item-title>{{ operation.operation }}</v-list-item-title>
                  <v-list-item-subtitle>{{ operation.operationDetail }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-col>
            <v-col cols="5" align="end">
              <v-list-item two-line>
                <v-list-item-content>
                  <v-list-item-title>{{ operation.amount }}</v-list-item-title>
                  <v-list-item-subtitle :class=operation.textColor>{{ operation.status }}</v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-col>
          </v-row>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              text
              color="grey lighten-1"
            >
              See more
            </v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    <!-- BUY dialog -->
    <v-row justify="center">
      <v-dialog v-model="buyDialog" persistent max-width="600px">
        <v-card>
          <v-card-title>
            <span class="headline">{{ tokenName }} Purchase</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-form v-model="buyValid">
                <v-container
                  fill-height
                  fluid>
                  <v-row>
                    <v-col>
                      <v-text-field
                        v-model="buyQuantity"
                        :rules="quantityRules"
                        label="Amount to purchase"
                        required
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-container>
              </v-form>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" text @click="buyDialog = false">Cancel</v-btn>
            <v-btn v-if="buyValid"  color="blue darken-1" text @click="purchase">Proceed</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-row>
    <!-- Sell dialog -->
    <v-row justify="center">
      <v-dialog v-model="sellDialog" persistent max-width="600px">
        <v-card>
          <v-card-title>
            <span class="headline">{{ tokenName }} Redemption</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-form v-model="sellValid">
                <v-container
                  fill-height
                  fluid>
                  <v-row>
                    <v-col>
                      <v-text-field
                        v-model="sellQuantity"
                        :rules="quantityRules"
                        label="Amount to redeem"
                        required
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-container>
              </v-form>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="blue darken-1" text @click="sellDialog = false">Cancel</v-btn>
            <v-btn v-if="sellValid" color="blue darken-1" text @click="burn">Proceed</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <!-- Send dialog -->
      <v-row justify="center">
        <v-dialog v-model="sendDialog" persistent max-width="600px">
          <v-card>
            <v-card-title>
              <span class="headline">{{ tokenName }} Send</span>
            </v-card-title>
            <v-card-text>
              <v-container>
                <v-form v-model="sendValid">
                  <v-container
                    fill-height
                    fluid>
                    <v-row>
                      <v-col>
                        <v-text-field
                          v-model="sendQuantity"
                          :rules="quantityRules"
                          label="Amount to send"
                          required
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col>
                        <v-select
                          v-model="sendRecipient"
                          :items="sendToList"
                          label="Send to"
                          :rules="[(v) => !!v || 'Recipient is required']"
                          required
                        ></v-select>
                      </v-col>
                    </v-row>
                  </v-container>
                </v-form>
              </v-container>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn color="blue darken-1" text @click="sendDialog = false">Cancel</v-btn>
              <v-btn v-if="sendValid" color="blue darken-1" text @click="send">Proceed</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-row>
    </v-row>
    </v-sheet>
  </div>
</template>

<script>
  import axios from 'axios'
  import Cookie from 'js-cookie'
  import Utils from '../utils'
  import ProgressDialog from './ProgressDialog'
  import { bus } from '../main'
  import router from '../router'
  import * as Proto from '../proto/messages_pb'

  export default {
    name: 'Operate',
    components: {
      ProgressDialog
    },
    data () {
      return {
        restAPI: 'http://' + window.location.hostname + ':' + process.env.HOST_PORT,
        inProgress: false,
        balance: 0,
        tokenName: Cookie.get('tokenName'),
        buyDialog: false,
        sellDialog: false,
        sendDialog: false,
        buyValid: false,
        sellValid: false,
        sendValid: false,
        buyQuantity: 0,
        sellQuantity: 0,
        sendQuantity: 0,
        sendToList: [],
        sendRecipient: '',
        address: '',
        quantityRules: [
          v => v.toString().length > 0 || 'Amount is required',
          v => parseInt(v).toString() === v || 'Amount must be integer',
          v => v.valueOf() >= 1 || 'Amount must be more than 0'
        ],
        userName: Cookie.get('userName'),
        operations: [],
        accountTitle: Cookie.get('userName')
      }
    },
    created () {
      this.address = Cookie.get('userKey')
      this.userName = Cookie.get('userName')
      if ((typeof (this.address) === 'undefined') || (this.address === '')) {
        router.replace('/')
      } else {
        axios.get(this.restAPI.concat('/v1/token/userExists/' + this.userName))
          .then(response => {
            if (response.data.exists === false) {
              // user doesn't exist
              Utils.resetCookies()
              router.replace('/')
            }
          })
          .catch(e => {
            console.log(e)
          })
      }
      Utils.getBalance()
        .then(balance => {
          this.balance = balance
        })
      Utils.getUsers()
        .then(users => {
          this.sendToList = users
        })

      this.getOperations()

      bus.$on('refresh', (message) => {
        this.getOperations()
        Utils.getBalance()
          .then(balance => {
            this.balance = balance
          })
      })
    },
    methods: {
      home: function () {
        router.replace('/')
      },
      getOperations: function () {
        axios.get(this.restAPI.concat('/v1/token/operations/' + this.userName))
          .then(response => {
            this.operations = []
            // const icon = 'mdi-minus-circle' // mdi-plus-circle
            // const operation = ''
            // const operationDetail = ''
            // const amount = '+/- xxx'
            // const status = 'complete'

            let iMax = response.data.operations.length
            if (iMax > 5) {
              iMax = 5
            }
            for (let i = 0; i < iMax; i++) {
              const obj = response.data.operations[i]
              const operation = {}
              switch (obj.operation) {
                case 'construct':
                  operation.icon = 'mdi-wrench'
                  operation.iconColor = 'green darken-2'
                  operation.operation = 'Token Construction'
                  operation.operationDetail = ''
                  operation.amount = ''
                  break
                case 'mint':
                  operation.icon = 'mdi-wrench'
                  operation.iconColor = 'green darken-2'
                  operation.operation = 'Token minting'
                  operation.operationDetail = ''
                  operation.amount = '+' + obj.amount
                  break
                case 'join':
                  operation.icon = 'mdi-set-center'
                  operation.iconColor = 'green darken-2'
                  operation.operation = 'Joined token'
                  operation.operationDetail = ''
                  operation.amount = ''
                  break
                case 'buy':
                  operation.icon = 'mdi-plus-circle'
                  operation.iconColor = 'green darken-2'
                  operation.operation = 'Bought token'
                  operation.operationDetail = 'Purchase of ' + this.tokenName
                  operation.amount = '+' + obj.amount
                  break
                case 'redeem':
                  operation.icon = 'mdi-minus-circle'
                  operation.iconColor = 'red darken-2'
                  operation.operation = 'Sold token'
                  operation.operationDetail = ''
                  operation.amount = '-' + obj.amount
                  break
                case 'transfer':
                  operation.operation = 'Transfer token'
                  if (obj.from_username === this.userName) {
                    // from me to other
                    operation.icon = 'mdi-minus-circle'
                    operation.iconColor = 'red darken-2'
                    operation.operationDetail = 'Payment to ' + obj.to_username
                    operation.amount = '-' + obj.amount
                  } else {
                    operation.icon = 'mdi-plus-circle'
                    operation.iconColor = 'green darken-2'
                    operation.operationDetail = 'Payment from ' + obj.from_username
                    operation.amount = '+' + obj.amount
                  }
                  break
              }
              operation.status = obj.status
              if (obj.status === 'complete') {
                operation.textColor = 'green--text'
              } else {
                operation.textColor = 'deep-orange--text'
              }
              operation.id = obj.id
              this.operations.push(operation)
            }
          })
          .catch(e => {
            console.log(e)
          })
      },
      purchase: function () {
        this.buyDialog = false
        const body = {}
        body.address = this.userName
        body.quantity = this.buyQuantity

        this.inProgress = true

        axios.post(this.restAPI.concat('/v1/token/mintTo'), body)
          .then(response => {
            this.inProgress = false
            console.log(response.data.message)
            if (response.data.status) {
              bus.$emit('showSuccess', response.data.message)
            } else {
              bus.$emit('showError', response.data.message)
            }
          })
          .catch(e => {
            this.inProgress = false
            console.log(e)
            bus.$emit('showError')
          })
      },
      burn: function () {
        try {
          this.sellDialog = false
          const body = {}

          const burnProto = new Proto.Burn()
          burnProto.setAmount(this.sellQuantity)
          console.log(this.sellQuantity)

          let primitive = new Proto.Primitive()
          primitive.setHeader(Utils.primitiveHeader(burnProto.toArray(), this.address))
          primitive.setBurn(burnProto)

          const serializedPrimitive = primitive.serializeBinary()

          const base64 = btoa(String.fromCharCode.apply(null, serializedPrimitive))
          body.primitive = base64

          this.inProgress = true

          axios.post(this.restAPI.concat('/v1/token/transaction'), body)
            .then(response => {
              this.inProgress = false
              console.log(response.data.message)
              if (response.data.status) {
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
        } catch (e) {
          console.log(e)
        }
      },
      send: function () {
        try {
          this.sendDialog = false
          const body = {}

          const transferProto = new Proto.Transfer()
          transferProto.setToAddress(this.sendRecipient)
          transferProto.setQuantity(this.sendQuantity)

          let primitive = new Proto.Primitive()
          primitive.setHeader(Utils.primitiveHeader(transferProto.toArray(), this.address))
          primitive.setTransfer(transferProto)

          const serializedPrimitive = primitive.serializeBinary()

          const base64 = btoa(String.fromCharCode.apply(null, serializedPrimitive))
          body.primitive = base64

          this.inProgress = true

          axios.post(this.restAPI.concat('/v1/token/transaction'), body)
            .then(response => {
              this.inProgress = false
              console.log(response.data.message)
              if (response.data.status) {
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
        } catch (e) {
          console.log(e)
        }
      }
    }
  }
</script>

<style scoped>
</style>
