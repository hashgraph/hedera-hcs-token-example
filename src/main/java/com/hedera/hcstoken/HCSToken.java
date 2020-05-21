package com.hedera.hcstoken;

/*-
 * ‌
 * hcs-token-example
 * ​
 * Copyright (C) 2020 Hedera Hashgraph, LLC
 * ​
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ‍
 */

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcstoken.state.Address;
import com.hedera.hcstoken.state.Token;

import static java.lang.Integer.*;

/**
 * Entry point the the example
 * This takes comand line inputs
 */
public final class HCSToken
{
    public static void main( String[] args ) throws Exception {

        // create or load state
        Token token = Persistence.loadToken();

        if (args.length == 0) {
            System.out.println("Missing command line arguments, valid commands are (note, not case sensitive)");
            // operations
            System.out.println("  construct {name} {symbol} {decimals}");
            System.out.println("  transfer {address} {quantity}");
            System.out.println("  mint {quantity}");
            System.out.println("  approve {spender} {amount}");
            System.out.println("  increaseAllowance {spender} {addedValue}");
            System.out.println("  decreaseAllowance {spender} {addedValue}");
            System.out.println("  transferFrom {fromAddress} {toAddress} {amount}");
            System.out.println("  burn {amount}");
            // queries
            System.out.println("  balanceOf {address}");
            System.out.println("  totalSupply");
            System.out.println("  name");
            System.out.println("  decimals");
            System.out.println("  symbol");
            System.out.println("  allowance {owner} {spender}");
            System.out.println();
            System.out.println("  join {topicId}");
            System.out.println("  genkey - generates a key pair");
            System.out.println("  refresh - pulls updates from mirror node");

            System.out.println("Exiting");
            return;
        }

        System.out.print("--> ");
        for (String argument : args) {
            System.out.print(String.format(" %s", argument));
        }
        System.out.println();
        Transactions transactions = new Transactions();

        switch (args[0].toUpperCase()) {
            // primitives
            case "CONSTRUCT":
                // construct {name} {symbol} {decimals}
                transactions.construct(token, args[1], args[2], parseInt(args[3]));
                break;
            case "MINT":
                // mint {quantity}
                transactions.mint(token, Long.parseLong(args[1]));
                break;
            case "TRANSFER":
                // transfer {address} {quantity}
                transactions.transfer(token, args[1], Long.parseLong(args[2]));
                break;
            case "APPROVE":
                // approve {spender} {amount}
                transactions.approve(token, args[1], Long.parseLong(args[2]));
                break;
            case "INCREASEALLOWANCE":
                // increaseAllowance {spender} {addedValue}
                transactions.increaseAllowance(token, args[1], Long.parseLong(args[2]));
                break;
            case "DECREASEALLOWANCE":
                // increaseAllowance {spender} {addedValue}
                transactions.decreaseAllowance(token, args[1], Long.parseLong(args[2]));
                break;
            case "TRANSFERFROM":
                // transferFrom {fromAddress} {toAddress} {amount}
                transactions.transferFrom(token, args[1], args[2], Long.parseLong(args[3]));
                break;
            case "BURN":
                // burn {amount}
                transactions.burn(token, Long.parseLong(args[1]));
                break;
            case "JOIN":
                // join {topicId}
                if (token.getTopicId().isEmpty()) {
                    token.setTopicId(args[1]);
                } else if (token.getTopicId().equals(args[1])) {
                    // already subscribed to another topic id
                    System.out.println("Already subscribed to another topic id " + token.getTopicId());
                }
                break;
            // getters don't need a HCS round trip
            case "BALANCEOF":
                // balanceOf {address}
                Address address = token.getAddress(args[1]);
                if (address == null) {
                    System.out.println("BalanceOf " + args[1] + " : 0");
                } else {
                    System.out.println("BalanceOf " + args[1] + " : " + address.getBalance());
                }
                break;
            case "TOTALSUPPLY":
                // totalSupply
                System.out.println("Token Supply : " + token.getTotalSupply());
                break;
            case "NAME":
                // name
                System.out.println("Token Name : " + token.getName());
                break;
            case "DECIMALS":
                // decimals
                System.out.println("Token Decimals : " + token.getDecimals());
                break;
            case "SYMBOL":
                // symbol
                System.out.println("Token Symbol : " + token.getSymbol());
                break;
            case "ALLOWANCE":
                // allowance {owner} {spender}
                Address owner = token.getAddress(args[1]);
                if (owner == null) {
                    System.out.println("Invalid owner address.");
                } else {
                    System.out.println("Allowance is: " + owner.getAllowance(args[2]));
                }
                break;
            // utilities
            case "REFRESH":
                HederaMirror.subscribe(token, 10);
                break;
            case "GENKEY":
                // generates and outputs a new key pair
                Ed25519PrivateKey privateKey = Ed25519PrivateKey.generate();
                System.out.println("Private Key: " + privateKey.toString());
                System.out.println("Public  Key: " + privateKey.publicKey.toString());
                break;
        }
        // save state
        Persistence.saveToken(token);
    }
}
