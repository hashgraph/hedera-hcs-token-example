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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

public class PersistenceTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PersistenceTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PersistenceTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testPersistence() throws Exception {
        final File stateFile = new File(getClass().getClassLoader().getResource("test.json").getFile());

        final String topicId = "0.0.43342";
        final long totalSupply = 1000;
        final String symbol = "TTT";
        final String name = "TestToken";
        final int decimals = 8;
        final long lastConsensusSeconds = 1589301202;
        final int lastConsensusNanos = 955026000;

        final String pubKeyOwner = "302a300506032b65700321006e42135c6c7c9162a5f96f6d693677742fd0b3f160e1168cc28f2dadaa9e79cc";
        final long ownerBalance = 980;

        final String pubKeyOther = "302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232";
        final long otherBalance = 20;

        Token token = new Token();

        token.setTopicId(topicId);
        token.setTotalSupply(totalSupply);
        token.setSymbol(symbol);
        token.setName(name);
        token.setDecimals(decimals);
        token.setLastConsensusSeconds(lastConsensusSeconds);
        token.setLastConsensusNanos(lastConsensusNanos);

        token.addAddress(pubKeyOwner);
        token.getAddress(pubKeyOwner).setBalance(ownerBalance);
        token.getAddress(pubKeyOwner).setOwner(true);

        token.addAddress(pubKeyOther);
        token.getAddress(pubKeyOther).setBalance(otherBalance);

        Persistence.saveToken(token, stateFile);

        token = null;
        Token tokenLoad = Persistence.loadToken(stateFile);

        assertEquals("0.0.43342", tokenLoad.getTopicId());
        assertEquals(totalSupply, tokenLoad.getTotalSupply());
        assertEquals(symbol, tokenLoad.getSymbol());
        assertEquals(name, tokenLoad.getName());
        assertEquals(decimals, tokenLoad.getDecimals());
        assertEquals(lastConsensusSeconds, tokenLoad.getLastConsensusSeconds());
        assertEquals(lastConsensusNanos, tokenLoad.getLastConsensusNanos());
        assertEquals(2, tokenLoad.getAddresses().size());

        assertEquals(980, tokenLoad.getAddress(pubKeyOwner).getBalance());
        assertEquals(pubKeyOwner, tokenLoad.getAddress(pubKeyOwner).getPublicKey());
        assertTrue(tokenLoad.getAddress(pubKeyOwner).isOwner());

        assertEquals(20, tokenLoad.getAddress(pubKeyOther).getBalance());
        assertEquals(pubKeyOther, tokenLoad.getAddress(pubKeyOther).getPublicKey());
        assertFalse(tokenLoad.getAddress(pubKeyOther).isOwner());

    }
}
