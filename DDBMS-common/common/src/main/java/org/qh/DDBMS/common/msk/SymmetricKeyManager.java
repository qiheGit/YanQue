package org.qh.DDBMS.common.msk;

import org.qh.DDBMS.common.key.SymmetricKey;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 管理对称密钥的接口
 */
public interface SymmetricKeyManager<Q> {

    /**
     * <pre>
     * 说明：注册对称密钥到该管理器中
     * @param key 要注册的对称密钥
     * @since 0.0.0
     */
    void registerKey(SymmetricKey<Q> key);

    /**
     * <pre>
     * 说明：注销传入所有者对应的对称密钥
     * @param owner 所有者的标识
     * @return 注销的对称密钥
     * @since 0.0.0
     */
    SymmetricKey<Q> unregisterKey(Q owner);

    /**
     * <pre>
     * 说明：获取所有者对应的对称密钥
     * @param owner 所有者的标识
     * @return 对称密钥的字节数组
     * @since 0.0.0
     */
    byte[] getKey(Q owner);
}
