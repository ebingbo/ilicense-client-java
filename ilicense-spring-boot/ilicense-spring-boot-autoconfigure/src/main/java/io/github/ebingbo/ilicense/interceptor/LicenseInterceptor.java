package io.github.ebingbo.ilicense.interceptor;

import io.github.ebingbo.ilicense.config.LicenseProperties;
import io.github.ebingbo.ilicense.core.LicenseManager;
import io.github.ebingbo.ilicense.exception.LicenseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Slf4j
@Component
public class LicenseInterceptor implements HandlerInterceptor {

    private final LicenseManager licenseManager;
    private final LicenseProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        log.info("license interceptor started processing request {}", request.getRequestURI());
        try {
            licenseManager.checkLicense();
            log.info("license interceptor passed request {}", request.getRequestURI());
            return true;
        } catch (LicenseException e) {
            log.warn("license interceptor blocked request {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":403,\"message\":\"" + e.getMessage() + "\"}"
            );
            return false;
        }
    }
}