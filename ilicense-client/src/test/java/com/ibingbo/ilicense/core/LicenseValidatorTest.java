package com.ibingbo.ilicense.core;

import com.ibingbo.ilicense.exception.LicenseException;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LicenseValidatorTest {

    @Test
    void validateShouldParseLicenseInfoWhenSignatureIsValid() throws Exception {
        KeyPair keyPair = LicenseCryptoTestSupport.generateRsaKeyPair();
        String activationCode = LicenseCryptoTestSupport.buildActivationCode(
                keyPair.getPrivate(),
                OffsetDateTime.now().plusDays(30),
                "base,advanced"
        );

        LicenseValidator validator = new LicenseValidator(LicenseCryptoTestSupport.toPemPublicKey(keyPair));
        LicenseInfo info = validator.validate(activationCode);

        assertEquals("Demo Customer", info.getCustomerName());
        assertEquals("Demo Product", info.getProductName());
        assertTrue(info.hasModule("advanced"));
        assertFalse(info.isExpired());
        assertTrue(info.getDaysLeft() >= 29);
    }

    @Test
    void validateShouldThrowWhenSignatureVerificationFails() throws Exception {
        KeyPair signKey = LicenseCryptoTestSupport.generateRsaKeyPair();
        KeyPair verifyKey = LicenseCryptoTestSupport.generateRsaKeyPair();
        String activationCode = LicenseCryptoTestSupport.buildActivationCode(
                signKey.getPrivate(),
                OffsetDateTime.now().plusDays(30),
                "base"
        );

        LicenseValidator validator = new LicenseValidator(LicenseCryptoTestSupport.toPemPublicKey(verifyKey));

        assertThrows(LicenseException.class, () -> validator.validate(activationCode));
    }
}
