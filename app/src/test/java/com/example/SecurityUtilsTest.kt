package com.example

import com.example.data.util.SecurityUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun redeemCode_acceptsValidCodeAndRejectsInvalidInputs() {
        assertTrue(SecurityUtils.verifyPremiumRedeemCode("charfeddine23a"))
        assertTrue(SecurityUtils.verifyPremiumRedeemCode("  charfeddine23a  "))
        assertFalse(SecurityUtils.verifyPremiumRedeemCode(""))
        assertFalse(SecurityUtils.verifyPremiumRedeemCode("   "))
        assertFalse(SecurityUtils.verifyPremiumRedeemCode("CHARFEDDINE23A"))
        assertFalse(SecurityUtils.verifyPremiumRedeemCode("free_bypass"))
    }

    @Test
    fun vaultPinHash_usesUniqueSaltAndVerifiesCorrectly() {
        val saltA = SecurityUtils.generateSaltHex()
        val saltB = SecurityUtils.generateSaltHex()
        assertNotEquals(saltA, saltB)

        val hashA = SecurityUtils.hashVaultPin("4829", saltA)
        val hashB = SecurityUtils.hashVaultPin("4829", saltB)
        assertNotEquals(hashA, hashB)
        assertNotEquals("4829", hashA)

        assertTrue(SecurityUtils.verifyVaultPinHash("4829", saltA, hashA))
        assertFalse(SecurityUtils.verifyVaultPinHash("1234", saltA, hashA))
        assertFalse(SecurityUtils.verifyVaultPinHash("", saltA, hashA))
    }
}
