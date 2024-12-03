package org.qh.DDBMS.common.decoder;

import com.qh.exception.MethodParameterException;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.URI;
import org.qh.tools.code.AESUtils;
import org.qh.tools.code.RSAUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 默认的解密(码)类
 */

public class DefaultDecoder extends AbstractDecoder {

    /**
     * <pre>
     * 说明：该方法负责对使用AES算法进行加密的数据进行解密
     * URI：AES
     * 实现步骤：
     *   1) 校验key是否符合规范，长度必须是16的倍数。
     *     1. key不合格则直接抛出异常。
     *   2) 调用AES工具类进行解密。
     *   3) 返回解密后的数据。
     * @param entity 解密实体
     * @return 解密后的字节数组
     * @since 0.0.0
     */
    @URI(Constant.Code.AES)
    protected byte[] aes(DecoderEntity entity) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (entity.getKey().length % 16 != 0) {
            throw new MethodParameterException("Key length must be a multiple of 16");
        }
        return AESUtils.decode(entity.getData(), entity.getKey());
    }

    /**
     * <pre>
     * 说明：该方法负责对使用RSA进行加密的数据进行解密
     * URI：RSA
     * 实现步骤：
     *   1) 调用RSA工具类进行解密。
     *   2) 返回解密后的数据。
     * @param entity 解密实体
     * @return 解密后的字节数组
     * @since 0.0.0
     */
    @URI(Constant.Code.RSA)
    protected byte[] rsa(DecoderEntity entity) throws Exception {
        byte[] decryptedData = RSAUtils.decodeByPrivateKey(entity.getData(), entity.getKey());
        return decryptedData;
    }
}

