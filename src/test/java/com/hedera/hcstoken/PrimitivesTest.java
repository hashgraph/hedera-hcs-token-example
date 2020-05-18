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

import com.hedera.hcstoken.state.Address;
import com.hedera.hcstoken.state.Token;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

public class PrimitivesTest
    extends TestCase
{

    private final String topicId = "0.0.43342";
    private final long totalSupply = 1000;
    private final String symbol = "TTT";
    private final String name = "TestToken";
    private final int decimals = 8;
    private final long lastConsensusSeconds = 1589301202;
    private final int lastConsensusNanos = 955026000;

    private final String pubKeyOwner = "302a300506032b65700321006e42135c6c7c9162a5f96f6d693677742fd0b3f160e1168cc28f2dadaa9e79cc";
    private final long ownerBalance = 980;

    private final String pubKeyOther = "302a300506032b65700321009308a434a9cac34e2f7ce95fc671bfbbaa4e43760880c4f1ad5a58a0b3932232";
    private final long otherBalance = 20;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PrimitivesTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( PrimitivesTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testConstruct() throws Exception {

        Token token = new Token();

        Primitives.construct(token, pubKeyOwner, name, symbol, decimals);

        assertEquals(name, token.getName());
        assertEquals(symbol, token.getSymbol());
        assertEquals(decimals, token.getDecimals());

        Address ownerAddress = token.getAddress(pubKeyOwner);
        assertEquals(pubKeyOwner, ownerAddress.getPublicKey());
        assertEquals(0, ownerAddress.getBalance());
        assertTrue(ownerAddress.isOwner());

        try {
            Primitives.construct(token, pubKeyOwner, name, symbol, decimals);
            fail("Should throw exception when constructing an existing token");
        } catch(Exception e){
            assertTrue(e.getMessage().contains("Construct - Token already constructed"));
        }
    }

    public void testMint() throws Exception {

        Token token = new Token();

        Primitives.construct(token, pubKeyOwner, name, symbol, decimals);

        // mint with the wrong owner address
        try {
            Primitives.mint(token, pubKeyOther, 10);
            fail("Should throw exception when minting with the wrong owner address");
        } catch(Exception e){
            assertTrue(e.getMessage().contains("Address is not token owner's address"));
        }

        long quantity = 100;

        Primitives.mint(token, pubKeyOwner, 100);
        assertEquals(name, token.getName());
        assertEquals(symbol, token.getSymbol());
        assertEquals(decimals, token.getDecimals());

        long tokenSupply = quantity * (10 ^ token.getDecimals());
        assertEquals(tokenSupply, token.getTotalSupply());

        Address ownerAddress = token.getAddress(pubKeyOwner);
        assertEquals(tokenSupply, ownerAddress.getBalance());

        try {
            Primitives.mint(token, pubKeyOwner, 10);
            fail("Should throw exception when minting already minted token");
        } catch(Exception e){
            assertTrue(e.getMessage().contains("Mint - Token already minted"));
        }
    }
}
