package org.qh.DDBMS.common.input;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: 获取server必要配置的接口
 */
public interface ServerConfig {

    /**
     * <pre>
     * 说明：获取当前server监听的端口
     * </pre>
     * @return int 当前监听的端口
     * @since 0.0.0
     */
    int port();

    /**
     * <pre>
     * 说明：获取当前server的站点名称
     * </pre>
     * @return String 当前站点名称
     * @since 0.0.0
     */
    String siteName();

    /**
     * <pre>
     * 说明：获取当前server可被访问的ip地址
     * </pre>
     * @return String 可被其他站点访问的ip地址
     * @since 0.0.0
     */
    String ip();

    /**
     * <pre>
     * 说明：获取本站点维护的数据库名
     * </pre>
     * @return String 数据库名
     * @since 0.0.0
     */
    String dbName();
}

