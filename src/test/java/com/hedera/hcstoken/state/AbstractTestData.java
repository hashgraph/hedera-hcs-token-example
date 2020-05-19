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

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public abstract class AbstractTestData {
    final long balance = 10;
    final String firstPublicKey = Ed25519PrivateKey.generate().publicKey.toString();
    final String secondPublicKey = Ed25519PrivateKey.generate().publicKey.toString();
    final long totalSupply = 1000;
    final String symbol = "TTT";
    final String name = "TestToken";
    final int decimals = 8;
    final long lastConsensusSeconds = 1589301202;
    final int lastConsensusNanos = 955026000;
    final String topicId = "0.0.43342";
}
