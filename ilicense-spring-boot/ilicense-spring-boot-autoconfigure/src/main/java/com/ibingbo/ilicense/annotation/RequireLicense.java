package com.ibingbo.ilicense.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLicense {
    /**
     * 需要的模块名称
     */
    @AliasFor("module")
    String value() default "";

    /**
     * 需要的模块名称
     */
    @AliasFor("value")
    String module() default "";

    /**
     * 错误提示信息
     */
    String message() default "该功能需要有效的License授权";
}
