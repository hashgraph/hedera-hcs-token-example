# hedera-hcs-token-example-ui

> A token example using Hedera HCS

## Build Setup

``` bash
# install dependencies
npm install ajv@^6.9.1 --save
npm install webpack --save

npm install
```

### Rebuild protobuf

_This should not be necessary_

``` bash
mkdir src/proto
protoc --proto_path=../ --js_out=import_style=commonjs,binary:src/proto messages.proto
```

``` bash
# serve with hot reload at localhost:8080
npm run dev

# build for production with minification
npm run build

# build for production and view the bundle analyzer report
npm run build --report

# run unit tests
npm run unit

# run all tests
npm test
```

For detailed explanation on how things work, checkout the [guide](http://vuejs-templates.github.io/webpack/) and [docs for vue-loader](http://vuejs.github.io/vue-loader).

## Cookies

this example uses cookies to store user data, the cookies created are valid for 365 days

- tokenName: the name of the token
- tokenSymbol: the symbol for the token
- tokenDecimals: the decimals for the token
- userName: the username of the registered user
- friendlyName: the forename + surname of the registered user
- userKey: the private key of the registered user (We don't recommend this as best practice !)

A menu option to clear cookies is available
