package org.qh.DDBMS.common.input;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.qh.DDBMS.common.Constant;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: NettyServer,在特定端口进行监听
 */
@Slf4j
public class DDBMSNettyServer {

    /**
     * <pre>
     * 说明：channel初始化器
     * </pre>
     */
    @Resource
    private ServerChannelInitializer channelInitializer;

    /**
     * <pre>
     * 说明：获取配置信息的接口实例
     * </pre>
     */
    @Resource
    private ServerConfig config;

    /**
     * <pre>
     * 说明：监听的连接事件的线程组
     * </pre>
     */
    private NioEventLoopGroup bossGroup;

    /**
     * <pre>
     * 说明：处理读写事件的线程组
     * </pre>
     */
    private NioEventLoopGroup workGroup;

    /**
     * <pre>
     * 说明：启动当前服务在特定端口进行监听
     * 实现步骤：
     *   1) 按照模板进行启动
     * </pre>
     */
    @PostConstruct
    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(Constant.ServerAndClient.BOSS_THREAD_COUNT);
        workGroup = new NioEventLoopGroup(Constant.ServerAndClient.WORK_THREAD_COUNT);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(channelInitializer);
        serverBootstrap.bind(config.port()).sync();
        log.info("Server has monitored at {} port", config.port());
    }

    /**
     * <pre>
     * 说明：关闭当前服务
     * 实现步骤：
     *   1) 关闭bossGroup
     *   2) 关闭workGroup
     * </pre>
     */
    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }
}

