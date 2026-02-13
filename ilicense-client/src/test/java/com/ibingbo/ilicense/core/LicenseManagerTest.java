package com.ibingbo.ilicense.core;

import com.ibingbo.ilicense.config.LicenseClientProperties;
import com.ibingbo.ilicense.event.LicenseEventListener;
import com.ibingbo.ilicense.exception.LicenseException;
import com.ibingbo.ilicense.exception.LicenseNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LicenseManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void activateShouldPersistAndPublishActivationEvent() throws Exception {
        String code = "ACTIVATION-CODE-001";
        LicenseInfo licenseInfo = newLicense(OffsetDateTime.now().plusDays(10), "base,advanced");

        FakeValidator validator = new FakeValidator();
        validator.register(code, licenseInfo);

        RecordingListener listener = new RecordingListener();
        LicenseManager manager = new LicenseManager(newProperties(tempDir.resolve("license.dat")), validator, listener);

        LicenseInfo activated = manager.activate(code);

        assertEquals("Demo Customer", activated.getCustomerName());
        assertEquals(1, listener.activatedCount);
        assertTrue(Files.exists(tempDir.resolve("license.dat")));
        assertEquals(code, Files.readString(tempDir.resolve("license.dat")));
    }

    @Test
    void checkLicenseShouldThrowWhenLicenseNotActivated() {
        LicenseManager manager = new LicenseManager(newProperties(tempDir.resolve("license.dat")), new FakeValidator(), LicenseEventListener.NO_OP);

        assertThrows(LicenseNotFoundException.class, manager::checkLicense);
    }

    @Test
    void checkModuleShouldThrowWhenModuleNotGranted() {
        String code = "ACTIVATION-CODE-002";
        LicenseInfo licenseInfo = newLicense(OffsetDateTime.now().plusDays(5), "base");

        FakeValidator validator = new FakeValidator();
        validator.register(code, licenseInfo);

        LicenseManager manager = new LicenseManager(newProperties(tempDir.resolve("license.dat")), validator, LicenseEventListener.NO_OP);
        manager.activate(code);

        assertThrows(LicenseException.class, () -> manager.checkModule("advanced"));
    }

    @Test
    void initShouldLoadLicenseWhenValidateOnStartupEnabled() throws Exception {
        String code = "ACTIVATION-CODE-003";
        Path storagePath = tempDir.resolve("license.dat");
        Files.writeString(storagePath, code);

        FakeValidator validator = new FakeValidator();
        validator.register(code, newLicense(OffsetDateTime.now().plusDays(3), "base"));

        LicenseClientProperties properties = newProperties(storagePath);
        properties.setValidateOnStartup(true);

        LicenseManager manager = new LicenseManager(properties, validator, LicenseEventListener.NO_OP);
        manager.init();

        assertNotNull(manager.getCurrentLicense());
        assertTrue(manager.isValid());
    }

    @Test
    void initShouldPublishExpiredEventWhenLoadedLicenseIsExpired() throws Exception {
        String code = "ACTIVATION-CODE-004";
        Path storagePath = tempDir.resolve("license.dat");
        Files.writeString(storagePath, code);

        FakeValidator validator = new FakeValidator();
        validator.register(code, newLicense(OffsetDateTime.now().minusDays(1), "base"));

        RecordingListener listener = new RecordingListener();
        LicenseClientProperties properties = newProperties(storagePath);
        properties.setValidateOnStartup(true);
        properties.setAllowStartWhenExpired(true);

        LicenseManager manager = new LicenseManager(properties, validator, listener);
        manager.init();

        assertEquals(1, listener.expiredCount);
    }

    private static LicenseClientProperties newProperties(Path storagePath) {
        LicenseClientProperties properties = new LicenseClientProperties();
        properties.setEnabled(true);
        properties.setStoragePath(storagePath.toString());
        properties.setValidateOnStartup(false);
        properties.setAllowStartWhenExpired(true);
        properties.setExpiryWarningDays(30);
        return properties;
    }

    private static LicenseInfo newLicense(OffsetDateTime expireAt, String modules) {
        LicenseInfo info = new LicenseInfo();
        info.setCustomerName("Demo Customer");
        info.setProductName("Demo Product");
        info.setExpireAt(expireAt);
        info.setModules(modules);
        info.setDaysLeft(1);
        info.setValid(!info.isExpired());
        return info;
    }

    private static class FakeValidator extends LicenseValidator {

        private final Map<String, LicenseInfo> licenses = new HashMap<>();

        FakeValidator() {
            super("unused-public-key");
        }

        void register(String activationCode, LicenseInfo info) {
            licenses.put(activationCode, info);
        }

        @Override
        public LicenseInfo validate(String activationCode) {
            LicenseInfo info = licenses.get(activationCode);
            if (info == null) {
                throw new LicenseException("unknown activation code");
            }
            return info;
        }
    }

    private static class RecordingListener implements LicenseEventListener {
        int activatedCount;
        int expiredCount;

        @Override
        public void onActivated(LicenseInfo licenseInfo) {
            activatedCount++;
        }

        @Override
        public void onExpired(LicenseInfo licenseInfo) {
            expiredCount++;
        }
    }
}
