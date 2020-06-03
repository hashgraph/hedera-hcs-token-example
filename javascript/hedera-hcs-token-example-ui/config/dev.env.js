'use strict'

const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  HOST_PORT: '"http://localhost:3128"',
  HOST_WS: '"ws://localhost:5000"'
})
