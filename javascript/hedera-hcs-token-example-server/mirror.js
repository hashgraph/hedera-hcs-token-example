const dotEnv = require('dotenv')
const dbService = require('./database')
const utils = require('./utils')
const Token = require('./token')
const Primitives = require("./primitives")
const Nacl = require('tweetnacl')
const Utils = require('./utils')

const {
    MirrorClient,
    MirrorConsensusTopicQuery,
    Ed25519PublicKey
} = require('@hashgraph/sdk')

dotEnv.config()

const mirrorClient = new MirrorClient(process.env.MIRROR_ADDRESS)
let consensusTopicId;

let isListening = false
let listenAttempts = 0
let lastReceivedResponseTime = Date.now()
let token = new Token.Token();

exports.startListening = function () {
    // Guard against being called multiple times
    console.log('Mirror startListening')
    if (isListening) return
    isListening = true

    Token.getToken()
        .then(dbToken => {
            token = dbToken;
            consensusTopicId = token.topicId;
            lastReceivedResponseTime = token.lastConsensusTime;

            console.log('Mirror new MirrorConsensusTopicQuery()')
            new MirrorConsensusTopicQuery()
                .setTopicId(consensusTopicId)
                // .setStartTime(0)
                // .setStartTime(lastReceivedResponseTime)
                .subscribe(mirrorClient, (response) => {
                    listenAttempts = 0
                    lastReceivedResponseTime = response.consensusTimestamp.asDate()

                    console.log('Response from MirrorConsensusTopicQuery()')
                    const message = response.message
                    const timestamp = utils.secondsToDate(response.consensusTimestamp).toUTCString()
                    const sequence = response.sequenceNumber

                    handleNotification(response);
                }, (error) => {
                    console.warn('Mirror error')
                    console.warn(error)
                    listenAttempts += 1
                    setTimeout(() => {
                        console.log('reconnecting...')
                        this.startListening()
                    }, listenAttempts * 250)
                })
        })
        .catch(err => {
            console.error(err);
        })
}

const handleNotification = function(mirrorResponse) {
    const primitiveProto = proto.Primitive.deserializeBinary(mirrorResponse.message);
    const primitiveHeaderProto = primitiveProto.getHeader();

    // set last consensus time stamp
    token.lastConsensusTime = mirrorResponse.consensusTimestamp.asDate();

    const signatureHex = Utils.toHexString(primitiveHeaderProto.getSignature());
    dbService.addOperation(token, signatureHex)
        .then(() => {
            const signature = primitiveHeaderProto.getSignature_asU8();
            const address = primitiveHeaderProto.getPublickey();
            const publicKey = Ed25519PublicKey.fromString(address).toBytes();
            const random = primitiveHeaderProto.getRandom().toString();

            if (primitiveProto.hasConstruct()) {
                console.log('construct received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getConstruct(), random)) {
                    Primitives.construct(signatureHex, primitiveProto.getConstruct(), address);
                } else {
                    console.warn('invalid signature');
                }
            } else if (primitiveProto.hasMint()) {
                console.log('mint received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getMint(), random)) {
                    Primitives.mint(signatureHex, primitiveProto.getMint(), address);
                } else {
                    console.warn('invalid signature');
                }
            } else if (primitiveProto.hasJoin()) {
                console.log('join received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getJoin(), random)) {
                    Primitives.join(signatureHex, primitiveProto.getJoin(), address);
                } else {
                    console.warn('invalid signature');
                }
            } else if (primitiveProto.hasMintTo()) {
                console.log('mintto received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getMintTo(), random)) {
                    Primitives.mintTo(signatureHex, primitiveProto.getMintTo(), address);
                }
            } else if (primitiveProto.hasBurn()) {
                console.log('burn received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getBurn(), random)) {
                    Primitives.burn(signatureHex, primitiveProto.getBurn(), primitiveProto.getHeader().getPublickey());
                }
            } else if (primitiveProto.hasTransfer()) {
                console.log('transfer received from mirror');
                if (signatureValid(signature, publicKey, primitiveProto.getTransfer(), random)) {
                    Primitives.transfer(signatureHex, primitiveProto.getTransfer(), primitiveProto.getHeader().getPublickey());
                }
            }

        })
        .catch(err => {
            if (err.toString().includes('UNIQUE constraint')) {
                console.warn("Duplicate Operation hash detected - skipping");
            } else {
                console.error(err);
            }
        })

}

const signatureValid = function (signature, publicKey, toVerify, random) {
    const verifyMe = toVerify.toArray().concat(Array.from(random));
    return Nacl.sign.detached.verify(Uint8Array.from(verifyMe), signature, publicKey);
}


