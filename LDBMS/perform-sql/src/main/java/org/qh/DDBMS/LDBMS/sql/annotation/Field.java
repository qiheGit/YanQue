package org.qh.DDBMS.LDBMS.sql.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description:
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Field {
    String value() default "";
}
