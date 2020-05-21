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

import com.hedera.hcstoken.state.Token;
import org.junit.jupiter.api.*;
import proto.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TransactionsTest extends AbstractTestData {

    @Test
    public void testConstruct() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();
        Primitive primitive = transactions.construct(token, name, symbol, decimals);

        Assertions.assertEquals(this.topicId, token.getTopicId());

        Construct construct = Construct.newBuilder()
                .setName(name)
                .setSymbol(symbol)
                .setDecimals(decimals)
                .build();

        Assertions.assertArrayEquals(construct.toByteArray(), primitive.getConstruct().toByteArray());

        byte[] signature = signature(construct.toByteArray());
        checkSigAndKey(signature, primitive);

        // construct again, should fail
        try {
            transactions.construct(token, name, symbol, decimals);
            Assertions.fail("Second construction should have thrown error");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Topic ID is already set, you cannot overwrite"));
        }
    }

    @Test
    public void testJoin() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        // join with empty address
        try {
            transactions.join(token, "");
            Assertions.fail("Join with empty address should have thrown error");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Cannot join with an empty address"));
        }

        Join join = Join.newBuilder()
                .setAddress(this.pubKeyOther)
                .build();

        Primitive primitive = transactions.join(token, this.pubKeyOther);
        Assertions.assertArrayEquals(join.toByteArray(), primitive.getJoin().toByteArray());

        byte[] signature = signature(join.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testMint() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        Mint mint = Mint.newBuilder()
                .setAddress(this.operatorKey.publicKey.toString())
                .setQuantity(this.quantity)
                .build();

        Primitive primitive = transactions.mint(token, this.quantity);
        Assertions.assertArrayEquals(mint.toByteArray(), primitive.getMint().toByteArray());

        byte[] signature = signature(mint.toByteArray());
        checkSigAndKey(signature, primitive);

        // mint again for error
        try {
            token.setTotalSupply(this.totalSupply);
            transactions.mint(token, this.quantity);
            Assertions.fail("Already minted token should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Token already minted"));
        }
    }

    @Test
    public void testTransfer() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        Transfer transfer = Transfer.newBuilder()
                .setToAddress(this.pubKeyOther)
                .setQuantity(this.quantity)
                .build();

        Primitive primitive = transactions.transfer(token, this.pubKeyOther, this.quantity);
        Assertions.assertArrayEquals(transfer.toByteArray(), primitive.getTransfer().toByteArray());

        byte[] signature = signature(transfer.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testApprove() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        Approve approve = Approve.newBuilder()
                .setSpender(this.pubKeyOther)
                .setAmount(this.approveAmount)
                .build();

        Primitive primitive = transactions.approve(token, this.pubKeyOther, this.approveAmount);
        Assertions.assertArrayEquals(approve.toByteArray(), primitive.getApprove().toByteArray());

        byte[] signature = signature(approve.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testIncreaseAllowance() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        IncreaseAllowance increaseAllowance = IncreaseAllowance.newBuilder()
                .setSpender(this.pubKeyOther)
                .setAddedValue(this.allowance)
                .build();

        Primitive primitive = transactions.increaseAllowance(token, this.pubKeyOther, this.allowance);
        Assertions.assertArrayEquals(increaseAllowance.toByteArray(), primitive.getIncreaseAllowance().toByteArray());

        byte[] signature = signature(increaseAllowance.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testDecreaseAllowance() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        DecreaseAllowance decreaseAllowance = DecreaseAllowance.newBuilder()
                .setSpender(this.pubKeyOther)
                .setSubtractedValue(this.allowance)
                .build();

        Primitive primitive = transactions.decreaseAllowance(token, this.pubKeyOther, this.allowance);
        Assertions.assertArrayEquals(decreaseAllowance.toByteArray(), primitive.getDecreaseAllowance().toByteArray());

        byte[] signature = signature(decreaseAllowance.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testTransferFrom() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        TransferFrom transferFrom = TransferFrom.newBuilder()
                .setFromAddress(this.operatorKey.publicKey.toString())
                .setToAddress(this.pubKeyOther)
                .setAmount(this.transferAmount)
                .build();

        Primitive primitive = transactions.transferFrom(token, this.operatorKey.publicKey.toString(), this.pubKeyOther, this.transferAmount);
        Assertions.assertArrayEquals(transferFrom.toByteArray(), primitive.getTransferFrom().toByteArray());

        byte[] signature = signature(transferFrom.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    @Test
    public void testBurn() throws Exception {
        Transactions transactions = new Transactions();
        setTestData(transactions);
        Token token = new Token();

        Burn burn = Burn.newBuilder()
                .setAmount(this.burnAmount)
                .build();


        Primitive primitive = transactions.burn(token, this.burnAmount);
        Assertions.assertArrayEquals(burn.toByteArray(), primitive.getBurn().toByteArray());

        byte[] signature = signature(burn.toByteArray());
        checkSigAndKey(signature, primitive);
    }

    private void checkSigAndKey(byte[] signature, Primitive primitive) {
        Assertions.assertArrayEquals(signature, primitive.getHeader().getSignature().toByteArray());
        Assertions.assertEquals(operatorKey.publicKey.toString(), primitive.getHeader().getPublicKey());
    }

    private byte[] signature(byte[] toSign) throws IOException {
        // get a random long into a byte array
        byte[] randomString = String.valueOf(this.randomLong).getBytes("UTF-8");
        // concatenate random long with data to sign
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( toSign );
        outputStream.write( randomString );
        // sign the result
        return this.operatorKey.sign(outputStream.toByteArray( ));
    }

}
