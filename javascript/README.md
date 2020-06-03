# HCS Stable Coin

This is a sample implementation of a stable coin in a Hedera Consensus Service(HCS) Decentralized Application.

## Overview

The purpose is to show how to build the necessary components to implement a stable token on HCS.
* Definition for messages (this is done in protobuf here so that the implementation is easily portable to other languages)
* A model for the state (Token and addresses primarily)
* A means to generate messages to send to HCS
* A means to subscribe to notifications from a mirror node and act upon the state as a result of processing the notifications.
* Ensuring that duplicate messages aren't processed to avoid double spend.

The javascript code contains both a back end (written in node.js) and a front end (Vue Progressive Web App) and should server as an example, not a production-ready implementation.

## End to end flow

### Token creation

The token is automatically created, called STABL, with no supply and no decimals. 
Refer to the backend readme to change this or not have the token created automatically.

### UI Launch

When the UI is first launched, it generates a private key in which it stores in a cookie.

If the token doesn't exist in the database, the UI will switch to a `Construct` screen where you can input the token's details.
Once constructed, wait for mirror notification (approx 10s) and refresh the UI.

If the token exists, but no `username` cookie exists, the UI switches to a `Register` screen enabling you to specify a username to join the application network.

This generates a HCS transaction to `join`, when the transaction is notified to the back end the username and their public key is added to the database.

The UI should then refresh and switch to the `accounts` view where the user can witness their token balance (the other balances are hard coded).

From there, click on the token button, will move the UI to the `operate` screen where it's possible to buy tokens, return them in exchange for fiat or send them to another user.

In all cases, the UI should refresh after the back end has received the mirror notification by way of websocket notifications from the back end to the UI.
