const Umzug = require('umzug')
const Sequelize = require('sequelize');
const dotEnv = require('dotenv')
const path = require('path')

dotEnv.config()

const sequelize = new Sequelize({
  dialect: 'sqlite',
  storage: process.env.DATABASE_LOCATION
});

const umzug = new Umzug({
  migrations: {
    // indicates the folder containing the migration .js files
    path: path.join(__dirname, './migrations'),
    pattern: /\.js$/,
    // inject sequelize's QueryInterface in the migrations
    params: [
      sequelize.getQueryInterface()
    ]
  },
  // indicates that the migration data should be store in the database
  // itself through sequelize. The default configuration creates a table
  // named `SequelizeMeta`.
  storage: 'sequelize',
  storageOptions: {
    sequelize: sequelize
  }
})

function logUmzugEvent (eventName) {
  return function (name, migration) {
    console.log(`${name} ${eventName}`)
  }
}

;(async () => {
  // checks migrations and run them if they are not already applied
  console.log('migrating')
  await umzug.up()
  console.log('All migrations performed successfully')
})()

// // using event listeners to log events
// umzug.on('migrating', logUmzugEvent('migrating'))
// umzug.on('migrated', logUmzugEvent('migrated'))
// umzug.on('reverting', logUmzugEvent('reverting'))
// umzug.on('reverted', logUmzugEvent('reverted'))
//
// // this will run your migrations
// umzug.up().then(console.log('all migrations done'))
