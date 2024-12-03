package org.qh.DDBMS.common.input.impl;

import com.qh.net.Receiver;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.decoder.Decoder;
import org.qh.DDBMS.common.decoder.DecoderEntity;
import org.qh.DDBMS.common.input.DDBMSProtocolDispatcher;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.input.SocketManager;
import org.qh.DDBMS.common.msk.SymmetricKeyManager;
import org.qh.DDBMS.common.protocol.ACKProtocol;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: ProtocolDispatcher的默认实现类
 */
public class DefaultProtocolDispatcher implements DDBMSProtocolDispatcher {

    /**
     * <pre>
     * 说明：指定对哪些类型的协议实例不进行解密
     * </pre>
     */
    private Set<Byte> ignoredType;

    /**
     * <pre>
     * 说明：协议类型与其对应的receiver之间的关系
     * </pre>
     */
    private Map<Byte, DDBMSReceiver> protocolReceiver = new ConcurrentHashMap<Byte, DDBMSReceiver>();

    /**
     * <pre>
     * 说明：用于对传输的数据内容进行解密，得到原始的传输协议内容
     * </pre>
     */
    @Resource
    private Decoder decoder;

    /**
     * <pre>
     * 说明：用于获取站点的对称密钥
     * </pre>
     */
    @Resource
    private SymmetricKeyManager<String> symmetricKeyManager;

    /**
     * <pre>
     * 说明：用于获取socket对应的站点名，得到原始的传输协议内容
     * </pre>
     */
    @Resource
    private SocketManager socketManager;

    public DefaultProtocolDispatcher(Byte... ignoredType) {
        this.ignoredType = new HashSet<>(Arrays.asList(ignoredType));
    }

    /**
     * <pre>
     * 说明：分发协议实例给到真正的处理器
     * 实现步骤：
     *   1) 获取协议类型对应的receiver
     *   2) 将加密后的协议实例进行解密得到明文的协议实例
     *   3) 执行receiver的receive方法，并获取其返回值res
     *   4) res不为null，则将其发送出去。
     *   5) 出现异常则将异常封装为一个ACK将异常信息返回。
     * </pre>
     *
     * @param protocol 协议实例
     * @param ctx ChannelHandlerContext 实例
     * @return TransportProtocol<ByteBuffer> 解密后的传输协议
     * @since 0.0.0
     */
    public TransportProtocol doDispatch(BaseTransportProtocol protocol, ChannelHandlerContext ctx) {
        byte protocolType = protocol.data()[0];
        if (!ignoredType.contains(protocolType)) {
            protocol = new BaseTransportProtocol(decoder.decode(new DecoderEntity(Constant.Code.AES,
                    symmetricKeyManager.getKey(socketManager.get(ctx)),
                    Arrays.copyOfRange(protocol.data(), Byte.BYTES + Long.BYTES, protocol.data().length))));
        }

        DDBMSReceiver ddbmsReceiver = protocolReceiver.get(protocolType);
        TransportProtocol result = null;
        try {
            if (ddbmsReceiver == null) return null;
            result = ddbmsReceiver.receive(protocol, ctx);
        } catch (Exception e) {
            result = new ACKProtocol(Constant.ACKType.EXCEPTION, e.getMessage());
            ctx.fireExceptionCaught(e);
        }

        if (result != null) ctx.write(result);
        return null; // 示例返回
    }

    /**
     * <pre>
     * 说明：注册处理相应类型协议实例的Receiver
     * 实现步骤：
     *   1) 将类型和receiver的映射关系注册到protocolReceiver中
     * </pre>
     *
     * @param type 协议类型
     * @param receiver 处理器
     * @since 0.0.0
     */
    @Override
    public void register(byte type, DDBMSReceiver<BaseTransportProtocol, ChannelHandlerContext> receiver) {
        protocolReceiver.put(type, receiver);
    }

    /**
     * <pre>
     * 说明：注销处理相应类型协议实例的Receiver
     * 实现步骤：
     *   1) 将协议类型和receiver的映射关系从protocolReceiver中移除
     * </pre>
     *
     * @param type 协议类型
     * @since 0.0.0
     */
    public void unregister(byte type) {
        protocolReceiver.remove(type);
    }
}
