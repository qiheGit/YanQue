package org.qh.DDBMS.common.decoder;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 解密模块的顶级接口，用于对被加密的数据进行解密。
 */
public interface Decoder {
    /**
     * 说明：根据传入的DecryptEntity返回解密后的数据
     * @param entity 解码参数容器实例
     * @return 解码后的数据
     * @since 0.0.0
     */
    public byte[] decode(DecoderEntity entity);
}
