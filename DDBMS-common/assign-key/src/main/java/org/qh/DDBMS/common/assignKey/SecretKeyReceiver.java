package org.qh.DDBMS.common.assignKey;

import com.qh.protocol.exception.ProtocolResolveException;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.decoder.Decoder;
import org.qh.DDBMS.common.decoder.DecoderEntity;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.input.SocketManager;
import org.qh.DDBMS.common.key.DefaultPublicKey;
import org.qh.DDBMS.common.key.DefaultSymmetricKey;
import org.qh.DDBMS.common.key.PublicKey;
import org.qh.DDBMS.common.key.SymmetricKey;
import org.qh.DDBMS.common.msk.SymmetricKeyManager;
import org.qh.DDBMS.common.protocol.ACKProtocol;
import org.qh.DDBMS.common.protocol.AssignKeyProtocol;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/23
 * @Version: 0.0.0
 * @Description: 密钥接收器
 */
public class SecretKeyReceiver implements DDBMSReceiver<BaseTransportProtocol, ChannelHandlerContext> {

    /**
     * <pre>
     * 说明：用于保存对称密钥
     * </pre>
     */
    @Resource
    private SymmetricKeyManager<String> keyManager;

    /**
     * <pre>
     * 说明：用于保存socket和站点的映射关系
     * </pre>
     */
    @Resource
    private SocketManager socketManager;

    /**
     * <pre>
     * 说明：用于获取，分发对称密钥的协议实例
     * </pre>
     */
    @Resource
    private KeyAssigner<String> keyAssign;

    /**
     * <pre>
     * 说明：用于对加密后的对称密钥进行解密
     * </pre>
     */
    @Resource
    private Decoder decoder;

    /**
     * <pre>
     * 说明：用于获取私钥
     * </pre>
     */
    @Resource
    private KeyConfig keyConfig;

    /**
     * <pre>
     * 说明：用于获取本站点名
     * </pre>
     */
    @Resource
    private ServerConfig serverConfig;

    /**
     * <pre>
     * 说明：处理接收到的密钥
     * 实现步骤：
     *   1) 判定协议类型不是密钥分发协议，返回
     *   2) 构建AssignKeyProtocol实例
     *   3) 保存socket和站点名的映射关系。
     *   4) 根据密钥类型分别执行handleSymmetricKey或者handlePublicKey方法
     * </pre>
     *
     * @param protocol 接收到的传输协议
     * @param ctx 上下文
     * @return 处理后的传输协议
     * @since 0.0.0
     */
    @Override
    public TransportProtocol receive(BaseTransportProtocol protocol, ChannelHandlerContext ctx) throws Exception {
        if (protocol.type() != Constant.Protocol.ASSIGN_KEY_PROTOCOL_TYPE) throw new ProtocolResolveException();
        AssignKeyProtocol akp = new AssignKeyProtocol(protocol.data());
        socketManager.register(ctx, akp.siteName());
        if (akp.keyType() == Constant.KeyType.SYMMETRIC_KEY) {
            handleSymmetricKey(new DefaultSymmetricKey(akp.siteName(), akp.key()), ctx);
        } else if (akp.keyType() == Constant.KeyType.PUBLIC_KEY) {
            handlePublicKey(new DefaultPublicKey(akp.siteName(), akp.key()), ctx);
        }
        return null;
    }

    /**
     * <pre>
     * 说明：处理接收到的对称密钥
     * 实现步骤：
     *   1) 将对称密钥进行解密
     *   2) 将对称密钥保存到keyManager中
     *   3) 判断client站点之前没有保存本密钥到本站点，则向client站点发送Ready协议
     * </pre>
     *
     * @param symmetricKey 接收到的对称密钥
     * @param ctx 通道上下文对象
     * @since 0.0.0
     */
    private void handleSymmetricKey(SymmetricKey<String> symmetricKey, ChannelHandlerContext ctx) {
        byte[] key = decoder.decode(new DecoderEntity(Constant.Code.RSA, keyConfig.privateKey(), symmetricKey.key()));
        byte[] older = keyManager.getKey(symmetricKey.owner());
        keyManager.registerKey(new DefaultSymmetricKey(symmetricKey.owner(), key));
        if (!Arrays.equals(key, older)) {
            ctx.writeAndFlush(new ACKProtocol(Constant.ACKType.READY, serverConfig.siteName()));
        }
    }

    /**
     * <pre>
     * 说明：处理接收到的公钥
     * 注意：该实现可能导致对称密钥被窃取
     * 实现步骤：
     *   1) 从keyAssign获取分发对称密钥协议
     *   2) 将协议发送出去
     *   3) 保存本站点对称密钥和目标站点的映射关系。因为对方是服务站点，所以用本站点的对称密钥进行通讯
     * </pre>
     *
     * @param publicKey 接收到的公钥
     * @param ctx 上下文
     * @since 0.0.0
     */
    private void handlePublicKey(PublicKey<String> publicKey, ChannelHandlerContext ctx) throws Exception {
        AssignKeyProtocol assignKeyProtocol = keyAssign.assignSymmetricKey(publicKey);
        ctx.writeAndFlush(assignKeyProtocol.data().array());
        keyManager.registerKey(new DefaultSymmetricKey(publicKey.owner(), keyConfig.symmetricKey()));
    }

}

