package org.qh.DDBMS.common.ack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version:
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AckHandler {

    int value() default Integer.MIN_VALUE; // 表示对什么类型的ACKType进行处理
}
