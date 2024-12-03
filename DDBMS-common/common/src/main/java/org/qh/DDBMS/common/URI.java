package org.qh.DDBMS.common;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该注解用于标记一个资源的URI
 */
public @interface URI {
    /**
     * 说明：此方法用于指定uri的值。
     * @return uri的值
     * @since 0.0.0
     */
    String value();
}
