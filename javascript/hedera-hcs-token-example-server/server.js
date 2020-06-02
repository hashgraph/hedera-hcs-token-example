const express = require('express')
const bodyParser = require('body-parser');
const dotEnv = require('dotenv')
const app = express();
const Transactions = require('./transactions')
const Mirror = require('./mirror')
const DbService = require('./database')
const cors = require('cors')

dotEnv.config();

const PORT = process.env.PORT || 3128;

app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.json());
app.use(express.json())
app.use(cors())

Mirror.startListening()

app.get('/v1/token', function (req, res) {
    DbService.getToken()
        .then(dbToken => {
            return res.json(dbToken)
        })
        .catch(err => {
            console.log(err)
            res.sendStatus(501)
        })
})

app.get('/v1/token/balance/:address?', function (req, res) {
    if (typeof req.params.address !== 'undefined') {
        DbService.getBalance(req.params.address)
            .then(balance => {
                return res.json({
                    balance: balance
                })
            }).catch(err => {
                console.log(err)
                res.sendStatus(501)
        })
    } else {
        return res.json({
            balance: 0
        })
    }
})

app.get('/v1/token/users/:address?', function (req, res) {
    let address = '';
    if (typeof req.params.address !== 'undefined') {
        address = req.params.address;
    }

    DbService.getUsersExcept(req.params.address)
        .then(users => {
            return res.json({
                users: users
            })
        }).catch(err => {
        console.log(err)
        res.sendStatus(501)
    })
})

app.get('/v1/token/operations/:username', function (req, res) {
    DbService.getOperations(req.params.username)
        .then(operations => {
            return res.json({
                operations: operations
            })
        }).catch(err => {
            console.log(err)
            res.sendStatus(501)
        })
})

app.get('/v1/token/userExists/:userName?', function (req, res) {
    if ((typeof req.params.userName !== 'undefined') && (req.params.userName !== 'undefined') && (req.params.userName.length !== 0)) {
        DbService.getUserExists(req.params.userName)
            .then(userExists => {
                return res.json({
                    exists: userExists.exist
                })
            }).catch(err => {
                console.log(err)
                res.sendStatus(501)
            })
    } else {
        return res.json({
            exists: false
        })
    }
})
app.post('/v1/token/construct', function (req, res) {
    console.log('Construct request for ' + req.body.name);
    Transactions.construct(req.body.name, req.body.symbol, req.body.decimals)
        .then(result => {
            return res.json({
                status: true,
                message: 'Construct request sent'
            })
        })
        .catch(err => {
            return res.json({
                status: false,
                message: err
            })
        })
})

app.post('/v1/token/mint', function (req, res) {
    console.log('Mint request');
    Transactions.mint(req.body.quantity)
        .then(result => {
            return res.json({
                status: true,
                message: 'Mint request sent'
            })
        })
        .catch(err => {
            return res.json({
                status: false,
                message: err
            })
        })
})

app.post('/v1/token/join', function (req, res) {
    console.log('Join request');
    Transactions.join(req.body.publicKey, req.body.userName)
        .then(result => {
            return res.json({
                status: true,
                message: 'Join request sent'
            })
        })
        .catch(err => {
            return res.json({
                status: false,
                message: err
            })
        })
})

app.post('/v1/token/mintTo', function (req, res) {
    console.log('MintTo request');
    Transactions.mintTo(req.body.address, req.body.quantity)
        .then(result => {
            return res.json({
                status: true,
                message: 'MintTo request sent'
            })
        })
        .catch(err => {
            return res.json({
                status: false,
                message: err
            })
        })
})

app.post('/v1/token/transaction', function (req, res) {
    console.log('Raw transaction request');
    Transactions.raw(req.body.primitive)
        .then(result => {
            return res.json({
                status: true,
                message: 'Transaction request sent'
            })
        })
        .catch(err => {
            return res.json({
                status: false,
                message: err
            })
        })
})

app.listen(PORT, function () {
    console.log(`App running on localhost:${PORT}`)
})
