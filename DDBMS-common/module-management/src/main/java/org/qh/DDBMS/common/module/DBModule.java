package org.qh.DDBMS.common.module;

import org.springframework.context.ApplicationContext;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/22
 * @Version: 0.0.0
 * @Description: 此接口的实例表示用户定义的，用于操作数据库的模块
 */
public interface DBModule {

    /**
     * 说明：该方法返回一个模块的主启动类
     * @return 模块的主启动类
     * @since 0.0.0
     */
    public Class<?> main();

    /**
     * 说明：该方法返回一个模块的Spring容器
     * @return 模块的Spring容器
     * @since 0.0.0
     */
    public ApplicationContext ioc();
}

