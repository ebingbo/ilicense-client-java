package com.ibingbo.sdk.license.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibingbo.sdk.license.exception.LicenseException;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
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
            Map<String, Object> dataMap = objectMapper.readValue(jsonData, Map.class);

            LicenseInfo info = new LicenseInfo();
            info.setLicenseCode((String) dataMap.get("licenseCode"));
            info.setCustomerCode(String.valueOf(dataMap.get("customerCode")));
            info.setCustomerName((String) dataMap.get("customerName"));
            info.setProductCode((String) dataMap.get("productCode"));
            info.setProductName((String) dataMap.get("productName"));
            info.setIssuerCode((String) dataMap.get("issuerCode"));
            info.setIssuerName((String) dataMap.get("issuerName"));
            info.setIssueDate(LocalDate.parse((String) dataMap.get("issueDate")));
            info.setExpiryDate(LocalDate.parse((String) dataMap.get("expiryDate")));
            info.setModules((List<String>) dataMap.get("modules"));
            info.setMaxInstances((Integer) dataMap.get("maxInstances"));

            // 6. 计算有效性
            info.setValid(!info.isExpired());
            info.setDaysLeft(java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), info.getExpiryDate()));

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