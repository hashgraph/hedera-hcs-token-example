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

import com.google.protobuf.ByteString;
import com.hedera.hcstoken.state.Token;
import org.junit.jupiter.api.*;
import proto.*;

public class TransactionsTest extends AbstractTestData {

    @Test
    public void testConstruct() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();
        Primitive primitive = Transactions.construct(token, name, symbol, decimals);

        Assertions.assertEquals(this.topicId, token.getTopicId());

        Construct construct = Construct.newBuilder()
                .setName(name)
                .setSymbol(symbol)
                .setDecimals(decimals)
                .build();

        byte[] signature = operatorKey.sign(construct.toByteArray());

        Assertions.assertArrayEquals(construct.toByteArray(), primitive.getConstruct().toByteArray());
        checkSigAndKey(signature, primitive);

        // construct again, should fail
        try {
            Transactions.construct(token, name, symbol, decimals);
            Assertions.fail("Second construction should have thrown error");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Topic ID is already set, you cannot overwrite"));
        }
    }

    @Test
    public void testJoin() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();

        // join with empty address
        try {
            Transactions.join(token, "");
            Assertions.fail("Join with empty address should have thrown error");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Cannot join with an empty address"));
        }

        Join join = Join.newBuilder()
                .setAddress(this.pubKeyOther)
                .build();

        byte[] signature = operatorKey.sign(join.toByteArray());

        Primitive primitive = Transactions.join(token, this.pubKeyOther);

        Assertions.assertArrayEquals(join.toByteArray(), primitive.getJoin().toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testMint() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();

        Mint mint = Mint.newBuilder()
                .setAddress(this.operatorKey.publicKey.toString())
                .setQuantity(this.quantity)
                .build();

        byte[] signature = operatorKey.sign(mint.toByteArray());

        Primitive primitive = Transactions.mint(token, this.quantity);

        Assertions.assertArrayEquals(mint.toByteArray(), primitive.getMint().toByteArray());
        checkSigAndKey(signature, primitive);

        // mint again for error
        try {
            token.setTotalSupply(this.totalSupply);
            Transactions.mint(token, this.quantity);
            Assertions.fail("Already minted token should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Token already minted"));
        }
    }

    @Test
    public void testTransfer() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();

        Transfer transfer = Transfer.newBuilder()
                .setToAddress(this.pubKeyOther)
                .setFromAddress(this.operatorKey.publicKey.toString())
                .setQuantity(this.quantity)
                .build();

        byte[] signature = this.operatorKey.sign(transfer.toByteArray());

        Primitive primitive = Transactions.transfer(token, this.pubKeyOther, this.quantity);

        Assertions.assertArrayEquals(transfer.toByteArray(), primitive.getTransfer().toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testApprove() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();

        Approve approve = Approve.newBuilder()
                .setFromAddress(this.operatorKey.publicKey.toString())
                .setSpender(this.pubKeyOther)
                .setAmount(this.approveAmount)
                .build();

        byte[] signature = this.operatorKey.sign(approve.toByteArray());

        Primitive primitive = Transactions.approve(token, this.pubKeyOther, this.approveAmount);

        Assertions.assertArrayEquals(approve.toByteArray(), primitive.getApprove().toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testIncreaseAllowance() throws Exception {
        Transactions.setTestData(this.topicId, this.operatorId, this.operatorKey);
        Token token = new Token();

        IncreaseAllowance increaseAllowance = IncreaseAllowance.newBuilder()
                .setFromAddress(this.operatorKey.publicKey.toString())
                .setSpender(this.pubKeyOther)
                .setAddedValue(this.allowance)
                .build();

        byte[] signature = this.operatorKey.sign(increaseAllowance.toByteArray());

        Primitive primitive = Transactions.increaseAllowance(token, this.pubKeyOther, this.allowance);

        Assertions.assertArrayEquals(increaseAllowance.toByteArray(), primitive.getIncreaseAllowance().toByteArray());
        checkSigAndKey(signature, primitive);
    }

    private void checkSigAndKey(byte[] signature, Primitive primitive) {
        Assertions.assertArrayEquals(signature, primitive.getSignature().toByteArray());
        Assertions.assertEquals(operatorKey.publicKey.toString(), primitive.getPublicKey());
    }
}
