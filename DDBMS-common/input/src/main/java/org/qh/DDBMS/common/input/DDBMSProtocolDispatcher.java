package org.qh.DDBMS.common.input;

import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: 协议分发器接口，用于分发接收的协议实例
 */
public interface DDBMSProtocolDispatcher extends DDBMSReceiver<BaseTransportProtocol, ChannelHandlerContext> {

    /**
     * <pre>
     * 说明：
     *   1) 接收BaseTransportProtocol实例类型，并进行处理
     *   2) 该方法是default方法。
     * </pre>
     * @param protocol BaseTransportProtocol 接收到的协议
     * @param ctx ChannelHandlerContext 上下文
     * @return TransportProtocol 处理后的协议
     * @since 0.0.0
     */
    default TransportProtocol receive(BaseTransportProtocol protocol, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException {
        // 实现逻辑
        return doDispatch(protocol, ctx);
    }

    /**
     * <pre>
     * 说明：分发协议实例给到真正的处理器
     * </pre>
     * @param protocol BaseTransportProtocol 要分发的协议
     * @param ctx ChannelHandlerContext 上下文
     * @return TransportProtocol&lt;ByteBuffer&gt; 分发后的协议
     * @since 0.0.0
     */
    TransportProtocol doDispatch(BaseTransportProtocol protocol, ChannelHandlerContext ctx) throws InvocationTargetException, IllegalAccessException;

    /**
     * <pre>
     * 说明：注册处理相应类型协议实例的Receiver
     * </pre>
     * @param type byte 协议类型
     * @param receiver Receiver&lt;BaseTransportProtocol, ChannelHandlerContext&gt; 处理器
     * @since 0.0.0
     */
    void register(byte type, DDBMSReceiver<BaseTransportProtocol, ChannelHandlerContext> receiver);

    /**
     * <pre>
     * 说明：注销处理相应类型协议实例的Receiver
     * </pre>
     * @param type byte 协议类型
     * @since 0.0.0
     */
    void unregister(byte type);
}
