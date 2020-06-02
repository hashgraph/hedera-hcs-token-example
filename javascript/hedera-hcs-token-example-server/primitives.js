const BigNumber = require('bignumber.js');
const Nacl = require('tweetnacl')
const Token = require('./token')
const Messages = require('./proto/messages_pb')
const HederaClient = require('./hederaClient')
const DbService = require('./database')
const Utils = require('./utils')

const {
    ConsensusMessageSubmitTransaction
} = require('@hashgraph/sdk')

const construct = function (signature, primitive, ownerAddress) {
    Token.getToken()
        .then(token => {
            if (token.name === '') {
                token.name = primitive.getName();
                token.symbol = primitive.getSymbol();
                token.decimals = primitive.getDecimals();

                DbService.construct(signature, token, ownerAddress)
                    .then(result => {
                        console.info('Token constructed');
                    })
                    .catch(error => {
                        console.error(error);
                    });

            } else {
                const error = "Construct - Token already constructed";
                console.warn(error);
            }
        });
}

const mint = function (signature, primitive, ownerAddress) {
    Token.getToken()
        .then(token => {
            if (token.owner != ownerAddress) {
                console.warn('Address is not token owner\'s address');
            } else {
                const quantity = (primitive.getQuantity() * Math.pow(10, token.decimals));
                token.totalSupply = token.totalSupply + quantity;

                DbService.mint(signature, token, ownerAddress, quantity)
                    .then(result => {
                        console.info('Token minted - ' + quantity);
                    })
                    .catch(error => {
                        console.error(error);
                    });
            }

        });
}

const join = function (signature, primitive, ownerAddress) {
    Token.getToken()
        .then(token => {
            if (token.owner != ownerAddress) {
                console.warn('Address is not token owner\'s address');
            } else {
                DbService.join(signature, primitive.getAddress(), primitive.getUsername())
                    .then(result => {
                        if (result === 'created') {
                            console.info('Address ' + primitive.getAddress() + ' joined');
                        } else {
                            console.warn('Address ' + primitive.getAddress() + ' already joined');
                        }
                    })
                    .catch(error => {
                        console.error(error);
                    })
            }
        });
}

const mintTo = function (signature, primitive, ownerAddress) {
    Token.getToken()
        .then(token => {
            if (token.owner != ownerAddress) {
                console.warn('Address is not token owner\'s address');
            } else {
                DbService.mintTo(signature, primitive.getAddress(), primitive.getQuantity())
                    .then(result => {
                        console.warn('Minted ' + primitive.getQuantity() + ' to ' + primitive.getAddress());
                    })
                    .catch(error => {
                        console.error(error);
                    })
            }
        });
}

const burn = function (signature, primitive, address) {
    Token.getToken()
        .then(token => {
            DbService.burn(signature, address, primitive.getAmount())
                .then(result => {
                    console.warn('Burnt ' + primitive.getAmount() + ' from ' + address);
                })
                .catch(error => {
                    console.error(error);
                })
        });
}

const transfer = function (signature, primitive, addressFrom) {
    Token.getToken()
        .then(token => {
            DbService.transfer(signature, addressFrom, primitive.getToAddress(), primitive.getQuantity())
                .then(result => {
                    console.warn('Transferred ' + primitive.getQuantity() + ' from ' + addressFrom + ' to ' + primitive.getToAddress());
                })
                .catch(error => {
                    console.error(error);
                })
        });
}

module.exports = {
    construct,
    mint,
    join,
    mintTo,
    burn,
    transfer
}
