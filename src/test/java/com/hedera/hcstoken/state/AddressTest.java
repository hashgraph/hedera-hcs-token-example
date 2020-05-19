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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.print.attribute.HashPrintJobAttributeSet;
import java.util.HashMap;
import java.util.Map;

public class AddressTest extends AbstractTestData{

    @Test
    public void testAddress() {
        Address address = new Address(this.firstPublicKey);

        Assertions.assertEquals(this.firstPublicKey, address.getPublicKey());
        Assertions.assertEquals(0, address.getBalance());
        Assertions.assertFalse(address.isOwner());

        address.setBalance(this.balance);
        Assertions.assertEquals(this.balance, address.getBalance());

        address.setPublicKey("");
        Assertions.assertEquals("", address.getPublicKey());

        address.setOwner(true);
        Assertions.assertTrue(address.isOwner());
    }

    @Test
    public void testAddressAllowances() throws Exception {
        Address address = new Address(this.firstPublicKey);

        long allowance = 10;

        Map<String, Long> allowances = new HashMap<String, Long>();
        allowances.put(this.secondPublicKey, allowance);
        Assertions.assertEquals(0, address.getAllowances().size());
        address.setAllowances(allowances);
        Assertions.assertEquals(1, address.getAllowances().size());

        Assertions.assertEquals(allowance, address.getAllowance(this.secondPublicKey));
        Assertions.assertEquals(0, address.getAllowance("unknown"));

        // try adding again
        try {
            address.addAllowance(this.secondPublicKey, allowance);
            Assertions.fail("Should not have allowed duplicate allowance");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("This address is already added to allowances"));
        }

        // try adding self
        try {
            address.addAllowance(this.firstPublicKey, 10L);
            Assertions.fail("Should not have allowed self allowance");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Cannot add self to allowances"));
        }

        // try setting allowances with self included
        allowances = new HashMap<String, Long>();
        allowances.put(this.firstPublicKey, 10L);
        allowances.put(this.secondPublicKey, 10L);
        try {
            address.setAllowances(allowances);
            Assertions.fail("Should not have allowed allowances to be set with self included");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Cannot add self to allowances"));
        }
    }

}
