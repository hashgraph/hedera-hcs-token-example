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
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hcstoken.state.Token;
import io.github.cdimascio.dotenv.Dotenv;
import proto.*;

/**
 * This class is responsible for constructing and sending messages to HCS
 * It is invoked by the HCSErc20 class following command line inputs
 */
public final class Transactions {
    private static AccountId OPERATOR_ID;
    private static Ed25519PrivateKey OPERATOR_KEY;
    private static final Client client = Client.forTestnet();

    private static boolean testing = false;
    private static String testTopicId = "";

    /** sets up data for unit testing
     *
     * @param topicId:      The topic Id to use for test purposes
     * @param operatorId:   The operator Id to use for test purposes
     * @param operatorKey:  The operator Key to use for test purposes
     */
    public static void setTestData(String topicId, String operatorId, Ed25519PrivateKey operatorKey) {
        testing = true;
        testTopicId = topicId;
        OPERATOR_ID = AccountId.fromString(operatorId);
        OPERATOR_KEY = operatorKey;
    }
    /**
     * Constructs a token (similar to an ERC20 token construct function)
     *
     * @param token:    The token object
     * @param name:     The name of the token
     * @param symbol:   The symbol for the token
     * @param decimals: The number of decimals for this token
     * @throws Exception
     */
    public static Primitive construct(Token token, String name, String symbol, int decimals) throws Exception {

        if (token.getTopicId().isEmpty()) {
            setupSDKClient();
            if (testing) {
                token.setTopicId(testTopicId);
            } else {
                TransactionId transactionId = new ConsensusTopicCreateTransaction()
                        .execute(client);

                final ConsensusTopicId topicId = transactionId.getReceipt(client).getConsensusTopicId();
                System.out.println("New topic created: " + topicId);

                // create a new topic Id
                token.setTopicId(topicId.toString());
            }

            Construct construct = Construct.newBuilder()
                    .setName(name)
                    .setSymbol(symbol)
                    .setDecimals(decimals)
                    .build();

            byte[] signature = OPERATOR_KEY.sign(construct.toByteArray());

            Primitive primitive = Primitive.newBuilder()
                    .setConstruct(construct)
                    .setSignature(ByteString.copyFrom(signature))
                    .setPublicKey(OPERATOR_KEY.publicKey.toString())
                    .build();
            HCSSend(token, primitive);
            return primitive;
        } else {
            String error = "Topic ID is already set, you cannot overwrite";
            System.out.println(error);
            throw new Exception(error);
        }
    }

    /**
     * Adds an address to the App Net
     *
     * @param token:    The token object
     * @param address:  The address to add
     * @throws Exception
     */
    public static Primitive join(Token token, String address) throws Exception {
        if (address.isEmpty()) {
            String error = "Cannot join with an empty address";
            System.out.println(error);
            throw new Exception(error);
        }
        setupSDKClient();
        Join join = Join.newBuilder()
                .setAddress(address)
                .build();

        byte[] signature = OPERATOR_KEY.sign(join.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setJoin(join)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();

        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Mints a token (similar to an ERC20 token mint function)
     *
     * @param token:    The token object
     * @param quantity: The amount to mint
     * @throws Exception
     */
    public static Primitive mint(Token token, long quantity) throws Exception {
        if (token.getTotalSupply() == 0) {
            setupSDKClient();
            Mint mint = Mint.newBuilder()
                    .setAddress(OPERATOR_KEY.publicKey.toString())
                    .setQuantity(quantity)
                    .build();

            byte[] signature = OPERATOR_KEY.sign(mint.toByteArray());

            Primitive primitive = Primitive.newBuilder()
                    .setMint(mint)
                    .setSignature(ByteString.copyFrom(signature))
                    .setPublicKey(OPERATOR_KEY.publicKey.toString())
                    .build();

            HCSSend(token, primitive);
            return primitive;
        } else {
            String error = "Token already minted";
            System.out.println(error);
            throw new Exception(error);
        }
    }

    /**
     * Transfers tokens (similar to an ERC20 token transfer function)
     *
     * @param token:    The token object
     * @param address:  The address to send to
     * @param quantity: The quantity to transfer
     * @throws Exception
     */
    public static Primitive transfer(Token token, String address, long quantity) throws Exception {
        setupSDKClient();
        Transfer transfer = Transfer.newBuilder()
                .setToAddress(address)
                .setQuantity(quantity)
                .build();

        byte[] signature = OPERATOR_KEY.sign(transfer.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setTransfer(transfer)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Approves spend from another address
     *
     * @param token:    The token object
     * @param spender:  The address to approve
     * @param amount: The amount to approve
     * @throws Exception
     */
    public static Primitive approve(Token token, String spender, long amount) throws Exception {
        setupSDKClient();
        Approve approve = Approve.newBuilder()
                .setSpender(spender)
                .setAmount(amount)
                .build();

        byte[] signature = OPERATOR_KEY.sign(approve.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setApprove(approve)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Increase allowance for an address
     *
     * @param token:    The token object
     * @param spender:  The address to approve
     * @param addedValue: The amount to add to the allowance
     * @throws Exception
     */
    public static Primitive increaseAllowance(Token token, String spender, long addedValue) throws Exception {
        setupSDKClient();
        IncreaseAllowance increaseAllowance = IncreaseAllowance.newBuilder()
                .setSpender(spender)
                .setAddedValue(addedValue)
                .build();

        byte[] signature = OPERATOR_KEY.sign(increaseAllowance.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setIncreaseAllowance(increaseAllowance)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Decrease allowance for an address
     *
     * @param token:    The token object
     * @param spender:  The address to approve
     * @param substractedValue: The amount to add to the allowance
     * @throws Exception
     */
    public static Primitive decreaseAllowance(Token token, String spender, long substractedValue) throws Exception {
        setupSDKClient();
        DecreaseAllowance decreaseAllowance = DecreaseAllowance.newBuilder()
                .setSpender(spender)
                .setSubtractedValue(substractedValue)
                .build();

        byte[] signature = OPERATOR_KEY.sign(decreaseAllowance.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setDecreaseAllowance(decreaseAllowance)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Transfers from an address by an approved address
     *
     * @param token:    The token object
     * @param fromAddress: The address to withdraw from
     * @param toAddress:  The address to send to
     * @param amount: The amount to send
     * @throws Exception
     */
    public static Primitive transferFrom(Token token, String fromAddress, String toAddress, long amount) throws Exception {
        setupSDKClient();
        TransferFrom transferFrom = TransferFrom.newBuilder()
                .setFromAddress(fromAddress)
                .setToAddress(toAddress)
                .setAmount(amount)
                .build();

        byte[] signature = OPERATOR_KEY.sign(transferFrom.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setTransferFrom(transferFrom)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Burns tokens from an address
     *
     * @param token:    The token object
     * @param amount: The amount to burn
     * @throws Exception
     */
    public static Primitive burn(Token token, long amount) throws Exception {
        setupSDKClient();
        Burn burn = Burn.newBuilder()
                .setAmount(amount)
                .build();

        byte[] signature = OPERATOR_KEY.sign(burn.toByteArray());

        Primitive primitive = Primitive.newBuilder()
                .setBurn(burn)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(OPERATOR_KEY.publicKey.toString())
                .build();
        HCSSend(token, primitive);
        return primitive;
    }

    /**
     * Generic method to send a transaction to HCS
     *
     * @param token:     The token object
     * @param primitive: The primitive (message) to send
     * @throws Exception: in the event of an error
     */
    private static void HCSSend(Token token, Primitive primitive) throws Exception {
        if (testing) {
            return;
        }
        new ConsensusMessageSubmitTransaction()
                .setTopicId(ConsensusTopicId.fromString(token.getTopicId()))
                .setMessage(primitive.toByteArray())
                .execute(client)
                .getReceipt(client);
    }

    /**
     * Sets up the SDK client, accounting for the possiblity we're running unit tests
     */
    private static void setupSDKClient() {
        if ( ! testing ) {
            OPERATOR_ID = AccountId.fromString(Dotenv.configure().ignoreIfMissing().load().get("OPERATOR_ID"));
            OPERATOR_KEY = Ed25519PrivateKey.fromString(Dotenv.configure().ignoreIfMissing().load().get("OPERATOR_KEY"));
        }
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
    }
}
