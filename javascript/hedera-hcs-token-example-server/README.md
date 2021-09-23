# Image Authenticity

Javascript back end and front end code to support the Image Authenticity demos

## Backend

This is written in Node.js

### Setup

Copy the `app/backend/.env.sample` file to `app/backend/.env` and edit to setup environment variables.

```
OPERATOR_KEY=302...
OPERATOR_ID=0.0.xxxx
TOPIC_ID=0.0.xxxx

MIRROR_ADDRESS="hcs.testnet.mirrornode.hedera.com:5600"
DATABASE_LOCATION="./database/token.db"
```

### Rebuild protobuf

_This should not be necessary_

mkdir proto
protoc --proto_path=../ --js_out=import_style=commonjs,binary:proto messages.proto

### Install

Install node.js version 13.x if not already installed

```shell script
# Using Ubuntu
curl -sL https://deb.nodesource.com/setup_13.x | sudo -E bash -
sudo apt-get install -y nodejs

# Using Debian, as root
curl -sL https://deb.nodesource.com/setup_13.x | bash -
apt-get install -y nodejs
```

Install make and c++ compiler (for protobuf)

```shell script
sudo apt-get update; sudo apt-get install build-essential
```

```shell script
npm install sqlite3 --save

npm install
```

### Setup database

_note: edit `.env` to include token name, etc... if you want to construct the token at initialisation_
```
mkdir database

node scripts/migrate.js
```

this will create a database in `./database` called `token.db`.
To reset the demo environment, delete the database file and run the `migrate.js` script again.

### Run

```
node server.js
```

Should output `App running on localhost:3128` if all is well.
