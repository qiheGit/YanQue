package org.qh.DDBMS.common.input;

import com.qh.protocol.net.TransportProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.encoder.EncodeEntity;
import org.qh.DDBMS.common.encoder.Encoder;
import org.qh.DDBMS.common.msk.SymmetricKeyManager;

import javax.annotation.Resource;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: 对需要进行网络传输的协议实例进行加密处理
 */
@ChannelHandler.Sharable
public class DDBMSProtocolEncoder extends MessageToByteEncoder<TransportProtocol<ByteBuffer>> {

    /**
     * <pre>
     * 说明：用于保存忽略哪些类型的协议
     * </pre>
     */
    private Set<Byte> ignoredType;

    /**
     * <pre>
     * 说明：用于获取当前socket的目标站点名
     * </pre>
     */
    @Resource
    private SocketManager socketManager;

    /**
     * <pre>
     * 说明：用于获取各个目标站点的对称密钥
     * </pre>
     */
    @Resource
    private SymmetricKeyManager<String> keyManager;


    /**
     * <pre>
     * 说明：用于对传输数据进行加密
     * </pre>
     */
    @Resource
    private Encoder encoder;

    public DDBMSProtocolEncoder(Byte... ignoredType) {
        this.ignoredType = new HashSet<Byte>(Arrays.asList(ignoredType));
    }

    /**
     * <pre>
     * 说明：对传输协议实例进行加密，并进行输出
     * 实现步骤：
     *   1) 判定该协议实例不用进行加密，直接输出协议实例
     *   2) out装入协议的类型
     *   3) 获取密钥，并对整个协议内容进行加密得到密文
     *   4) 将ciphertext的长度以long的形式装入out
     *   5) 将ciphertext装入out
     * </pre>
     *
     * @param ctx ChannelHandlerContext 实例
     * @param protocol 传输协议实例
     * @param out 输出的ByteBuf
     * @since 0.0.0
     */
    public void encode(ChannelHandlerContext ctx, TransportProtocol<ByteBuffer> protocol, ByteBuf out) throws Exception {
        byte[] p = protocol.data().array();
        if (ignoredType.contains(p[0])) out.writeBytes(p);
        else {
            out.writeByte(p[0]);
            byte[] encode = encoder.encode(new EncodeEntity(Constant.Code.AES, keyManager.getKey(socketManager.get(ctx)), p));
            out.writeLong(encode.length);
            out.writeBytes(encode);
        }

    }
}

