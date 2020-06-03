'use strict'
const dotEnv = require('dotenv');
const { Sequelize } = require('sequelize');
const { QueryTypes } = require('sequelize');

const {
  Ed25519PrivateKey
} = require('@hashgraph/sdk')

dotEnv.config()

module.exports = {
  up: async(query) => {

    console.log('running 1.2')

    const name = process.env.TOKEN_NAME;
    const symbol = process.env.SYMBOL;
    const totalSupply = process.env.TOTAL_SUPPLY;
    const decimals = process.env.DECIMALS;
    const topicId = process.env.TOPIC_ID;
    const publicKey = Ed25519PrivateKey.fromString(process.env.OPERATOR_KEY).publicKey.toString();

    if (typeof (name) !== 'undefined') {

      await query.sequelize.query(
        'UPDATE tokens SET name = ?, symbol = ?, total_supply = ?, decimals = ?, topic_id = ?',
        {
          replacements: [name, symbol, totalSupply, decimals, topicId],
          type: QueryTypes.UPDATE
        }
      );

      await query.sequelize.query(
        'INSERT INTO addresses (public_key, owner) VALUES (?, 1)',
        {
          replacements: [publicKey],
          type: QueryTypes.INSERT
        }
      );
    } else if (typeof (topicId) !== 'undefined') {
      await query.sequelize.query(
          'UPDATE tokens SET topic_id = ?, decimals = 0',
          {
            replacements: [topicId],
            type: QueryTypes.UPDATE
          }
      );
    }
  },
  down: async(query) => {
    await query.dropTable('addresses');
  }
}
