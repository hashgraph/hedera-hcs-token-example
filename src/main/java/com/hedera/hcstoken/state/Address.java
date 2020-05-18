package com.hedera.hcstoken.state;

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
