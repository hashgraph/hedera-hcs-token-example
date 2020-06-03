const WSPORT = process.env.WSPORT || 5000;
const webSocketServer = new (require('ws')).Server({ port: WSPORT });
const webSockets = {}

webSocketServer.on('connection', function connection(webSocket) {
    let userID = ''
    console.log('Socket connection')
    webSocket.on('message', function incoming(message) {
        userID = JSON.parse(message).userId
        webSockets[userID] = webSocket
        console.log('connected: ' + userID)
    })

    // Forward Message
    //
    // Receive               Example
    // [toUserID, text]      [2, "Hello, World!"]
    //
    // Send                  Example
    // [fromUserID, text]    [1, "Hello, World!"]
    // webSocket.on('message', function(message) {
    //     console.log('received from ' + userID + ': ' + message)
    //     var messageArray = JSON.parse(message)
    //     var toUserWebSocket = webSockets[messageArray[0]]
    //     if (toUserWebSocket) {
    //         console.log('sent to ' + messageArray[0] + ': ' + JSON.stringify(messageArray))
    //         messageArray[0] = userID
    //         toUserWebSocket.send(JSON.stringify(messageArray))
    //     }
    // })

    webSocket.on('close', function () {
        delete webSockets[userID]
    })
})

exports.sendNotification = function  (destination, message) {
    const toUserWebSocket = webSockets[destination]
    if (toUserWebSocket) {
        toUserWebSocket.send(JSON.stringify(message))
    }
}
