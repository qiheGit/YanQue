package org.qh.DDBMS.common.decoder;

import com.qh.exception.ClassFieldException;
import lombok.Getter;
import org.qh.DDBMS.common.Validator;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该类作为数据解密的参数类。
 */
@Getter
public class DecoderEntity implements Validator<DecoderEntity, String> {
    // 指定data属性的加密算法
    private String algorithm; // algorithm: String

    // 指定解密所需要使用的密钥
    private byte[] key; // key: byte[]

    // 需要被解密的数据
    private byte[] data; // data: byte[]

    /**
     * <pre>
     * 说明：该类的全参构造器。
     * 实现步骤：
     *   1) 为每个属性赋值。
     *   2) 调用valid方法验证参数的合法性
     *     1. 非法：抛出异常
     * @param algorithm
     * @param key
     * @param data
     */
    public DecoderEntity(String algorithm, byte[] key, byte[] data) {
        this.algorithm = algorithm;
        this.key = key;
        this.data = data;
        String validate = validate(this);
        if (validate != null) {
            throw new ClassFieldException(validate);
        }
    }
    /**
     * <pre>
     * 说明：验证当前实例对象的属性是否满足规范
     * 规范：
     *   1) algorithm：不能为空
     *   2) key: 不能为空
     *   3) data: 不能为空
     * @return 异常信息
     * @since 0.0.0
     */
    @Override
    public String validate(DecoderEntity decoderEntity) {
        if (algorithm == null) return "The field algorithm is null";
        if (key == null) return "The field key is null";
        if (data == null) return "The field data is null";
        return null;
    }
}
