package org.qh.DDBMS.common.output;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/23
 * @Version: 0.0.0
 * @Description: 连接器，用于连接其他的站点
 */
public interface Connector {

    /**
     * <pre>
     * 说明：连接到其他站点
     * </pre>
     * @param host String 目标站点的主机地址
     * @param port int 目标站点的端口号
     * @since 0.0.0
     */
    void connect(String host, int port);
}
