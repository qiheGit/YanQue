package org.qh.DDBMS.common.key;

import com.qh.exception.ClassFieldException;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.str.StringUtils;

import java.io.Serializable;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 该类是SymmetricKey接口的默认实现
 */
public class DefaultSymmetricKey implements SymmetricKey<String>, Validator<DefaultSymmetricKey, String> , Serializable {

    private static final long serialVersionUID = 1L;

    private String siteName; // 密钥对应的站点名
    private byte[] key;    //  该属性则是socket的对称密钥

    /**
     * <pre>
     * 说明：该类的全参构造器
     * @param siteName 密钥对应的站点名
     * @param key 对称密钥
     * @throws ClassFieldException 如果验证失败
     * @since 0.0.0
     */
    public DefaultSymmetricKey(String siteName, byte[] key) {
        this.siteName = siteName;
        this.key = key;
        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：返回该密钥的持有者（socket ID）
     * 实现步骤：返回socketId
     * @return socketId 持有者的ID
     * @since 0.0.0
     */
    @Override
    public String owner() {
        return siteName;
    }

    /**
     * <pre>
     * 说明：返回对称密钥的字节数组
     * 实现步骤：
     *   1) 为socketId和key赋值
     *   2) 将当前对象作为参数调用validate()进行验证
     *     1. validate()返回非null，抛出字段异常
     * @return key 对称密钥的字节数组
     * @since 0.0.0
     */
    @Override
    public byte[] key() {
        return key;
    }

    /**
     * <pre>
     * 说明：验证传入参数的合法性
     * 实现步骤：
     *   1) 判定siteName为空，则返回
     *   "The socketId filed is value(invalid)"。
     *   2) 判定key为空或key.length不是16的倍数，则返回
     *   "The key filed is value(invalid)"。
     * @param defaultSymmetricKey 要验证的对象
     * @return 错误信息，如果合法则返回null
     * @since 0.0.0
     */
    @Override
    public String validate(DefaultSymmetricKey defaultSymmetricKey) {
        if (StringUtils.isEmpty(defaultSymmetricKey.siteName)) {
            return "The field socketId is " + defaultSymmetricKey.siteName + "(invalid)";
        }
        if (defaultSymmetricKey.key == null || defaultSymmetricKey.key.length % 16 != 0) {
            return "The field key is " + new String(defaultSymmetricKey.key) + "(invalid)";
        }
        return null;
    }
}

