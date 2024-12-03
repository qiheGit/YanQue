package org.qh.DDBMS.common.encoder;

import com.qh.exception.ClassFieldException;
import lombok.Getter;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.str.StringUtils;

import javax.xml.bind.ValidationException;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/18
 * @Version: 0.0.0
 * @Description: 该类作为数据加密的参数类
 */
@Getter
public class EncodeEntity implements Validator<EncodeEntity, String> {
    // 属性
    private String algorithm; // 指定data属性的加密算法
    private byte[] key;       // 指定加密所需的密钥
    private byte[] data;      // 需要被加密的数据

    /**
     * <pre>
     * 说明：该类的全参构造器。
     * 实现步骤：
     *   1) 为每个属性赋值。
     *   2) 调用validate方法验证参数的合法性。
     *      1. 非法：抛出异常。
     * @param algorithm 指定的加密算法
     * @param key 加密所需的密钥
     * @param data 需要被加密的数据
     * @since 0.0.0
     */
    public EncodeEntity(String algorithm, byte[] key, byte[] data) {
        this.algorithm = algorithm;
        this.key = key;
        this.data = data;
        String validate = validate(this);// 验证参数合法性
        if (validate != null) throw new ClassFieldException(validate);
    }

    /**
     * <pre>
     * 说明：验证当前实例对象的属性是否满足规范。
     * 规范：
     *   1) algorithm：不能为空。
     *   2) key：不能为空。
     *   3) data：不能为空。
     * @return
     * @since 0.0.0
     */
    @Override
    public String validate(EncodeEntity encodeEntity) {
        if (StringUtils.isEmpty(algorithm)) {
            return "The field algorithm cannot be null or empty";
        }
        if (key == null || key.length == 0) {
            return "The field key cannot be null or empty";
        }
        if (data == null || data.length == 0) {
            return "The field data cannot be null or empty";
        }
        return null;
    }

    // Getter methods
    /**
     * <pre>
     * 说明：获取加密算法
     * @return algorithm 加密算法
     * @since 0.0.0
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * <pre>
     * 说明：获取加密密钥
     * @return key 加密密钥
     * @since 0.0.0
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * <pre>
     * 说明：获取需要加密的数据
     * @return data 需要加密的数据
     * @since 0.0.0
     */
    public byte[] getData() {
        return data;
    }
}

