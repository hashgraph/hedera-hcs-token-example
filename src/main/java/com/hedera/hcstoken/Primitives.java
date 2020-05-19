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

import com.hedera.hcstoken.state.Address;
import com.hedera.hcstoken.state.Token;
/**
 * This class is handling all the state changes following a mirror notification
 */

public final class Primitives {
    /**
     * Constructs the token
     * @param token: the token object
     * @param name: the name of the token
     * @param symbol: the symbol for the token
     * @param decimals: the number of decimals for the token
     * @throws Exception: in the event of an error
     */
    public static void construct(Token token, String ownerAddress, String name, String symbol, int decimals) throws Exception {
        System.out.println(String.format("Processing mirror notification - construct %s %s %s", name, symbol, decimals));
        if (token.getName().isEmpty()) {
            token.setName(name);
            token.setSymbol(symbol);
            token.setDecimals(decimals);

            Address address = token.addAddress(ownerAddress);
            address.setOwner(true);
        } else {
            String error = "Construct - Token already constructed";
            System.out.println(error);
            throw new Exception(error);
        }
    }
    /**
     * Mints the token, sets the total supply and sets the balance of the token's owner
     * @param token: the token object
     * @param address: the address of the token's owner
     * @param quantity: the quantity to mint
     * @throws Exception: in the event of an error
     */
    public static void mint(Token token, String address, long quantity) throws Exception {
        System.out.println(String.format("Processing mirror notification - mint %s %d", address, quantity));
        // check the initiator is the owner
        if ( ! isOwner(token, address) )  {
            String error = "Address is not token owner's address";
            System.out.println(error);
            throw new Exception(error);
        } else {
            if (token.getTotalSupply() == 0) {
                // TODO: Switch to BigInteger for supply and addresses ?
                long tokenSupply = quantity * (10 ^ token.getDecimals());
                token.setTotalSupply(tokenSupply);
                Address ownerAddress = token.getAddress(address);
                ownerAddress.setBalance(tokenSupply);
            } else {
                String error = "Mint - Token already minted";
                System.out.println(error);
                throw new Exception(error);
            }
        }
    }
    /**
     * Transfers tokens between two addresses
     * @param token: the token object
     * @param fromAddress: the address to transfer to
     * @param toAddress: the address to transfer to
     * @param quantity: the quantity to transfer
     * @throws Exception: in the event of an error
     */
    public static void transfer(Token token, String fromAddress, String toAddress, long quantity) throws Exception {
        final Address senderAddress = token.getAddress(fromAddress);
        Address recipientAddress = token.getAddress(toAddress);

        System.out.println(String.format("Processing mirror notification - transfer from (%s) to (%s) %d", fromAddress, toAddress, quantity));

        if (!isKnownAddress(token, fromAddress)) {
            String error = "Transfer - from address unknown";
            System.out.println(error);
            throw new Exception(error);
        } else if ((toAddress == null) || (toAddress.isEmpty())) {
            String error = "Transfer - to address is empty";
            System.out.println(error);
            throw new Exception(error);
        } else {
            if (recipientAddress == null) {
                // If toAddress is unknown, add it
                // TODO: Only for demo
                // Note: This is for demo purposes, it is dangerous to do this without knowing
                // if the address is valid, indeed funds could be locked into this account
                // indefinitely if the corresponding private key is not known.
                recipientAddress = token.addAddress(toAddress);
            }
            // check sender balance
            if (senderAddress.getBalance() >= quantity) {
                // we have sufficient balance
                senderAddress.setBalance(senderAddress.getBalance() - quantity);
                recipientAddress.setBalance(recipientAddress.getBalance() + quantity);
            } else {
                String error = "Transfer - Insufficient balance";
                System.out.println(error);
                throw new Exception(error);
            }
        }
    }
    /**
     * Approve spend from another address
     * @param token: the token object
     * @param from: the address to add allowance to
     * @param spender: the address to allow
     * @param amount: the amount to allow
     * @throws Exception: in the event of an error
     */
    public static void approve(Token token, String from, String spender, long amount) throws Exception {
        System.out.println(String.format("Processing mirror notification - approve (%s) for %d", spender, amount));

        final Address fromAddress = token.getAddress(from);

        if (!isKnownAddress(token, from)) {
            String error = "Approve - from address unknown";
            System.out.println(error);
            throw new Exception(error);
        } else if ((spender == null) || (spender.isEmpty())) {
            String error = "Approve - spender address is empty";
            System.out.println(error);
            throw new Exception(error);
        } else {
            fromAddress.addAllowance(spender, amount);
        }
    }
    /**
     * Increase allowance for an address
     * @param token: the token object
     * @param from: the address to add allowance to
     * @param spender: the address to allow
     * @param addedValue: the amount to add to the allowance
     * @throws Exception: in the event of an error
     */
    public static void increaseAllowance(Token token, String from, String spender, long addedValue) throws Exception {
        System.out.println(String.format("Processing mirror notification - increaseAllowance (%s) for (%s) by %d", from, spender, addedValue));

        final Address fromAddress = token.getAddress(from);

        if (!isKnownAddress(token, from)) {
            String error = "IncreaseAllowance - from address unknown";
            System.out.println(error);
            throw new Exception(error);
        } else if ((spender == null) || (spender.isEmpty())) {
            String error = "IncreaseAllowance - spender address is empty";
            System.out.println(error);
            throw new Exception(error);
        } else {
            if (fromAddress.getAllowances().containsKey(spender)) {
                long allowance = fromAddress.getAllowance(spender) + addedValue;
                fromAddress.getAllowances().put(spender, allowance);
            } else {
                String error = "IncreaseAllowance - spender address is not approved";
                System.out.println(error);
                throw new Exception(error);
            }
        }
    }
    /**
     * Adds an address to the App Net by adding it to the address book
     * @param token: the token object
     * @param address: the address wanting to join the network
     * @throws Exception: in the event of an error
     */
    public static void join(Token token, String address) throws Exception {
        System.out.println(String.format("Processing mirror notification - join %s", address));
        if (token.getAddress(address) == null) {
            token.addAddress(address);
        } else {
            String error = "Address " + address + " already part of the App Net";
            System.out.println(error);
            throw new Exception(error);
        }
    }

    /**
     * Checks if an address is valid (known to the App Net)
     * @param token: the token object
     * @param address: the address to check
     * @return boolean: true if the address is known
     */
    private static boolean isKnownAddress(Token token, String address) {
        return (token.getAddress(address) != null);
    }
    /**
     * Checks if an address is valid and the owner of the token
     * @param token: the token object
     * @param address: the address to check
     * @return boolean: true if the address is know and is the owner
     */
    private static boolean isOwner(Token token, String address) {
        return (token.getAddress(address) != null && token.getAddress(address).isOwner());
    }
}
