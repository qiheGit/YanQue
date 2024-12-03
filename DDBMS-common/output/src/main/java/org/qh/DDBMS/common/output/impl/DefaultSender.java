package org.qh.DDBMS.common.output.impl;

import com.qh.protocol.net.CallbackProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.input.SocketManager;
import org.qh.DDBMS.common.output.CallbackReceiverManager;
import org.qh.DDBMS.common.output.DDBMSSender;

import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/17
 * @Version: 0.0.0
 * @Description: DBMSSender<String>的默认实现类
 */

public class DefaultSender implements DDBMSSender<String> {

    /**
     * <pre>
     * 说明：用于注册回调方法的临时receiver
     * </pre>
     */
    @Resource
    private CallbackReceiverManager receiverManager;

    /**
     * <pre>
     * 说明：用于获取目标站点的socket
     * </pre>
     */
    @Resource
    private SocketManager socketManager;

    /**
     * <pre>
     * 说明：发送协议实例的目标方法
     * 实现步骤：
     *   1) 从socketManager中获取相应站点的socket
     *   2) 判定当前协议是回调协议，并传入的receiver不是null，则调用receiverManager的temporary方法
     *   3) 将协议实例通过获得到的socket发送出去
     * </pre>
     * @param siteName 站点名
     * @param protocol TransportProtocol 发送的协议实例
     * @param receiver DDBMSReceiver&lt;Q, ?&gt; 接收者
     * @param sync 发送时是否阻塞
     * @since 0.0.0
     */
    @Override
    public void send(String siteName, TransportProtocol protocol, DDBMSReceiver receiver, boolean sync)  {
        ChannelHandlerContext context = socketManager.get(siteName);
        if (context == null) throw new RuntimeException("Context Not Found!");
        if (protocol instanceof CallbackProtocol && receiver != null) {
            receiverManager.temporary(((CallbackProtocol) protocol).key(), receiver);
        }
        try {
            ChannelFuture channelFuture = context.writeAndFlush(protocol);
            if (sync) channelFuture.sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
