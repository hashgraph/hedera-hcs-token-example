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

public class PersistenceTest extends AbstractTestData {

    @Test
    public void testPersistence() throws Exception {

        Token token = new Token();

        token.setTopicId(this.topicId);
        token.setTotalSupply(this.totalSupply);
        token.setSymbol(this.symbol);
        token.setName(this.name);
        token.setDecimals(this.decimals);
        token.setLastConsensusSeconds(this.lastConsensusSeconds);
        token.setLastConsensusNanos(this.lastConsensusNanos);

        token.addAddress(this.pubKeyOwner);
        token.getAddress(this.pubKeyOwner).setBalance(this.ownerBalance);
        token.getAddress(this.pubKeyOwner).setOwner(true);

        token.addAddress(this.pubKeyOther);
        token.getAddress(this.pubKeyOther).setBalance(this.otherBalance);

        Persistence.saveToken(token, this.stateFile);

        token = null;
        Token tokenLoad = Persistence.loadToken(this.stateFile);

        Assertions.assertEquals("0.0.43342", tokenLoad.getTopicId());
        Assertions.assertEquals(totalSupply, tokenLoad.getTotalSupply());
        Assertions.assertEquals(symbol, tokenLoad.getSymbol());
        Assertions.assertEquals(name, tokenLoad.getName());
        Assertions.assertEquals(decimals, tokenLoad.getDecimals());
        Assertions.assertEquals(lastConsensusSeconds, tokenLoad.getLastConsensusSeconds());
        Assertions.assertEquals(lastConsensusNanos, tokenLoad.getLastConsensusNanos());
        Assertions.assertEquals(2, tokenLoad.getAddresses().size());

        Assertions.assertEquals(980, tokenLoad.getAddress(pubKeyOwner).getBalance());
        Assertions.assertEquals(pubKeyOwner, tokenLoad.getAddress(pubKeyOwner).getPublicKey());
        Assertions.assertTrue(tokenLoad.getAddress(pubKeyOwner).isOwner());

        Assertions.assertEquals(20, tokenLoad.getAddress(pubKeyOther).getBalance());
        Assertions.assertEquals(pubKeyOther, tokenLoad.getAddress(pubKeyOther).getPublicKey());
        Assertions.assertFalse(tokenLoad.getAddress(pubKeyOther).isOwner());

    }
}
