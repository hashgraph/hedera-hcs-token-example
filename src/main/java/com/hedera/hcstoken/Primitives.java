package com.hedera.hcstoken;

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

            Address address = token.getAddressBook().addAddress(ownerAddress);
            address.setOwner(true);
        } else {
            System.out.println("Construct - Token already constructed");
        }
    }
    /**
     * Mints the token, sets the total supply and sets the balance of the token's owner
     * @param token: the token object
     * @param address: the address of the token's owner
     * @param quantity: the quantity to mint
     */
    public static void mint(Token token, String address, long quantity) {
        System.out.println(String.format("Processing mirror notification - mint %s %d", address, quantity));
        // check the initiator is the owner
        if ( ! isOwner(token, address) )  {
            System.out.println("Address is not token owner's address");
        } else {
            if (token.getTotalSupply() == 0) {
                token.setTotalSupply(quantity);
                Address ownerAddress = token.getAddressBook().getAddress(address);
                ownerAddress.setBalance(quantity);
            } else {
                System.out.println("Mint - Token already minted");
            }
        }
    }
    /**
     * Transfers tokens between two addresses
     * @param token: the token object
     * @param fromAddress: the address to transfer to
     * @param toAddress: the address to transfer to
     * @param quantity: the quantity to transfer
     */
    public static void transfer(Token token, String fromAddress, String toAddress, long quantity) {
        final Address senderAddress = token.getAddressBook().getAddress(fromAddress);
        Address recipientAddress = token.getAddressBook().getAddress(toAddress);

        System.out.println(String.format("Processing mirror notification - transfer from (%s) to (%s) %d", fromAddress, toAddress, quantity));

        if (!isKnownAddress(token, fromAddress)) {
            System.out.println("Transfer - Unknown address, please join first");
        } else if (senderAddress == null) {
            System.out.println("Transfer - Sender address is empty");
        } else {
            if (recipientAddress == null) {
                // If toAddress is unknown, add it
                // TODO: Only for demo
                // Note: This is for demo purposes, it is dangerous to do this without knowing
                // if the address is valid, indeed funds could be locked into this account
                // indefinitely if the corresponding private key is not known.
                recipientAddress = token.getAddressBook().addAddress(toAddress);
            }
            // check sender balance
            if (senderAddress.getBalance() >= quantity) {
                // we have sufficient balance
                senderAddress.setBalance(senderAddress.getBalance() - quantity);
                recipientAddress.setBalance(recipientAddress.getBalance() + quantity);
            } else {
                System.out.println("Transfer - Insufficient balance");
            }
        }
    }
    /**
     * Adds an address to the App Net by adding it to the address book
     * @param token: the token object
     * @param address: the address wanting to join the network
     */
    public static void join(Token token, String address) {
        System.out.println(String.format("Processing mirror notification - join %s", address));
        if (token.getAddressBook().getAddress(address) == null) {
            token.getAddressBook().addAddress(address);
        } else {
            System.out.println("Address " + address + " already part of the App Net");
        }
    }

    /**
     * Checks if an address is valid (known to the App Net)
     * @param token: the token object
     * @param address: the address to check
     * @return boolean: true if the address is known
     */
    private static boolean isKnownAddress(Token token, String address) {
        return (token.getAddressBook().getAddress(address) != null);
    }
    /**
     * Checks if an address is valid and the owner of the token
     * @param token: the token object
     * @param address: the address to check
     * @return boolean: true if the address is know and is the owner
     */
    private static boolean isOwner(Token token, String address) {
        return (token.getAddressBook().getAddress(address) != null && token.getAddressBook().getAddress(address).isOwner());
    }
}
