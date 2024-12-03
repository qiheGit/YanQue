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
 * @Description: 默认实现的公钥对象
 */
public class DefaultPublicKey implements PublicKey<String>, Validator<DefaultPublicKey, String> , Serializable {

    private static final long serialVersionUID = 1L;

    // 属性
    private String siteName; // 公钥站点的socketId值，通过该id可以找到唯一对应的socket
    private byte[] key;    // 公钥

    /**
     * <pre>
     * 说明：此类的全参构造器
     * 实现步骤：
     *   1) 为属性赋值
     *   2) 将当前实例作为参数执行validate()
     *     1. validate返回非null，抛出字段异常
     * @param siteName 和公钥站点连接的socketId
     * @param key 公钥
     * @throws ClassFieldException 验证失败
     * @since 0.0.0
     */
    public DefaultPublicKey(String siteName, byte[] key) {
        this.siteName = siteName;
        this.key = key;
        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：校验传入参数的属性是否合规
     * 实现步骤：
     *   1) 判定socketId == NULL || socketId < 1则返回
     *   "The socketId field is value(invalid)."
     *   2) 判定key为空，则返回
     *   "The key field is empty."
     * @param publicKey 要验证的对象
     * @return 错误信息，如果合法则返回null
     * @since 0.0.0
     */
    @Override
    public String validate(DefaultPublicKey publicKey) {
        if (StringUtils.isEmpty(publicKey.siteName)) {
            return "The socketId field is " + publicKey.siteName + " (invalid).";
        }
        if (publicKey.key == null || publicKey.key.length == 0) {
            return "The key field is empty.";
        }
        return null;
    }

    /**
     * <pre>
     * 说明：获取该公钥的拥有者
     * @return socketId 拥有者的socketId
     * @since 0.0.0
     */
    @Override
    public String owner() {
        return siteName;
    }

    /**
     * <pre>
     * 说明：获取公钥
     * @return byte[] 公钥的字节数组
     * @since 0.0.0
     */
    @Override
    public byte[] key() {
        return key;
    }
}

