const sqlite3 = require('sqlite3').verbose()
const dotEnv = require('dotenv')
const Utils = require('./utils')
const WebSockets = require('./webSockets')

dotEnv.config({ debug: process.env.DEBUG })
const db = new sqlite3.Database(process.env.DATABASE_LOCATION)
const failed = 'failed'
const complete = 'complete'

const getUserExists = function (username) {
  const sql = `SELECT count(*) as count FROM addresses where username = ?`
  return new Promise(function (resolve, reject) {
    const exists = {
      exist: false
    };

    db.all(sql, [ username ], (err, rows) => {
      if (err) {
        reject(err);
      }
      if (rows[0].count === 1) {
        exists.exist = true
      }
      resolve(exists);
    })
  })
}

const getBalance = function (address) {
  const sql = `SELECT balance FROM addresses where public_key = ?`
  return new Promise(function (resolve, reject) {
    const balance = {
      balance: 0
    };

    db.all(sql, [ address ], (err, rows) => {
      if (err) {
        reject(err);
      }
      if (rows.length !== 0) {
        balance.balance = rows[0].balance;
      }
      resolve(balance);
    })
  })
}

const getUsersExcept = function (address) {
  const sql = `SELECT username as text FROM addresses where public_key <> ? AND username <> ? and username <> 'Token' order by username`
  return new Promise(function (resolve, reject) {

    db.all(sql, [ address, address ], (err, rows) => {
      if (err) {
        reject(err);
      } else {
        resolve(rows);
      }
    })
  })
}

const getToken = function () {
  const sql = `SELECT t.* FROM tokens t`
  return new Promise(function (resolve, reject) {
    const token = {
      name: '',
      topicId: '',
      decimals: '',
      symbol: '',
      lastConsensusTime: '',
      totalSupply: '',
      owner: ''
    };

    db.all(sql, [], (err, rows) => {
      if (err) {
        reject(err);
      }
      if (rows.length !== 0) {
        token.name = rows[0].name;
        token.topicId = rows[0].topic_id;
        token.decimals = rows[0].decimals;
        token.symbol = rows[0].symbol;
        token.lastConsensusTime = rows[0].last_consensus_time;
        token.totalSupply = rows[0].total_supply;
        getOwner()
            .then(owner => {
              token.owner = owner
              resolve(token);
            }).catch(err => {
              reject(err)
        })
        // token.owner = rows[0].owner;
      } else {
        resolve(token);
      }
    })
  })
}

const getOwner = function () {
  const sql = `SELECT public_key FROM addresses WHERE owner = 1`;
  return new Promise(function (resolve, reject) {
    db.all(sql, [], (err, rows) => {
      if (err) {
        reject(err.message);
      }
      if (rows.length === 0) {
        // no owner
        resolve('');
      } else {
        resolve(rows[0].public_key);
      }
    })
  })
}

const construct = function (signature, token, ownerAddress) {
  const operation = 'construct';
  let sql = 'UPDATE tokens SET name = ?, symbol = ?, decimals = ?'
  return new Promise(function (resolve, reject) {
    let data = [token.name, token.symbol, token.decimals]
    db.run(sql, data, function (err) {
      if (err) {
        updateOperation (signature, operation, ownerAddress, '', '', failed);
        reject(err.message)
      } else {
        // create an address for the owner
        sql = 'INSERT INTO addresses (public_key, owner) VALUES (?, ?)';
        data = [ownerAddress, 1];
        db.run(sql, data, function (err) {
          if (err) {
            updateOperation (signature, operation, ownerAddress, '', '', failed);
            reject(err.message)
          } else {
            updateOperation (signature, operation, ownerAddress, '', '', complete);
            resolve(true)
          }
        })
      }
    })
  })
}

const mint = function (signature, token, ownerAddress, quantity) {
  const operation = 'mint';
  let sql = 'UPDATE tokens SET total_supply = ?'
  return new Promise(function (resolve, reject) {
    let data = [token.totalSupply]
    db.run(sql, data, function (err) {
      if (err) {
        updateOperation (signature, operation, ownerAddress, '', '', failed);
        reject(err.message)
      } else {
        // update balance for the owner
        sql = 'UPDATE addresses SET balance = balance + ? WHERE public_key = ?';
        data = [quantity, ownerAddress];
        db.run(sql, data, function (err) {
          if (err) {
            updateOperation (signature, operation, ownerAddress, '', '', failed);
            reject(err.message)
          } else {
            updateOperation (signature, operation, ownerAddress, '', '', complete);
            resolve(true)
          }
        })
      }
    })
  })
}

const join = function (signature, address, username) {
  const operation = 'join';
  return new Promise(function (resolve, reject) {
    let sql = 'SELECT * FROM addresses WHERE public_key = ?'
    let data = [address]

    db.all(sql, data, (err, rows) => {
      if (err) {
        updateOperation (signature, operation, address, '', '', failed);
        reject(err.message);
      }
      if (rows.length === 0) {
        // create
        let sql = 'INSERT INTO addresses (public_key, username) VALUES ( ?, ?)'
        let data = [address, username]
        db.run(sql, data, function (err) {
          if (err) {
            updateOperation (signature, operation, address, username, '', failed);
            reject(err.message)
          } else {
            updateOperation (signature, operation, address, username, '', complete);
            resolve('created')
          }
        })
      } else {
        updateOperation (signature, operation, address, '', '', complete);
        resolve('existed');
      }
    })
 })
}

const mintTo = function (signature, address, quantity) {
  const operation = 'buy';
  return new Promise(function (resolve, reject) {
    let sql = 'UPDATE addresses SET balance = balance + ? WHERE public_key = ? OR username = ?'
    let data = [quantity, address, address]

    db.all(sql, data, (err, rows) => {
      if (err) {
        updateOperation (signature, operation, 'Token', address, quantity, failed);
        reject(err.message);
      } else {
        let sql = 'UPDATE tokens SET total_supply = total_supply + ?'
        let data = [quantity]
        db.run(sql, data, function (err) {
          if (err) {
            updateOperation (signature, operation, 'Token', address, quantity, failed);
            reject(err.message)
          } else {
            updateOperation (signature, operation, 'Token', address, quantity, complete);
            resolve('minted ' + quantity + ' to address/username')
          }
        })
      }
    })
  })
}

const burn = function (signature, address, quantity) {
  const operation = 'redeem';
  return new Promise(function (resolve, reject) {
    getBalance(address)
        .then(balance => {
          let newBalance = balance.balance - quantity;
          if (newBalance < 0) {
            newBalance = 0;
          }
          const burnAmount = balance.balance - newBalance;

          let sql = 'UPDATE addresses SET balance = ? WHERE public_key = ? OR username = ?'
          let data = [newBalance, address, address]

          db.all(sql, data, (err, rows) => {
            if (err) {
              updateOperation (signature, operation, address, 'Token', quantity, failed);
              reject(err.message);
            } else {
              let sql = 'UPDATE tokens SET total_supply = total_supply - ?'
              let data = [burnAmount]
              db.run(sql, data, function (err) {
                if (err) {
                  updateOperation (signature, operation, address, 'Token', quantity, failed);
                  reject(err.message)
                } else {
                  updateOperation (signature, operation, address, 'Token', burnAmount, complete);
                  resolve('burnt ' + quantity + ' from address/username')
                }
              })
            }
          })
        })
        .catch(error => {
          reject(error)
        })
  })
}

const transfer = function (signature, addressFrom, addressTo, quantity) {
  const operation = 'transfer';
  return new Promise(function (resolve, reject) {
    getBalance(addressFrom)
        .then(balance => {
          let newBalance = balance.balance - quantity;
          if (newBalance < 0) {
            newBalance = 0;
          }
          const transferAmount = balance.balance - newBalance;

          let sql = 'UPDATE addresses SET balance = ? WHERE public_key = ? OR username = ?'
          let data = [newBalance, addressFrom, addressFrom]

          db.all(sql, data, (err, rows) => {
            if (err) {
              updateOperation (signature, operation, addressFrom, addressTo, quantity, failed);
              reject(err.message);
            } else {
              let sql = 'UPDATE addresses SET balance = balance + ? WHERE public_key = ? OR username = ?'
              let data = [transferAmount, addressTo, addressTo]
              db.run(sql, data, function (err) {
                if (err) {
                  updateOperation (signature, operation, addressFrom, addressTo, quantity, failed);
                  reject(err.message)
                } else {
                  updateOperation (signature, operation, addressFrom, addressTo, transferAmount, complete);
                  resolve('transferred ' + quantity + ' from address/username')
                }
              })
            }
          })
        })
        .catch(error => {
          reject(error)
        })
  })
}

const addOperation = function (token, signature) {
  return new Promise(function (resolve, reject) {
    let sql = 'UPDATE tokens SET last_consensus_time = ?'
    let data = [token.lastConsensusTime]
    db.run(sql, data, function (err) {
      if (err) {
        reject(err.message)
      } else {
        sql = 'INSERT INTO operations (signature) VALUES (?)'
        const data = [ signature ]
        db.run(sql, data, function (err) {
          if (err) {
            reject(err.message)
          } else {
            resolve(true)
          }
        })
      }
    })
  })
}

const userFromAddress = function(address) {
  return new Promise( function (resolve, reject) {
    if (address.startsWith('302a')) {
      const sql = 'SELECT username FROM addresses WHERE public_key = ?'
      db.all(sql, [ address ], (err, rows) => {
        if (err) {
          reject(err);
        }
        if (rows.length === 1) {
          resolve(rows[0].username);
        } else {
          resolve(address);
        }
      })
    } else {
      resolve(address)
    }
  })
}

const getOperations = function (userName) {
  return new Promise( function (resolve, reject) {
    const sql = 'SELECT * FROM operations WHERE from_username = ? OR to_username = ? order by id desc'
    db.all(sql, [ userName, userName ], (err, rows) => {
      if (err) {
        reject(err);
      } else {
        resolve(rows);
      }
    })
  })
}

const updateOperation = function (signature, operation, from, to, amount, status) {
  return new Promise(function (resolve, reject) {
    userFromAddress(from)
        .then(fromUser => {
          userFromAddress(to)
              .then (toUser => {
                const sql = 'UPDATE operations SET operation = ?, from_username = ?, to_username = ?, amount = ?, status = ? where signature = ?'
                const data = [ operation, fromUser, toUser, amount, status, signature ]
                db.run(sql, data, function (err) {
                  const notification = {}
                  notification.operation = operation
                  notification.from = fromUser
                  notification.to = toUser
                  notification.amount = amount
                  notification.status = status

                  if ((fromUser !== '') && (fromUser !== 'Token')) {
                    WebSockets.sendNotification(fromUser, notification)
                  }
                  if ((toUser !== '') && (toUser !== 'Token')) {
                    WebSockets.sendNotification(toUser, notification)
                  }
                  if (err) {
                    reject(err.message)
                  } else {
                    resolve(true)
                  }
                })
              })
        })

    })
}

module.exports = {
  getUserExists,
  getToken,
  getOwner,
  addOperation,
  construct,
  mint,
  join,
  getBalance,
  mintTo,
  burn,
  transfer,
  getUsersExcept,
  getOperations
}
