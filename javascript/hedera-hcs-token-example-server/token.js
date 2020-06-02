const dbService = require('./database')

const {
    ConsensusTopicId
} = require('@hashgraph/sdk')

class Token {
    totalSupply = 0;
    symbol = 0;
    name = '';
    decimals = 0;
    lastConsensusTime = 0;
    topicId = 0;
    owner = '';

    constructor() {
    }

}

let token = new Token();

const getToken = function () {
    return new Promise(function (resolve, reject) {
        // get or create the topic id for this token
        dbService.getToken()
            .then(dbToken => {
                token.name = dbToken.name;
                token.topicId = ConsensusTopicId.fromString(dbToken.topicId);
                token.decimals = dbToken.decimals;
                token.symbol = dbToken.symbol;
                token.lastConsensusTime = dbToken.lastConsensusTime;
                token.totalSupply = dbToken.totalSupply;

                console.log('Using topic id ' + token.topicId.toString());

                return token;
            })
            .then(token => {
                dbService.getOwner()
                    .then(owner => {
                        token.owner = owner;
                        resolve(token);
                    })
            })
            .catch(err => {
                // more serious error, print to console and exit
                console.error(err);
                process.exit(1);
            })
    })
}


module.exports = {
    Token,
    getToken
}
