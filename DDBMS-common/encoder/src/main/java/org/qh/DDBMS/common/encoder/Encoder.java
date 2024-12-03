package org.qh.DDBMS.common.encoder;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 加密模块的顶级接口，用于对数据进行加密。
 */
public interface Encoder {
    /**
     * <pre>
     * 说明：根据传入的EncodeEntity返回加密后的数据。
     * @param encodeEntity 需要加密的数据实体
     * @return byte[] 加密后的数据
     * @throws Exception 如果加密过程中发生错误
     */
    byte[] encode(EncodeEntity encodeEntity) throws Exception;
}

