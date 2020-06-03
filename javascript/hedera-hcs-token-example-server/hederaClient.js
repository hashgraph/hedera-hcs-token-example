const dotEnv = require('dotenv')

dotEnv.config();

const {
    Client,
    Ed25519PrivateKey
} = require('@hashgraph/sdk')

const operatorPrivateKey = Ed25519PrivateKey.fromString(process.env.OPERATOR_KEY)
const operatorAccount = process.env.OPERATOR_ID
const client = Client.forTestnet()
client.setOperator(operatorAccount, operatorPrivateKey)

module.exports = {
    client,
    operatorPrivateKey,
    operatorAccount
}
