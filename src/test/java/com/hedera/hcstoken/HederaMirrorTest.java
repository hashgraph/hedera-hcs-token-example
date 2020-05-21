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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcstoken.state.Token;
import org.junit.jupiter.api.*;
import proto.*;

import java.time.Instant;

public class HederaMirrorTest extends AbstractTestData {
    @Test
    public void testDuplicate() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        Primitive primitive = transactions.transfer(token, this.pubKeyOther, this.transferAmount);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());
        // duplicate
        // create new consensus Timestamp
        Instant newConsensusTimeStamp = this.consensusTimestamp.plusSeconds(20);
        try {
            HederaMirror.processNotification(token, primitive.toByteArray(), newConsensusTimeStamp);
            Assertions.fail("Should have detected duplicate operation");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Duplicate Operation hash detected"));
            // no change to balance
            Assertions.assertEquals(this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());
            // consensus timestamp updated
            Assertions.assertEquals(newConsensusTimeStamp.getEpochSecond(), token.getLastConsensusSeconds());
            Assertions.assertEquals(newConsensusTimeStamp.getNano(), token.getLastConsensusNanos());
        }
    }

    @Test
    public void testInvalidMessage() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Assertions.assertEquals(0, token.getLastConsensusSeconds());
        Assertions.assertEquals(0, token.getLastConsensusNanos());

        HederaMirror.processNotification(token, "Invalid message".getBytes("UTF-8"), this.consensusTimestamp);
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testConstruct() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        Primitive primitive = transactions.construct(token, this.name, this.symbol, this.decimals);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.name, token.getName());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testMint() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);

        Primitive primitive = transactions.mint(token, this.quantity);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertNotEquals(0, token.getTotalSupply());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testTransfer() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        Primitive primitive = transactions.transfer(token, this.pubKeyOther, this.transferAmount);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testJoin() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        Primitive primitive = transactions.join(token, this.pubKeyOther);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(2, token.getAddresses().size());
        Assertions.assertEquals(0, token.getAddress(this.pubKeyOther).getBalance());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testApprove() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);

        Primitive primitive = transactions.approve(token, this.pubKeyOther, this.approveAmount);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(1, token.getAddress(this.pubKeyOwner).getAllowances().size());
        Assertions.assertEquals(this.approveAmount, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testIncreaseAllowance() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);

        Primitive primitive = transactions.increaseAllowance(token, this.pubKeyOther, this.allowance);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.approveAmount + this.allowance, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }

    @Test
    public void testDecreaseAllowance() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);

        Primitive primitive = transactions.decreaseAllowance(token, this.pubKeyOther, this.allowance);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.approveAmount - this.allowance, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }
    @Test
    public void testTransferFrom() throws Exception {
        Transactions transactions = new Transactions();
        transactions.setTestData(this.topicId, this.operatorId, this.operatorKey, this.randomLong);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);

        // switch user
        transactions.setTestData(this.topicId, this.operatorId, this.operatorKeyOther, this.randomLong);
        Primitive primitive = transactions.transferFrom(token, this.pubKeyOwner, this.pubKeyOther, this.transferAmount);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(this.approveAmount - this.transferAmount, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
        Assertions.assertEquals(this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }
    @Test
    public void testBurn() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        long currentBalance = token.getAddress(this.pubKeyOwner).getBalance();

        Primitive primitive = transactions.burn(token, this.burnAmount);
        HederaMirror.processNotification(token, primitive.toByteArray(), this.consensusTimestamp);
        Assertions.assertEquals(currentBalance - this.burnAmount, token.getAddress(this.pubKeyOwner).getBalance());
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());
    }
}
