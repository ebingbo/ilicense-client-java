package com.ibingbo.ilicense.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibingbo.ilicense.exception.LicenseException;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class LicenseValidator {

    private final String publicKey;
    private final ObjectMapper objectMapper;

    public LicenseValidator(String publicKey) {
        this.publicKey = publicKey;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    /**
     * 验证激活码
     */
    public LicenseInfo validate(String activationCode) throws LicenseException {
        try {
            log.info("starting license validation");

            // 1. 清理格式
            String cleaned = activationCode.replaceAll("\\s", "").trim();

            // 2. Base64解码
            byte[] decoded = Base64.getUrlDecoder().decode(cleaned);

            // 3. 解析数据结构
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            int dataLength = buffer.getInt();
            byte[] dataBytes = new byte[dataLength];
            buffer.get(dataBytes);

            int signatureLength = buffer.getInt();
            byte[] signatureBytes = new byte[signatureLength];
            buffer.get(signatureBytes);

            // 4. 验证签名
            PublicKey pubKey = loadPublicKey(publicKey);
            if (!verifySignature(dataBytes, signatureBytes, pubKey)) {
                throw new LicenseException("signature verification failed");
            }

            log.info("signature verification successful");

            // 5. 解析License数据
            String jsonData = new String(dataBytes, StandardCharsets.UTF_8);
            LicenseInfo info = objectMapper.readValue(jsonData, LicenseInfo.class);

            // 6. 计算有效性
            info.setValid(!info.isExpired());
            info.setDaysLeft(ChronoUnit.DAYS.between(
                    Instant.now(), info.getExpireAt().toInstant()));

            log.info("license validation successful: {}", info.getCustomerName());

            return info;

        } catch (Exception e) {
            log.error("license validation failed", e);
            throw new LicenseException("license validation failed: " + e.getMessage(), e);
        }
    }

    private boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey)
            throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }

    private PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        String cleaned = publicKeyStr
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }
}