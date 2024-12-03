package org.qh.DDBMS.common.encoder;

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
 * @Description: 默认的加密类
 */
public class DefaultEncoder extends AbstractEncoder {

    /**
     * <pre>
     * 说明：该方法负责对数据使用AES算法进行加密。
     * 实现步骤：
     *   1) 校验key是否符合规范，长度必须是16的倍数。
     *      1. key不合格则直接抛出异常。
     *   2) 调用AES工具类进行加密。
     *   3) 返回加密后的数据。
     * @param entity 需要加密的EncodeEntity
     * @return byte[] 加密后的数据
     * @throws MethodParameterException 如果key不合格
     */
    @URI(Constant.Code.AES)
    protected byte[] aes(EncodeEntity entity) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        byte[] key = entity.getKey();
        if (key.length % 16 != 0) {
            throw new MethodParameterException("Key length must be a multiple of 16.");
        }
        return AESUtils.encode(entity.getData(), key);
    }

    /**
     * <pre>
     * 说明：该方法负责对数据使用RSA算法进行加密。
     * 实现步骤：
     *   1) 调用RSA工具类进行加密。
     *   2) 返回加密后的数据。
     * @param entity 需要加密的EncodeEntity
     * @return byte[] 加密后的数据
     */
    @URI(Constant.Code.RSA)
    protected byte[] rsa(EncodeEntity entity) throws Exception {
        // 调用RSA工具类进行加密
        return RSAUtils.encodeByPublicKey(entity.getData(), entity.getKey());
    }

}

