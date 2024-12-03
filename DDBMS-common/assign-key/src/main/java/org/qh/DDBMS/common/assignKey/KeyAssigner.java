package org.qh.DDBMS.common.assignKey;

import org.qh.DDBMS.common.key.PublicKey;
import org.qh.DDBMS.common.protocol.AssignKeyProtocol;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 该接口是当前模块的核心，提供本站点分发密钥的API
 */
public interface KeyAssigner<Q> {

    /**
     * <pre>
     * 说明：该方法用于分发当前站点的对称密钥给被连接的站点
     * (如：主站点和GDDBMS站点)
     * @param publicKey 要分发的公钥
     * @return AssignKeyProtocol 分发密钥的协议
     * @since 0.0.0
     */
    AssignKeyProtocol assignSymmetricKey(PublicKey<Q> publicKey) throws Exception;

    /**
     * <pre>
     * 说明：该方法用于分发当前站点的公钥给连接站点
     * @return AssignKeyProtocol 分发密钥的协议
     * @since 0.0.0
     */
    AssignKeyProtocol assignPublicKey() throws Exception;
}
