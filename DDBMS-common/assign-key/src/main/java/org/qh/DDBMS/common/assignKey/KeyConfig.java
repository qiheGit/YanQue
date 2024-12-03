package org.qh.DDBMS.common.assignKey;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 密钥配置类
 */
public interface KeyConfig {
    /**
     * <pre>
     * 说明：该方法返回本站点的RSA算法私钥
     * @return RSA私钥
     * @since 0.0.0
     */
    byte[] privateKey();

    /**
     * <pre>
     * 说明：该方法返回本站点的RSA算法公钥
     * @return RSA公钥
     * @since 0.0.0
     */
    byte[] publicKey();

    /**
     * <pre>
     * 说明：该方法返回本站点的AES算法密钥
     * @return AES密钥
     * @since 0.0.0
     */
    byte[] symmetricKey();
}

