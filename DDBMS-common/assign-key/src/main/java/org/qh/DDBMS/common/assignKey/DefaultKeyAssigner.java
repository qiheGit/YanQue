package org.qh.DDBMS.common.assignKey;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.encoder.EncodeEntity;
import org.qh.DDBMS.common.encoder.Encoder;
import org.qh.DDBMS.common.input.ServerConfig;
import org.qh.DDBMS.common.key.PublicKey;
import org.qh.DDBMS.common.protocol.AssignKeyProtocol;

import javax.annotation.Resource;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 说明：该类是KeyAssigner接口的默认实现类
 */
public class DefaultKeyAssigner implements KeyAssigner<Long> {

    // 属性：
    @Resource
    private KeyConfig keyConf;  // 说明：密钥配置类实例，用于获取配置的密钥
    @Resource
    private Encoder encoder;    // 说明：加密API提供接口，用于对分发的密钥进行加密

    @Resource
    private ServerConfig serverConfig; // 说明：获取本站点名

    // 方法：

    /**
     * <pre>
     * 说明：该方法用于分发当前站点的对称密钥给到，被连接的站点
     * 实现步骤：
     *   1) 从keyConf中获取本站点的对称密钥key
     *   2) 将key用接受到的公钥进行加密
     *   3) 构建AssignKeyProtocol实例类型AKP
     *   4) 返回AKP实例
     * @param publicKey 要分发的公钥
     * @return AssignKeyProtocol 分发密钥的协议
     * @since 0.0.0
     */
    @Override
    public AssignKeyProtocol assignSymmetricKey(PublicKey<Long> publicKey) throws Exception {
        byte[] key = encoder.encode(new EncodeEntity(Constant.Code.RSA,
                publicKey.key(), keyConf.symmetricKey()));
        AssignKeyProtocol akp =
                new AssignKeyProtocol(Constant.KeyType.SYMMETRIC_KEY, serverConfig.siteName(), key);
        return akp;
    }

    /**
     * <pre>
     * 说明：该方法用于分发当前站点的公钥给连接站点
     * 实现步骤：
     *   1) 从keyConf获取本站点的公钥key
     *   2) 将key信息封装为AssignKeyProtocol实例AKP
     *   3) 将AKP对象返回
     * @return AssignKeyProtocol 分发密钥的协议
     * @since 0.0.0
     */
    @Override
    public AssignKeyProtocol assignPublicKey() throws Exception {
        return new AssignKeyProtocol(Constant.KeyType.PUBLIC_KEY, serverConfig.siteName(), keyConf.publicKey());
    }
}

