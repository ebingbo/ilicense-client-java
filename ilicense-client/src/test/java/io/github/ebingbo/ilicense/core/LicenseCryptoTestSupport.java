package io.github.ebingbo.ilicense.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

final class LicenseCryptoTestSupport {

    private LicenseCryptoTestSupport() {
    }

    static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    static String toPemPublicKey(KeyPair keyPair) {
        String base64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    static String buildActivationCode(PrivateKey privateKey, OffsetDateTime expireAt, String modules) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("license_code", "LIC-001");
        payload.put("customer_code", "CUST-001");
        payload.put("customer_name", "Demo Customer");
        payload.put("product_code", "PRD-001");
        payload.put("product_name", "Demo Product");
        payload.put("issuer_code", "ISS-001");
        payload.put("issuer_name", "Demo Issuer");
        payload.put("issue_at", OffsetDateTime.now().minusDays(1));
        payload.put("expire_at", expireAt);
        payload.put("modules", modules);
        payload.put("max_instances", 10);

        byte[] dataBytes = mapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = sign(dataBytes, privateKey);

        ByteBuffer buffer = ByteBuffer.allocate(4 + dataBytes.length + 4 + signatureBytes.length);
        buffer.putInt(dataBytes.length);
        buffer.put(dataBytes);
        buffer.putInt(signatureBytes.length);
        buffer.put(signatureBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

    private static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }
}
