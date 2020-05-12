package com.hedera.hcstoken.state;

import java.util.HashMap;
import java.util.Map;

/**
 * The address book holds the collection of token holders
 */
public final class AddressBook {
    private Map<String, Address> addresses = new HashMap<String, Address>();

    public AddressBook() {
    }

    public void setAddresses(Map<String, Address> addresses) {
        this.addresses = addresses;
    }
    public Map<String, Address> getAddresses() {
        return this.addresses;
    }
    public Address addAddress(String publicKey) {
        Address address = new Address(publicKey);
        this.addresses.put(publicKey, address);
        return address;
    }
    public Address getAddress(String publicKey) {
        try {
            return this.addresses.get(publicKey);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
