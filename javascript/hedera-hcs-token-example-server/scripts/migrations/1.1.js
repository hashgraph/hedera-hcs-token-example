'use strict'
const dotEnv = require('dotenv')
const { Sequelize } = require('sequelize');

dotEnv.config()

module.exports = {
  up: async(query) => {
    console.log('running 1.1')
    const sql = 'INSERT INTO tokens (topic_id) VALUES ("' + process.env.TOPIC_ID + '")'
    return query.sequelize.query(
        sql,{ raw: true });
  },
  down: async(query) => {
    await query.dropTable('addresses');
  }
}