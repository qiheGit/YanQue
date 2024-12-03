package org.qh.DDBMS.common.assignKey;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version:
 * @Description:
 */

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.qh.tools.code.RSAUtils;

import java.util.List;
import java.util.UUID;

/**
 * DefaultKeyConfig
 * 说明：此类是KeyConfig接口的默认实现类
 */
public class DefaultKeyConfig implements KeyConfig {
    private byte[] privateKey; // 站点RSA私钥
    private byte[] publicKey;  // 站点RSA公钥
    private byte[] symmetricKey;  // 站点AES对称密钥

    public DefaultKeyConfig() throws Exception {
        init();
    }
    /**
     * <pre>
     * 说明：用于初始化各个密钥
     * 实现步骤：
     *   1) 初始化公钥和私钥
     *   2) 初始化对称密钥
     * @since 0.0.0
     */
    private void init() throws DecoderException {
        initRSAKey();
        initAESKey();
    }

    private void initAESKey() {
        symmetricKey = UUID.randomUUID().toString().replace("-", "").getBytes();
    }

    private void initRSAKey() throws DecoderException {
        List<String> keys = RSAUtils.generateKey();
        publicKey = Hex.decodeHex(keys.get(0));
        privateKey = Hex.decodeHex(keys.get(1));
    }

    @Override
    public byte[] privateKey() {
        return privateKey;
    }

    @Override
    public byte[] publicKey() {
        return publicKey;
    }

    @Override
    public byte[] symmetricKey() {
        return symmetricKey;
    }
}

