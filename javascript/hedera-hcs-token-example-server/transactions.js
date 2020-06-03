const Nacl = require('tweetnacl')
const Token = require('./token')
const Messages = require('./proto/messages_pb')
const HederaClient = require('./hederaClient')

const {
    ConsensusMessageSubmitTransaction,
    Ed25519PrivateKey
} = require('@hashgraph/sdk')

const construct = function (name, symbol, decimals) {
    return new Promise(function (resolve, reject) {
        try {
            const constructProto = new proto.Construct();
            constructProto.setName(name);
            constructProto.setSymbol(symbol);
            if (decimals != 0) {
                // else serialisation goes wrong with signatures
                constructProto.setDecimals(decimals);
            }

            let primitive = new proto.Primitive();
            primitive.setHeader(primitiveHeader(constructProto.toArray(), process.env.OPERATOR_KEY));
            primitive.setConstruct(constructProto);

            hcsSend(primitive)
                .then(result => {
                    resolve(true);
                })
                .catch(err => {
                    throw err;
            })
        } catch (e) {
            reject(e.message)
        }
    })
}

const mint = function (quantity) {
    return new Promise(function (resolve, reject) {
        try {
            const mintProto = new proto.Mint();
            if (quantity != 0) {
                // else serialisation goes wrong with signatures
                mintProto.setQuantity(quantity);
            }
            const key = Ed25519PrivateKey.fromString(process.env.OPERATOR_KEY).publicKey.toString();
            mintProto.setAddress(key);

            let primitive = new proto.Primitive();
            primitive.setHeader(primitiveHeader(mintProto.toArray(), process.env.OPERATOR_KEY));
            primitive.setMint(mintProto);

            hcsSend(primitive)
                .then(result => {
                    resolve(true);
                })
                .catch(err => {
                    reject(err);
                })
        } catch (e) {
            reject(e.message)
        }
    })
}

const join = function (address, username) {
    return new Promise(function (resolve, reject) {
        try {
            const joinProto = new proto.Join();
            joinProto.setAddress(address)
            joinProto.setUsername(username)

            let primitive = new proto.Primitive();
            primitive.setHeader(primitiveHeader(joinProto.toArray(), process.env.OPERATOR_KEY));
            primitive.setJoin(joinProto);

            hcsSend(primitive)
                .then(result => {
                    resolve(true);
                })
                .catch(err => {
                    reject(err);
                })
        } catch (e) {
            reject(e.message)
        }
    })
}

const mintTo = function (address, quantity) {
    return new Promise(function (resolve, reject) {
        try {
            const mintTo = new proto.MintTo();
            mintTo.setAddress(address)
            mintTo.setQuantity(quantity)

            let primitive = new proto.Primitive();
            primitive.setHeader(primitiveHeader(mintTo.toArray(), process.env.OPERATOR_KEY));
            primitive.setMintTo(mintTo);

            hcsSend(primitive)
                .then(result => {
                    resolve(true);
                })
                .catch(err => {
                    reject(err);
                })
        } catch (e) {
            reject(e.message)
        }
    })
}

const raw = function (base64Operation) {
    return new Promise(function (resolve, reject) {
        try {
            // Special use case here,
            // the user signs the operation (burn or transfer), so sends a complete transaction
            // payload which is base64 encoded

            const primitiveBuffer = Buffer.from(base64Operation, 'base64');
            const primitive = new proto.Primitive.deserializeBinary(primitiveBuffer);

            hcsSend(primitive)
                .then(result => {
                    resolve(true);
                })
                .catch(err => {
                    reject(err);
                })
        } catch (e) {
            reject(e.message)
        }
    })
}

const hcsSend = async function(primitive) {
    return new Promise(function (resolve, reject) {
        Token.getToken()
            .then(token => {
                new ConsensusMessageSubmitTransaction()
                    .setMessage(primitive.serializeBinary())
                    .setTopicId(token.topicId)
                    .execute(HederaClient.client)
                    .then(transactionId => {
                        console.log('Sent primitive - transaction id = ' + transactionId.toString())
                        return transactionId.getReceipt(HederaClient.client);
                    })
                    .then(receipt => {
                        console.log(receipt.getConsensusTopicSequenceNumber());
                        resolve(true);
                    })
                    .catch(err => {
                        console.error(err);
                        throw err;
                    })
            })
    })
}

const primitiveHeader = function (toSign, privateKey) {
    const rand = Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
    const randomString = rand.toString();

    // concatenate random long with data to sign
    const signMe = toSign.concat(Array.from(randomString));

    const key = Ed25519PrivateKey.fromString(privateKey);
    const signature = Nacl.sign.detached(Uint8Array.from(signMe)
        , key._keyData);

    const primitiveHeader = new proto.PrimitiveHeader();
    primitiveHeader.setRandom(rand);
    primitiveHeader.setSignature(signature);
    primitiveHeader.setPublickey(key.publicKey.toString());

    return primitiveHeader;
}

module.exports = {
    construct,
    mint,
    join,
    mintTo,
    raw
}
