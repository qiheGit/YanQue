package org.qh.DDBMS.common.config;

import lombok.Setter;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/25
 * @Version: 0.0.0
 * @Description: GDBMSConfig接口的默认实现类
 */
@Setter
public class DefaultGDBMSConfig implements GDBMSConfig{
    private String ip; // GDBMS可被访问的ip地址
    private int port;  // GDBMS服务监听的端口号

    private String siteName; // GDBMS的站点名

    @Override
    public String ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String siteName() {
        return siteName;
    }
}
