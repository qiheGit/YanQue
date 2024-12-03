package org.qh.DDBMS.common.key;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 对称密钥接口
 */
public interface SymmetricKey<Q> {

    /**
     * <pre>
     * 说明：该方法返回该密钥的持有者
     * @return 密钥持有者
     * @since 0.0.0
     */
    Q owner();

    /**
     * <pre>
     * 说明：该方法返回对称密钥
     * @return 对称密钥
     * @since 0.0.0
     */
    byte[] key();
}

