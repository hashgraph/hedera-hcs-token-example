package com.hedera.hcstoken.state;

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
