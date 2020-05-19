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

import java.time.Instant;

/**
 * Subscribes to a mirror node and handles notifications
 */
public final class HederaMirror {
    private static final String MIRROR_NODE_ADDRESS = Dotenv.configure().ignoreIfMissing().load().get("MIRROR_NODE_ADDRESS");
    private static final MirrorClient mirrorClient = new MirrorClient(MIRROR_NODE_ADDRESS);

    /**
     * Subscribes to a mirror node and sleeps for a few seconds to allow notifications to come through
     * The subscription starts from 1 nano second after the last consensus message we received from the mirror node
     * @param token: The token object
     * @param seconds: The number of seconds to sleep after subscribing
     * @throws Exception: in the event of an error
     */
    public static void subscribe(Token token, long seconds) throws Exception {
        if (token.getTopicId().isEmpty()) {
            return;
        }
        Instant startTime = Instant.ofEpochSecond(token.getLastConsensusSeconds(), token.getLastConsensusNanos() + 1);
        new MirrorConsensusTopicQuery()
                .setTopicId(ConsensusTopicId.fromString(token.getTopicId()))
                .setStartTime(startTime)
                .subscribe(mirrorClient, resp -> handleNotification(token, resp),
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
    private static void handleNotification(Token token, MirrorConsensusTopicResponse notification) {
        try {
            Primitive primitive = Primitive.parseFrom(notification.message);
            byte[] signature = primitive.getSignature().toByteArray();
            String address = primitive.getPublicKey();
            Ed25519PublicKey signingKey = Ed25519PublicKey.fromString(address);

            // set last consensus time stamp
            token.setLastConsensusSeconds(notification.consensusTimestamp.getEpochSecond());
            token.setLastConsensusNanos(notification.consensusTimestamp.getNano());

            // Process response
            if (primitive.hasConstruct()) {
                Construct construct = primitive.getConstruct();
                if ( ! Ed25519.verify(signature, 0, signingKey.toBytes(), 0, construct.toByteArray(), 0, construct.toByteArray().length)) {
                    System.out.println("Signature verification on message failed");
                    return;
                }
                Primitives.construct(token, address, construct.getName(), construct.getSymbol(), construct.getDecimals());
            } else if (primitive.hasMint()) {
                Mint mint = primitive.getMint();
                if ( ! Ed25519.verify(signature, 0, signingKey.toBytes(), 0, mint.toByteArray(), 0, mint.toByteArray().length)) {
                    System.out.println("Signature verification on message failed");
                    return;
                }
                Primitives.mint(token, mint.getAddress(), mint.getQuantity());
            } else if (primitive.hasTransfer()) {
                Transfer transfer = primitive.getTransfer();
                if ( ! Ed25519.verify(signature, 0, signingKey.toBytes(), 0, transfer.toByteArray(), 0, transfer.toByteArray().length)) {
                    System.out.println("Signature verification on message failed");
                    return;
                }
                Primitives.transfer(token, transfer.getFromAddress(), transfer.getToAddress(), transfer.getQuantity());
            } else if (primitive.hasJoin()) {
                Join join = primitive.getJoin();
                if ( ! Ed25519.verify(signature, 0, signingKey.toBytes(), 0, join.toByteArray(), 0, join.toByteArray().length)) {
                    System.out.println("Signature verification on message failed");
                    return;
                }
                Primitives.join(token, join.getAddress());
            } else {
                System.out.println("Unable to process mirror notification - unknown primitive");
            }
        } catch (Exception e) {
            System.out.println("Unable to process mirror notification - unknown message type");
            e.printStackTrace();
        }

    }
}
