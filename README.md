[![CircleCI](https://circleci.com/gh/hashgraph/hedera-hcs-token-example.svg?style=shield)](https://circleci.com/gh/hashgraph/hedera-hcs-token-example)
[![codecov](https://codecov.io/gh/hashgraph/hedera-hcs-token-example/branch/master/graph/badge.svg)](https://codecov.io/gh/hashgraph/hedera-hcs-token-example)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

# HCS Token

This is a sample implementation and ERC20 like token in a Hedera Consensus Service(HCS) Decentralized Application.

## Overview

The purpose is to show how to build the necessary components to implement a token on HCS.
* Definition for messages (this is done in protobuf here so that the implementation is easily portable to other languages)
* A model for the state (Token and addresses primarily)
* A means to generate messages to send to HCS
* A means to subscribe to notifications from a mirror node and act upon the state as a result of processing the notifications.

To keep things simple, it is operated with command line inputs and state is held in a json file.

## Process

### Operations

* User runs the application with command line inputs specifying an operation such as `construct`
* Inputs are validated and a primitive message specifying an operation such as `construct`, `mint`, `join` or `transfer` is prepared
* The operation is signed by the `OPERATOR_KEY` (private key) found in the `.env` file
* The signature is added to the primitive message
* The public key of the user (derived from OPERATOR_KEY in `.env`) is added to the primitive message
* This primitive message is sent to HCS for consensus
* No changes have been made to state (Except for construct which sets the topicId)

### Queries

* User runs the application with command line inputs specifying a query such as `balanceOf`
* The specified query is run against local state and printed to the console 

### Refresh

This is a "special" command which instructs the application to subscribe to a mirror node and act upon the notifications that result from operations above.

When a notification is received

* it is parsed
* the signature is verified
* operations that can only be performed by the owner are rejected if not initiated by the correct address
* inputs are checked against state (e.g. does an address have sufficient balance for a transfer)
* if all successful, local state is updated 

## Build

The project is built using maven

Linux/MacOs

```shell script
./mvnw clean install
```

Windows

```shell script
mvnw clean install
```

## Configuration

* copy `.env.sample` to `.env`
* edit `.env` and set the `OPERATOR_ID` and `OPERATOR_KEY`

## Run

### Test run

Once built, you can try it out with the following commands which will create the token, mint it and then query it.

```shell script
# construct the token
java -jar hcs-token-example-1.0-run.jar construct TestToken TTT 8
# mint the token
java -jar hcs-token-example-1.0-run.jar mint 1000
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query information about the token
java -jar hcs-token-example-1.0-run.jar name
java -jar hcs-token-example-1.0-run.jar symbol
java -jar hcs-token-example-1.0-run.jar decimals
java -jar hcs-token-example-1.0-run.jar totalSupply
```

** Note: Local state is stored in a file called `{your operator id}.json`, to reset the environment, simply delete the file **

## Individual commands

### Refresh 

Any operation that affects state is only applied as a result of a mirror notification, no local state updates are performed unless they are motivated by a mirror notification.
This ensures the consistency of state across all instances of the application. Indeed, if a HCS transaction failed for some reason and local state had been updated previously, there would be a discrepancy in state between application instances.

This update should be run before any queries to ensure the local application state is up to date.

```shell script
java -jar hcs-token-example-1.0-run.jar refresh
```

_Note: A more complete implementation would run this in the background as a thread for example to ensure local state is updated as promptly as possible_

_Note: Allow a few seconds after `construct` to run `refresh` to allow the new `TopicId` to be propagated to mirror nodes._

### Construct

This constructs a HCS transaction to construct the token with a `name`, `symbol` and `decimals`.
It will automatically create a new `HCS TopicId` and return it to the console, you can communicate this topic Id to others so they can join your App Net.
This will also be stored in `{your operator id}.json` so that it is remembered by the application.

_Note: If you include a space in the token name, be sure to surround the name in quotes (e.g. `"my token"`)_

```shell script
java -jar hcs-token-example-1.0-run.jar construct TestToken TTT 8
```

### Join

This sets up an App Net instance to join a particular Token by informing it of the `Topic Id` to use and also sends a HCS transaction to inform other App Net participants of the new user's address, it should be run by anyone wanting to take part in the token.

_Note: `Construct` automatically adds the operator's address to the address book and sets it to be the owner of the token._

```shell script
java -jar hcs-token-example-1.0-run.jar join topicId (e.g. 0.0.1234)
```

_Note: This step could be optional, but ensures consistency of states across all App Net instances and enables verification of a recipient address in the event of a transfer._

### GenKey

This can be used to generate a random key pair so that you can use the public key to test transfers with.

### Mint

This constructs a HCS transaction to mint the token.
When the notification is received (`refresh`), the token's `totalSupply` will be set to the amount specified and the `balance` of the public address derived from the `OPERATOR_ID` in `.env` will be set to the same value.

```shell script
java -jar hcs-token-example-1.0-run.jar mint 1000
```

### Get token name

This returns the name of the token from local state. If this returns empty, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar name
```

### Get token name

This returns the `name` of the token from local state. If this returns empty, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar name
```

### Get token symbol

This returns the `symbol` of the token from local state. If this returns empty, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar symbol
```

### Get token decimals

This returns the `decimals` of the token from local state. If this returns empty, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar decimals
```

### Get token totalSupply

This returns the `totalSupply` of the token from local state. If this returns empty, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar totalSupply
```

### Get address balance

This returns the `balance` of the specified address from local state. If this returns a value you weren't expecting, you may need to run `refresh`.
 
```shell script
java -jar hcs-token-example-1.0-run.jar balanceOf input_your_public_key_here
```

### Transfer 

This constructs a HCS transaction to transfer tokens from one address to another.
When the notification is received (`refresh`), both addresses' balances are updated.

_Note: The `from` address is set to the public key derived from the `OPERATOR_KEY` in the `.env` file._

```shell script
java -jar hcs-token-example-1.0-run.jar transfer 302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232 20
```

### Approve 

This constructs a HCS transaction to approve another address as a spender up to a given amount.
When the notification is received (`refresh`), the spender is added to the list of allowances.

_Note: The `from` address is set to the public key derived from the `OPERATOR_KEY` in the `.env` file._

```shell script
java -jar hcs-token-example-1.0-run.jar approve 302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232 20
```

### Allowance 

This queries local state and returns the current allowance for a given pair of addresses

```shell script
java -jar hcs-token-example-1.0-run.jar allowance 302a300506032b65700321006e42135c6c7c9162a5f96f6d693677742fd0b3f160e1168cc28f2dadaa9e79cc 302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232
```

### Increase Allowance

This constructs a HCS transaction to increase the allowance for a given address.
When the notification is received (`refresh`), the allowance for the `spender` address is increased accordingly.

```shell script
java -jar hcs-token-example-1.0-run.jar increaseAllowance 302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232 20
```

### Decrease Allowance

This constructs a HCS transaction to decrease the allowance for a given address.
When the notification is received (`refresh`), the allowance for the `spender` address is decreased accordingly.

```shell script
java -jar hcs-token-example-1.0-run.jar decreaseAllowance 302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232 20
```

## Acting as another user

If you would like to pretend to be another user (or node) of the App Net, you will need to:

* Create a Hedera Account Id with sufficient funds to run HCS transactions
* Update your `.env` file with the new account details

then 

```shell script
java -jar hcs-token-example-1.0-run.jar join topicId (e.g. 0.0.1234)
```

followed by 

```shell script
java -jar hcs-token-example-1.0-run.jar refresh
```

to update your local state from notifications

From then on, any operations you perform will be performed with this user's address.

## Local state

As mentioned above, local state is only updated as a result of a mirror notification. You can try this for yourself by sending a `Construct` command and checking the contents of the `{operator id}.json` file.

At this stage, the file should only contain the created `Topic Id`.

Then, run a `mint` command and check the state file again, no changes.

Finally, run a `refresh` command to see the state file updated as a result of mirror notifications.
