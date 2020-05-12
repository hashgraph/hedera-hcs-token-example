package com.hedera.hcstoken.state;

/**
 * This class holds the token and its properties
 * Some properties such as totalSupply are ERC20 related
 * Others such as the AddressBook, topicId and last consensus are related to AppNet or instance of the App
 */
public final class Token {
    // ERC20 properties
    private long totalSupply = 0;
    private String symbol = "";
    private String name = "";
    private int decimals = 0;
    // AppNet specifics
    private long lastConsensusSeconds = 0;
    private int lastConsensusNanos = 0;
    private AddressBook addressBook = new AddressBook();
    private String topicId = "";

    public Token() {
    }
    public void setLastConsensusSeconds(long lastConsensusSeconds) {
        this.lastConsensusSeconds = lastConsensusSeconds;
    }
    public long getLastConsensusSeconds() {
        return this.lastConsensusSeconds;
    }
    public void setLastConsensusNanos(int lastConsensusNanos) {
        this.lastConsensusNanos = lastConsensusNanos;
    }
    public int getLastConsensusNanos() {
        return this.lastConsensusNanos;
    }
    public void setAddressBook(AddressBook addressBook) {
        this.addressBook = addressBook;
    }
    public AddressBook getAddressBook() {
        return this.addressBook;
    }
    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
    public String getTopicId() {
        return this.topicId;
    }
    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }
    public long getTotalSupply() {
        return this.totalSupply;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getSymbol() {
        return this.symbol;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }
    public int getDecimals() {
        return this.decimals;
    }
}
