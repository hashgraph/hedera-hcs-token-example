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

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.mirror.MirrorClient;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicQuery;
import com.hedera.hashgraph.sdk.mirror.MirrorConsensusTopicResponse;
import com.hedera.hcstoken.state.Token;
import io.github.cdimascio.dotenv.Dotenv;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import proto.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

/**
 * Subscribes to a mirror node and handles notifications
 */
public final class HederaMirror {
    private static final String MIRROR_NODE_ADDRESS = Dotenv.configure().ignoreIfMissing().load().get("MIRROR_NODE_ADDRESS");

    /**
     * Subscribes to a mirror node and sleeps for a few seconds to allow notifications to come through
     * The subscription starts from 1 nano second after the last consensus message we received from the mirror node
     * @param token: The token object
     * @param seconds: The number of seconds to sleep after subscribing
     * @throws Exception: in the event of an error
     */
    public static void subscribe(Token token, long seconds) throws Exception {
        final MirrorClient mirrorClient = new MirrorClient(MIRROR_NODE_ADDRESS);

        if (token.getTopicId().isEmpty()) {
            return;
        }
        Instant startTime = Instant.ofEpochSecond(token.getLastConsensusSeconds(), token.getLastConsensusNanos() + 1);
        new MirrorConsensusTopicQuery()
                .setTopicId(ConsensusTopicId.fromString(token.getTopicId()))
                .setStartTime(startTime)
                .subscribe(mirrorClient, resp -> {
                            try {
                                handleNotification(token, resp);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
            // On gRPC error, print the stack trace
            Throwable::printStackTrace);

        // After this sleep period, the subscription ends
        Thread.sleep(seconds * 1000);
    }

    /**
     * Handles notifications from a mirror node
     * @param token: The token object
     * @param notification: The notification data from mirror node
     */
    private static void handleNotification(Token token, MirrorConsensusTopicResponse notification) throws Exception {
        processNotification(token, notification.message, notification.consensusTimestamp);
    }

    /**
     * Processes notifications from a mirror node
     * Not strictly necessary, all the processing could take place in handleNotification()
     * However it's not possible to instantiate a MirrorConsensusTopicResponse meaning
     * it's not possible to unit test otherwise
     * @param token: The token object
     * @param message: The message within the notification
     * @param consensusTimestamp: The consensus timestamp within the notification
     */
    public static void processNotification(Token token, byte[] message, Instant consensusTimestamp) throws Exception {
        byte[] operationHash = new byte[0];
        try {
            // hash the notification and add to the list of operations
            // this will throw an exception if the operation has already been processed
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            operationHash = digest.digest(message);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw e;
        }

        // set last consensus time stamp
        token.setLastConsensusSeconds(consensusTimestamp.getEpochSecond());
        token.setLastConsensusNanos(consensusTimestamp.getNano());

        try {
            token.addOperation(operationHash);
        } catch (Exception e) {
            if (e.getMessage().equals("Duplicate Operation hash detected")) {
                System.out.println("Duplicate Operation hash detected - skipping");
                return;
            } else {
                throw e;
            }
        }
        Primitive primitive = null;
        try {
            primitive = Primitive.parseFrom(message);
        } catch (Exception e) {
            System.out.println("Unable to process mirror notification - unknown message type");
            e.printStackTrace();
            return;
        }

        byte[] signature = primitive.getHeader().getSignature().toByteArray();
        String address = primitive.getHeader().getPublicKey();
        Ed25519PublicKey signingKey = Ed25519PublicKey.fromString(address);

        try {
            // Process response
            Long random = primitive.getHeader().getRandom();
            if (primitive.hasConstruct()) {
                Construct construct = primitive.getConstruct();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, construct.toByteArray(), random)) {
                    return;
                }
                Primitives.construct(token, address, construct.getName(), construct.getSymbol(), construct.getDecimals());
            } else if (primitive.hasMint()) {
                Mint mint = primitive.getMint();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, mint.toByteArray(), random)) {
                    return;
                }
                Primitives.mint(token, mint.getAddress(), mint.getQuantity());
            } else if (primitive.hasTransfer()) {
                Transfer transfer = primitive.getTransfer();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, transfer.toByteArray(), random)) {
                    return;
                }
                Primitives.transfer(token, primitive.getHeader().getPublicKey(), transfer.getToAddress(), transfer.getQuantity());
            } else if (primitive.hasJoin()) {
                Join join = primitive.getJoin();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, join.toByteArray(), random)) {
                    return;
                }
                Primitives.join(token, join.getAddress());
            } else if (primitive.hasApprove()) {
                Approve approve = primitive.getApprove();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, approve.toByteArray(), random)) {
                    return;
                }
                Primitives.approve(token, primitive.getHeader().getPublicKey(), approve.getSpender(), approve.getAmount());
            } else if (primitive.hasIncreaseAllowance()) {
                IncreaseAllowance increaseAllowance = primitive.getIncreaseAllowance();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, increaseAllowance.toByteArray(), random)) {
                    return;
                }
                Primitives.increaseAllowance(token, primitive.getHeader().getPublicKey(), increaseAllowance.getSpender(), increaseAllowance.getAddedValue());
            } else if (primitive.hasDecreaseAllowance()) {
                DecreaseAllowance decreaseAllowance = primitive.getDecreaseAllowance();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, decreaseAllowance.toByteArray(), random)) {
                    return;
                }
                Primitives.decreaseAllowance(token, primitive.getHeader().getPublicKey(), decreaseAllowance.getSpender(), decreaseAllowance.getSubtractedValue());
            } else if (primitive.hasTransferFrom()) {
                TransferFrom transferFrom = primitive.getTransferFrom();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, transferFrom.toByteArray(), random)) {
                    return;
                }
                Primitives.transferFrom(token, primitive.getHeader().getPublicKey(), transferFrom.getFromAddress(), transferFrom.getToAddress(), transferFrom.getAmount());
            } else if (primitive.hasBurn()) {
                Burn burn = primitive.getBurn();
                if ( ! signatureValid(primitive.getHeader().getSignature().toByteArray(), signingKey, burn.toByteArray(), random)) {
                    return;
                }
                Primitives.burn(token, primitive.getHeader().getPublicKey(), burn.getAmount());
            } else {
                System.out.println("Unable to process mirror notification - unknown primitive");
            }
        } catch (Exception e) {
            System.out.println("An error occurred : " + e.getMessage());
        }
    }

    private static boolean signatureValid(byte[] signature, Ed25519PublicKey publicKey, byte[] toVerify, long random) throws IOException {
        byte[] randomString = String.valueOf(random).getBytes("UTF-8");
        // concatenate random long with data to sign
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(toVerify);
        outputStream.write(randomString);
        // verify the result
        if ( ! Ed25519.verify(signature, 0, publicKey.toBytes(), 0, outputStream.toByteArray(), 0, outputStream.toByteArray().length)) {
            System.out.println("Signature verification on message failed");
            return false;
        } else {
            return true;
        }
    }
}
