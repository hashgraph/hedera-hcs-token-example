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

import java.io.File;
import java.time.Instant;

public abstract class AbstractTestData {
    final File stateFile = new File(getClass().getClassLoader().getResource("test.json").getFile());

    final String topicId = "0.0.43342";
    final long totalSupply = 10000000000L;
    final String symbol = "TTT";
    final String name = "TestToken";
    final int decimals = 10;
    final long lastConsensusSeconds = 1589301202;
    final int lastConsensusNanos = 955026000;
    final Instant consensusTimestamp = Instant.ofEpochSecond(this.lastConsensusSeconds).plusNanos(this.lastConsensusNanos);

    final long ownerBalance = 980;

    final long otherBalance = 20;

    final long quantity = 1;
    final long transferAmount = 10;
    final long approveAmount = 2000;
    final long allowance = 20;
    final long burnAmount = 200;

    final String operatorId = "0.0.999";
    final Ed25519PrivateKey operatorKey = Ed25519PrivateKey.generate();
    final String pubKeyOwner = operatorKey.publicKey.toString();

    final Ed25519PrivateKey operatorKeyOther = Ed25519PrivateKey.generate();
    final String pubKeyOther = operatorKeyOther.publicKey.toString();

    final long randomLong = 1234567890;

    public void setTestData(Transactions transactions) {
        transactions.setTestData(this.topicId, this.operatorId, this.operatorKey, this.randomLong);
    }
}
