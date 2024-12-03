package org.qh.DDBMS.common.key;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 该接口为公钥接口，描述了作为一个公钥对象应有的基本操作
 */
public interface PublicKey<Q> {

    /**
     * <pre>
     * 说明：获取该公钥的拥有者
     * @return 拥有者
     * @since 0.0.0
     */
    Q owner();

    /**
     * <pre>
     * 说明：获取公钥
     * @return byte[] 公钥的字节数组
     * @since 0.0.0
     */
    byte[] key();
}
