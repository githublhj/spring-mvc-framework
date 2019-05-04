package com.lhj.feamework.v1.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @Author: lhj
 * @Time: 2019/5/4 14:46
 * @Version: 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LhjAutowired {
    String value() default "";
}
