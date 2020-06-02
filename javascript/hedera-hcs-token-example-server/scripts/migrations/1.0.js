'use strict'
const dotEnv = require('dotenv')
const { Sequelize } = require('sequelize');

dotEnv.config()

module.exports = {
  up: async(query) => {
    console.log('running 1.0')
    await query.createTable('addresses', {
      id: {type: Sequelize.INTEGER, allowNull: false, primaryKey: true},
      balance: {type: Sequelize.BIGINT, defaultValue: 0},
      public_key: {type: Sequelize.STRING, allowNull: false, unique: true},
      owner: {type: Sequelize.INTEGER, allowNull: false, defaultValue: 0},
      username: {type: Sequelize.STRING, allowNull: false, defaultValue: 'Token', unique: true},
    });
    await query.createTable('allowances', {
      id: {type: Sequelize.INTEGER, allowNull: false, primaryKey: true},
      address_id: {type: Sequelize.INTEGER, allowNull: false},
      allowed_key: {type: Sequelize.STRING, allowNull: false},
      allowance: {type: Sequelize.BIGINT, allowNull: false}
    });
    await query.createTable('tokens', {
      id: {type: Sequelize.INTEGER, allowNull: false, primaryKey: true},
      total_supply: {type: Sequelize.BIGINT, defaultValue: 0},
      symbol: {type: Sequelize.STRING, defaultValue: ''},
      name: {type: Sequelize.STRING, defaultValue: ''},
      decimals: {type: Sequelize.INTEGER, defaultValue: ''},
      last_consensus_time: {type: Sequelize.DATE, defaultValue: 0},
      topic_id: {type: Sequelize.STRING, allowNull: false},
    });
    await query.createTable('operations', {
      id: {type: Sequelize.INTEGER, allowNull: false, primaryKey: true},
      signature: {type: Sequelize.STRING, allowNull: false, unique: true},
      operation: {type: Sequelize.STRING},
      from_username: {type: Sequelize.STRING},
      to_username: {type: Sequelize.STRING},
      amount: {type: Sequelize.STRING},
      status: {type: Sequelize.STRING}
    });
  },
  down: async(query) => {
    await query.dropTable('addresses');
  }
}
