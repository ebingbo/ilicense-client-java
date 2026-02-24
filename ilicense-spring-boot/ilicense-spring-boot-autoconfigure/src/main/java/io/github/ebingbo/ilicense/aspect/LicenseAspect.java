package io.github.ebingbo.ilicense.aspect;

import io.github.ebingbo.ilicense.annotation.RequireLicense;
import io.github.ebingbo.ilicense.core.LicenseManager;
import io.github.ebingbo.ilicense.exception.LicenseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LicenseAspect {

    private final LicenseManager licenseManager;

    @Around("@annotation(io.github.ebingbo.ilicense.annotation.RequireLicense) || " +
            "@within(io.github.ebingbo.ilicense.annotation.RequireLicense)")
    public Object checkLicense(ProceedingJoinPoint joinPoint) throws Throwable {

        log.info("license check start ...");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequireLicense annotation = AnnotatedElementUtils.findMergedAnnotation(signature.getMethod(), RequireLicense.class);

        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(joinPoint.getTarget().getClass(), RequireLicense.class);
        }
        if (annotation == null) {
            log.info("license check end, not found @RequireLicense annotation");
            return joinPoint.proceed();
        }

        try {
            // 检查License有效性
            licenseManager.checkLicense();

            // 检查模块权限
            String requiredModule = annotation.module();
            if (!requiredModule.isEmpty()) {
                licenseManager.checkModule(requiredModule);
            }

            return joinPoint.proceed();

        } catch (LicenseException e) {
            log.error("license check fail: {}", e.getMessage());
            throw e;
        }
    }
}
