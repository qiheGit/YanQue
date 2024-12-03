package org.qh.DDBMS.common.output.impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.config.GDBMSConfig;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.output.Connector;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/23
 * @Version: 0.0.0
 * @Description: Connector接口的默认实现类
 */
public class DefaultConnector implements Connector {

    /**
     * <pre>
     * 说明：用于连接其他站点
     * </pre>
     */
    private Bootstrap bootstrap;

    /**
     * <pre>
     * 说明：bootStrap的初始化器
     * </pre>
     */
    @Resource
    private ChannelInitializer<?> initializer;

    /**
     * <pre>
     * 说明：工作线程池
     * </pre>
     */
    private NioEventLoopGroup workGroup;

    /**
     * <pre>
     * 说明：用于获取配置的GDBMS站点配置信息
     * </pre>
     */
    @Resource
    private GDBMSConfig gdbmsConfig;

    /**
     * <pre>
     * 说明：启动当前客户端连接到GDBMS
     * 实现步骤：
     *   1) 初始化bootstrap
     *   2) 连接到GDBMS
     * </pre>
     */
    @PostConstruct
    public void start() {
        init();
        connect(gdbmsConfig.ip(), gdbmsConfig.port());
    }

    private void init() {
        workGroup = new NioEventLoopGroup(Constant.ServerAndClient.WORK_THREAD_COUNT);
        bootstrap = new Bootstrap()
                .group(workGroup)
                .channel(NioSocketChannel.class)
                .handler(initializer);
    }

    /**
     * <pre>
     * 说明：销毁当前客户端
     * 实现步骤：
     *   1) 关闭所有工作线程
     * </pre>
     */
    @PreDestroy
    public void destroy() {
        workGroup.shutdownGracefully();
    }

    /**
     * <pre>
     * 说明：连接到其他站点
     * 实现步骤：
     *   1) 调用bootStrap的方法进行连接。
     * </pre>
     * @param host String 目标站点的主机地址
     * @param port int 目标站点的端口号
     */
    @Override
    public void connect(String host, int port) {
        bootstrap.connect(host, port);
    }
}

