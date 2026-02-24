package io.github.ebingbo.ilicense.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ebingbo.ilicense.annotation.RequireLicense;
import io.github.ebingbo.ilicense.core.LicenseManager;
import io.github.ebingbo.ilicense.event.LicenseActivatedEvent;
import io.github.ebingbo.ilicense.exception.LicenseNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseAutoConfigurationIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateCoreBeansWhenPublicKeyConfigured() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        Path storagePath = tempDir.resolve("license.dat");

        newContextRunner(toPemPublicKey(keyPair), storagePath)
                .run(context -> {
                    assertThat(context).hasSingleBean(LicenseManager.class);
                    assertThat(context).hasBean("licenseValidator");
                    assertThat(context).hasBean("licenseEventListener");
                });
    }

    @Test
    void shouldPublishSpringEventWhenLicenseActivated() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String activationCode = buildActivationCode(keyPair.getPrivate(), OffsetDateTime.now().plusDays(5), "base");
        Path storagePath = tempDir.resolve("activation-license.dat");

        newContextRunner(toPemPublicKey(keyPair), storagePath)
                .run(context -> {
                    AtomicInteger activated = new AtomicInteger();
                    ((ConfigurableApplicationContext) context).addApplicationListener(
                            (ApplicationListener<LicenseActivatedEvent>) event -> activated.incrementAndGet()
                    );

                    LicenseManager licenseManager = context.getBean(LicenseManager.class);
                    licenseManager.activate(activationCode);

                    assertThat(activated.get()).isEqualTo(1);
                });
    }

    @Test
    void shouldEnforceRequireLicenseAspectBeforeAndAfterActivation() throws Exception {
        KeyPair keyPair = generateRsaKeyPair();
        String activationCode = buildActivationCode(keyPair.getPrivate(), OffsetDateTime.now().plusDays(5), "base,advanced");
        Path storagePath = tempDir.resolve("aspect-license.dat");

        newContextRunner(toPemPublicKey(keyPair), storagePath)
                .withUserConfiguration(TestServiceConfiguration.class)
                .run(context -> {
                    ProtectedService protectedService = context.getBean(ProtectedService.class);
                    LicenseManager licenseManager = context.getBean(LicenseManager.class);

                    assertThatThrownBy(protectedService::protectedCall)
                            .isInstanceOf(LicenseNotFoundException.class);

                    licenseManager.activate(activationCode);

                    assertThat(protectedService.protectedCall()).isEqualTo("ok");
                });
    }

    private ApplicationContextRunner newContextRunner(String publicKey, Path storagePath) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, LicenseAutoConfiguration.class))
                .withPropertyValues(
                        "license.enabled=true",
                        "license.public-key=" + publicKey,
                        "license.storage-path=" + storagePath,
                        "license.validate-on-startup=false",
                        "license.enable-scheduled-check=false",
                        "license.enable-api=false",
                        "license.enable-interceptor=false"
                );
    }

    @Configuration(proxyBeanMethods = false)
    static class TestServiceConfiguration {

        @Bean
        ProtectedService protectedService() {
            return new ProtectedService();
        }
    }

    static class ProtectedService {

        @RequireLicense(module = "advanced")
        public String protectedCall() {
            return "ok";
        }
    }

    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String toPemPublicKey(KeyPair keyPair) {
        String base64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    private static String buildActivationCode(PrivateKey privateKey, OffsetDateTime expireAt, String modules) throws Exception {
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
