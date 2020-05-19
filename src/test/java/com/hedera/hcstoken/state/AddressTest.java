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
}
