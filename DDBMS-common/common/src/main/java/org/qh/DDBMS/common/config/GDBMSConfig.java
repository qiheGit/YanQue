package org.qh.DDBMS.common.config;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: 有关全局数据库管理系统的配置信息
 */
public interface GDBMSConfig {

    /**
     * 说明：配置的GDBMS所在主机的ip地址
     * @return GDBMS主机地址
     * @since 0.0.0
     */
    String ip();

    /**
     * 说明：配置的GDBMS服务监听的端口
     * @return GDBMS服务监听的端口
     * @since 0.0.0
     */
    int port();

    /**
     * 说明：配置的GDBMS服务站点名
     * @return GDBMS服务站点名称
     * @since 0.0.0
     */
    String siteName();
}
