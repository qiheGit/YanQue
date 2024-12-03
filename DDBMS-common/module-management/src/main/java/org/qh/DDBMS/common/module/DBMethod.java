package org.qh.DDBMS.common.module;

import java.lang.reflect.Method;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 此接口是一个数据库方法的抽象接口
 */
public interface DBMethod {

    /**
     * <pre>
     * 说明：该方法返回操作数据方法的执行对象
     * @return Object 执行对象
     * @since 0.0.0
     */
    Object obj();

    /**
     * <pre>
     * 说明：该方法返回一个方法实例的uri
     * @return String 方法实例的uri
     * @since 0.0.0
     */
    String uri();

    /**
     * <pre>
     * 说明：该方法返回一个方法实例
     * @return Method 方法实例
     * @since 0.0.0
     */
    Method method();
}
