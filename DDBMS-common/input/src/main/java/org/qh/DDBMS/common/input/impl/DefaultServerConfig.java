package org.qh.DDBMS.common.input.impl;

import org.qh.DDBMS.common.input.ServerConfig;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: ServerConfig的默认实现类
 */
public class DefaultServerConfig implements ServerConfig {

    /**
     * <pre>
     * 说明: 当前server监听的端口
     * </pre>
     */
    private int port;

    /**
     * <pre>
     * 说明：当前站点名
     * </pre>
     */
    private String siteName;

    /**
     * <pre>
     * 说明: 可以被其他设备访问的ip地址
     * </pre>
     */
    private String ip;

    /**
     * <pre>
     * 说明: 本站点维护的数据库名
     * </pre>
     */
    private String dbName;

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public String siteName() {
        return this.siteName;
    }

    @Override
    public String ip() {
        return this.ip;
    }

    @Override
    public String dbName() {
        return this.dbName;
    }
}
