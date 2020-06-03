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
import org.junit.jupiter.api.*;

public class PrimitivesTest extends AbstractTestData {

    @Test
    public void testConstruct() throws Exception {

        Token token = new Token();

        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);

        Assertions.assertEquals(this.name, token.getName());
        Assertions.assertEquals(this.symbol, token.getSymbol());
        Assertions.assertEquals(this.decimals, token.getDecimals());

        Address ownerAddress = token.getAddress(this.pubKeyOwner);
        Assertions.assertEquals(this.pubKeyOwner, ownerAddress.getPublicKey());
        Assertions.assertEquals(0, ownerAddress.getBalance());
        Assertions.assertTrue(ownerAddress.isOwner());

        try {
            Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
            Assertions.fail("Should throw exception when constructing an existing token");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Construct - Token already constructed"));
        }
    }

    @Test
    public void testMint() throws Exception {

        Token token = new Token();

        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);

        // mint with the wrong owner address
        try {
            Primitives.mint(token, this.pubKeyOther, this.quantity);
            Assertions.fail("Should throw exception when minting with the wrong owner address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Address is not token owner's address"));
        }

        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Assertions.assertEquals(this.name, token.getName());
        Assertions.assertEquals(this.symbol, token.getSymbol());
        Assertions.assertEquals(this.decimals, token.getDecimals());

        long tokenSupply = (long) (this.quantity * Math.pow(10, token.getDecimals()));
        Assertions.assertEquals(tokenSupply, token.getTotalSupply());

        Address ownerAddress = token.getAddress(this.pubKeyOwner);
        Assertions.assertEquals(tokenSupply, ownerAddress.getBalance());

        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Assertions.assertEquals(tokenSupply * 2, ownerAddress.getBalance());
    }

    @Test
    public void testJoin() throws Exception {

        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        Assertions.assertEquals(1, token.getAddresses().size());

        Primitives.join(token, this.pubKeyOther);

        Assertions.assertEquals(2, token.getAddresses().size());
        Assertions.assertNotNull(token.getAddress(this.pubKeyOther));
        Assertions.assertEquals(0, token.getAddress(this.pubKeyOther).getBalance());
        Assertions.assertFalse(token.getAddress(this.pubKeyOther).isOwner());

        // join twice
        try {
            Primitives.join(token, this.pubKeyOther);
            Assertions.fail("Should throw exception when joining twice with the same address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains(" already part of the App Net"));
            Assertions.assertEquals(2, token.getAddresses().size());
        }
    }

    @Test
    public void testTransfer() throws Exception {

        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);

        // transfer from unknown address
        try {
            Primitives.transfer(token, "unknown from address", this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when transferring from unknown address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Transfer - from address unknown"));
        }

        // transfer to empty address (null)
        try {
            Primitives.transfer(token, this.pubKeyOwner, null, 1);
            Assertions.fail("Should throw exception when transferring to empty address (null)");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Transfer - to address is empty"));
        }

        // transfer to empty address ("")
        try {
            Primitives.transfer(token, this.pubKeyOwner, "", 1);
            Assertions.fail("Should throw exception when transferring to empty address (\"\")");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Transfer - to address is empty"));
        }

        // transfer to new address
        String newAddress = "new address";
        long senderBalance = token.getAddress(this.pubKeyOwner).getBalance();

        Primitives.transfer(token, this.pubKeyOwner, newAddress, this.transferAmount);
        Assertions.assertEquals(senderBalance - this.transferAmount, token.getAddress(this.pubKeyOwner).getBalance());
        Assertions.assertEquals(this.transferAmount, token.getAddress(newAddress).getBalance());

        // transfer to existing address
        senderBalance = token.getAddress(this.pubKeyOwner).getBalance();

        Primitives.transfer(token, this.pubKeyOwner, this.pubKeyOther, this.transferAmount);
        Assertions.assertEquals(senderBalance - this.transferAmount, token.getAddress(this.pubKeyOwner).getBalance());
        Assertions.assertEquals(this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());

        // transfer over balance
        try {
            long overBalance = token.getAddress(this.pubKeyOwner).getBalance() * 10;
            Primitives.transfer(token, this.pubKeyOwner, this.pubKeyOther, overBalance);
            Assertions.fail("Should throw exception when transferring above balance");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Transfer - Insufficient balance"));
        }
    }

    @Test
    public void testApprove() throws Exception {
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        // approve from unknown address
        try {
            Primitives.approve(token, "unknown from address", this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when approving an unknown address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Approve - from address unknown"));
        }

        // approve null/empty sender address
        try {
            Primitives.approve(token, this.pubKeyOwner, null, 1);
            Assertions.fail("Should throw exception when approving an empty address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Approve - spender address is empty"));
        }

        try {
            Primitives.approve(token, this.pubKeyOwner, "", 1);
            Assertions.fail("Should throw exception when approving an empty address");
        } catch(Exception e){
            Assertions.assertTrue(e.getMessage().contains("Approve - spender address is empty"));
        }

        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);
        Assertions.assertEquals(1, token.getAddress(this.pubKeyOwner).getAllowances().size());
        Assertions.assertEquals(this.approveAmount, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));

    }

    @Test
    public void increaseAllowanceTest() throws Exception {
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        // increase allowance for an unknown from address
        try {
            Primitives.increaseAllowance(token, "unknown from address", this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when increasing allowance for unknown from address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("IncreaseAllowance - from address unknown"));
        }

        // increase allowance for an empty spender address
        try {
            Primitives.increaseAllowance(token, this.pubKeyOwner, null, 1);
            Assertions.fail("Should throw exception when increasing allowance for unknown spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("IncreaseAllowance - spender address is empty"));
        }

        try {
            Primitives.increaseAllowance(token, this.pubKeyOwner, "", 1);
            Assertions.fail("Should throw exception when increasing allowance for unknown spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("IncreaseAllowance - spender address is empty"));
        }

        // increase for not yet approved address
        try {
            Primitives.increaseAllowance(token, this.pubKeyOwner, this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when increasing allowance for unapproved spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("IncreaseAllowance - spender address is not approved"));
        }

        // approve and increase
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);
        Primitives.increaseAllowance(token, this.pubKeyOwner, this.pubKeyOther, this.allowance);
        Assertions.assertEquals(this.approveAmount + this.allowance, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
    }

    @Test
    public void decreaseAllowanceTest() throws Exception {
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        // decrease allowance for an unknown from address
        try {
            Primitives.decreaseAllowance(token, "unknown from address", this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when decreasing allowance for unknown from address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("DecreaseAllowance - from address unknown"));
        }

        // decrease allowance for an empty spender address
        try {
            Primitives.decreaseAllowance(token, this.pubKeyOwner, null, 1);
            Assertions.fail("Should throw exception when decreasing allowance for unknown spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("DecreaseAllowance - spender address is empty"));
        }

        try {
            Primitives.decreaseAllowance(token, this.pubKeyOwner, "", 1);
            Assertions.fail("Should throw exception when decreasing allowance for unknown spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("DecreaseAllowance - spender address is empty"));
        }

        // decrease for not yet approved address
        try {
            Primitives.decreaseAllowance(token, this.pubKeyOwner, this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when decreasing allowance for unapproved spender address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("DecreaseAllowance - spender address is not approved"));
        }

        // approve and decrease
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);
        Primitives.decreaseAllowance(token, this.pubKeyOwner, this.pubKeyOther, this.allowance);
        Assertions.assertEquals(this.approveAmount - this.allowance, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));

        // decrease beyond current allowance
        try {
            Primitives.decreaseAllowance(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);
            Assertions.fail("Should throw exception when decreasing allowance below 0");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("DecreaseAllowance - decreased allowance below zero"));
        }
    }

    @Test
    public void transferFromTest() throws Exception {
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);
        Primitives.join(token, this.pubKeyOther);

        try {
            Primitives.transferFrom(token, "unknown sender address", this.pubKeyOwner, this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when transferring from an unknown message sender");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - operation initiator unknown"));
        }

        // transferFrom for an unknown from address
        try {
            Primitives.transferFrom(token, this.pubKeyOther, "unknown from address", this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when transferring from an unknown from address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - from address unknown"));
        }

        // transferFrom for an empty to address
        try {
            Primitives.transferFrom(token, this.pubKeyOther, this.pubKeyOwner,null, 1);
            Assertions.fail("Should throw exception when transferring from an unknown from address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - toAddress address is empty"));
        }

        try {
            Primitives.transferFrom(token, this.pubKeyOther, this.pubKeyOwner,"", 1);
            Assertions.fail("Should throw exception when transferring from an unknown from address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - toAddress address is empty"));
        }

        // transfer from an unapproved address
        try {
            Primitives.transferFrom(token, this.pubKeyOther, this.pubKeyOwner,this.pubKeyOther, 1);
            Assertions.fail("Should throw exception when transferring from an unapproved  address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - operation initiator is not approved"));
        }

        // approve
        Primitives.approve(token, this.pubKeyOwner, this.pubKeyOther, this.approveAmount);

        // attempt to transfer more than approved
        try {
            Primitives.transferFrom(token, this.pubKeyOther, this.pubKeyOwner,this.pubKeyOther, this.approveAmount * 10);
            Assertions.fail("Should throw exception when transferring above approved amount");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("TransferFrom - specified amount above approved allowance"));
        }

        long currentOwnerBalance = token.getAddress(this.pubKeyOwner).getBalance();
        long currentApproval = token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther);
        long currentOtherBalance = token.getAddress(this.pubKeyOther).getBalance();

        Primitives.transferFrom(token, this.pubKeyOther, this.pubKeyOwner,this.pubKeyOther, this.transferAmount);

        Assertions.assertEquals(currentOwnerBalance - this.transferAmount, token.getAddress(this.pubKeyOwner).getBalance());
        Assertions.assertEquals(currentOtherBalance + this.transferAmount, token.getAddress(this.pubKeyOther).getBalance());
        Assertions.assertEquals(currentApproval - this.transferAmount, token.getAddress(this.pubKeyOwner).getAllowance(this.pubKeyOther));
    }

    @Test
    public void burnTest() throws Exception {
        Token token = new Token();
        Primitives.construct(token, this.pubKeyOwner, this.name, this.symbol, this.decimals);
        Primitives.mint(token, this.pubKeyOwner, this.quantity);

        // burn unknown address
        try {
            Primitives.burn(token, "unknown sender address", 1);
            Assertions.fail("Should throw exception when burning from an unknown address");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Burn - unknown address"));
        }

        // burn above balance
        try {
            Primitives.burn(token, this.pubKeyOwner, this.totalSupply * 10);
            Assertions.fail("Should throw exception when burning above balance");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("Burn - amount exceeds balance"));
        }

        long currentOwnerBalance = token.getAddress(this.pubKeyOwner).getBalance();
        long currentSupply = token.getTotalSupply();

        Primitives.burn(token, this.pubKeyOwner, this.burnAmount);

        Assertions.assertEquals(currentOwnerBalance - this.burnAmount, token.getAddress(this.pubKeyOwner).getBalance());
        Assertions.assertEquals(currentSupply - this.burnAmount, token.getTotalSupply());
    }
}
