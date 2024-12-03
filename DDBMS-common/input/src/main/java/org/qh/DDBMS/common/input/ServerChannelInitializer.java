package org.qh.DDBMS.common.input;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: Server的Channel初始化器
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> implements ApplicationContextAware {

    /**
     * <pre>
     * 说明：协议处理器，对接收到的协议进行处理
     * </pre>
     */
    @Resource
    private DDBMSProtocolHandler protocolHandler;

    /**
     * <pre>
     * 说明：协议编码器，用于对发送的协议实例进行加密
     * </pre>
     */
    @Resource
    private DDBMSProtocolEncoder protocolEncoder;

    /**
     * <pre>
     * 说明：全局异常处理器，用于对没有被处理的异常进行处理
     * </pre>
     */
    @Resource
    private ChannelExceptionHandler exceptionHandler;

    /**
     * <pre>
     * 说明：spring上下文容器，用于获取DDBMSBaseTransportProtocolDecoder实例
     * </pre>
     */
    private ApplicationContext ioc;

    /**
     * <pre>
     * 说明：初始化channel.pipeline中的处理器
     * 实现步骤：
     *   1) 加入DDBMSBaseTransportProtocolDecoder实例
     *   2) 加入protocolEncoder
     *   3) 加入protocolHandler
     *   4) 加入exceptionHandler
     * </pre>
     *
     * @param channel SocketChannel实例
     * @since 0.0.0
     */
    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().addLast(ioc.getBean(DDBMSBaseTransportProtocolDecoder.class));
        channel.pipeline().addLast(protocolEncoder);
        channel.pipeline().addLast(protocolHandler);
        channel.pipeline().addLast(exceptionHandler);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ioc = applicationContext;
    }
}

