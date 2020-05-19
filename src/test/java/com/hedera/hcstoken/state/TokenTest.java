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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TokenTest extends AbstractTestData {

    @Test
    public void testToken() {

        Token token = new Token();
        Assertions.assertEquals(0, token.getTotalSupply());
        Assertions.assertEquals("", token.getSymbol());
        Assertions.assertEquals("", token.getName());
        Assertions.assertEquals(0, token.getDecimals());
        Assertions.assertEquals(0, token.getLastConsensusSeconds());
        Assertions.assertEquals(0, token.getLastConsensusNanos());
        Assertions.assertEquals("", token.getTopicId());
        Assertions.assertEquals(0, token.getAddresses().size());

        token.setTotalSupply(this.totalSupply);
        Assertions.assertEquals(this.totalSupply, token.getTotalSupply());

        token.setSymbol(this.symbol);
        Assertions.assertEquals(this.symbol, token.getSymbol());

        token.setName(this.name);
        Assertions.assertEquals(this.name, token.getName());

        token.setDecimals(this.decimals);
        Assertions.assertEquals(this.decimals, token.getDecimals());

        token.setLastConsensusSeconds(this.lastConsensusSeconds);
        Assertions.assertEquals(this.lastConsensusSeconds, token.getLastConsensusSeconds());

        token.setLastConsensusNanos(this.lastConsensusNanos);
        Assertions.assertEquals(this.lastConsensusNanos, token.getLastConsensusNanos());

        token.setTopicId(this.topicId);
        Assertions.assertEquals(this.topicId, token.getTopicId());

        Map<String, Address> addresses = new HashMap<>();
        Address address = new Address(this.firstPublicKey);
        addresses.put(this.firstPublicKey, address);
        token.setAddresses(addresses);

        Assertions.assertEquals(1, token.getAddresses().size());
        Assertions.assertNotNull(token.getAddress(this.firstPublicKey));

        address = new Address(this.secondPublicKey);
        addresses.put(this.secondPublicKey, address);

        Assertions.assertEquals(2, token.getAddresses().size());
        Assertions.assertNotNull(token.getAddress(this.secondPublicKey));
    }
}
