package org.qh.DDBMS.common.input;

import com.qh.protocol.net.BaseTransportProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: DDBMS的协议处理器，对接受到的协议内容进行处理
 */
@ChannelHandler.Sharable
public class DDBMSProtocolHandler extends SimpleChannelInboundHandler<BaseTransportProtocol> {

    /**
     * <pre>
     * 说明：协议分发器Receiver
     * </pre>
     */
    @Resource(name = "org.qh.DDBMS.common.input.impl.DefaultProtocolDispatcher")
    private DDBMSProtocolDispatcher dispatcher;

    /**
     * <pre>
     * 说明：对接收到的协议实例进行处理
     * 实现步骤：
     *   1) 执行dispatcher的receive方法
     * </pre>
     *
     * @param protocol 接收到的协议实例
     * @since 0.0.0
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseTransportProtocol protocol) throws InvocationTargetException, IllegalAccessException {
        dispatcher.receive(protocol, ctx);
    }
}

