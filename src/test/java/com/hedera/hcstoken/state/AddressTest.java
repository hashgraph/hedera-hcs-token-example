package com.hedera.hcstoken.state;

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