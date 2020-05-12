package com.hedera.hcstoken.state;

/**
 * An Address represents a token holder with a balance
 * An address also holds a boolean indicating if the address is the owner of the token
 * This could be used to validate operations such as Burn for example
 */
public final class Address {

    private long balance = 0;
    private String publicKey = "";
    private boolean owner = false;

    public Address() {
    }
    public Address(String publicKey) {
        this.publicKey = publicKey;
    }
    public void setBalance(long balance) {
        this.balance = balance;
    }
    public long getBalance() {
        return this.balance;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    public String getPublicKey() {
        return this.publicKey;
    }
    public boolean isOwner() {
        return this.owner;
    }
    public void setOwner(boolean owner) {
        this.owner = owner;
    }
}
