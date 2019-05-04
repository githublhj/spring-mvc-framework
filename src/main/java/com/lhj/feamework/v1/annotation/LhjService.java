package com.lhj.feamework.v1.annotation;

import java.lang.annotation.*;

/**
 * @Description:
 * @Author: lhj
 * @Time: 2019/5/4 14:38
 * @Version: 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LhjService {
    String value() default "";
}
