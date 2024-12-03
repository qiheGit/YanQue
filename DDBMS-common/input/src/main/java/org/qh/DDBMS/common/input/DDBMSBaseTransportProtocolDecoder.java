package org.qh.DDBMS.common.input;

import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.assignKey.KeyAssigner;
import org.qh.tools.netty.handler.BaseTransportProtocolDecoder;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;


/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: DDBMS中的基本协议解码器
 */
@Scope(value = "prototype")
public class DDBMSBaseTransportProtocolDecoder extends BaseTransportProtocolDecoder {

    /**
     * <pre>
     * 说明：Socket管理器
     * </pre>
     */
    @Resource
    private SocketManager manager;

    /**
     * <pre>
     * 说明：密钥分配器
     * </pre>
     */
    @Resource
    private KeyAssigner keyAssigner;

    public DDBMSBaseTransportProtocolDecoder(long maxContentLength, Byte... protocolTypes) {
        super(maxContentLength, protocolTypes);
    }

    /**
     * <pre>
     * 说明：
     *   1) 该方法在连接准备完成后执行。
     *   2) 分发当前站点的公钥给client
     * 实现步骤：
     *   1) 执行keyAssigner分发公钥方法，得到分发密钥协议
     *   2) 将协议发送给client
     * </pre>
     *
     * @param ctx ChannelHandlerContext 实例
     * @since 0.0.0
     */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write(keyAssigner.assignPublicKey().data().array());
    }

    /**
     * <pre>
     * 说明：
     *   1) 该方法在连接的生命周期结束后被执行
     *   2) 将当前连接从SocketManager中移除
     * 实现步骤：
     *   1) 调用SocketManager的remove方法删除当前socket
     * </pre>
     *
     * @param ctx ChannelHandlerContext 实例
     * @since 0.0.0
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        manager.remove(ctx);
    }
}

