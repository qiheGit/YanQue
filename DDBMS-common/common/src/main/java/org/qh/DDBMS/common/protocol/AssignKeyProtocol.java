package org.qh.DDBMS.common.protocol;

import com.qh.exception.ClassFieldException;
import com.qh.protocol.net.AbstractTransferProtocol;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.str.StringUtils;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/20
 * @Version: 0.0.0
 * @Description: 用于分发密钥的协议
 */

// 说明：用于分发密钥的协议
public class AssignKeyProtocol extends AbstractTransferProtocol<ByteBuffer> implements Validator<AssignKeyProtocol, String> , Serializable {

    private static final long serialVersionUID = 1L;
    // 属性
    private byte keyType; // 密钥类型: 0表示对称密钥，1表示公钥

    private String siteName; // 密钥所属的站点名
    private byte[] key;   // 密钥

    /**
     * <pre>
     * 说明：此类的全参构造器
     * 实现步骤：
     *   1) 为keyType和key赋值
     *   2) 为contentLength赋值
     *   3) 将自身当作参数调用validate方法
     *     1. validate()返回非null，抛出参数异常
     * @param keyType 密钥类型
     * @param key 密钥
     * @throws ClassFieldException 如果验证失败
     * @since 0.0.0
     */
    public AssignKeyProtocol(byte keyType, String siteName, byte[] key) {
        super(Constant.Protocol.ASSIGN_KEY_PROTOCOL_TYPE);
        this.keyType = keyType;
        this.siteName = siteName;
        this.key = key;
        setContentLength(Byte.BYTES + Integer.BYTES + siteName.getBytes().length
                + Integer.BYTES + key.length);

        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：根据传入数据还原该类实例的构造器
     * 实现步骤：
     *   1) 根据传入数据构建一个ByteBuffer实例
     *   2) 取出type
     *   3) 取出contentLength
     *   4) 取出keyType
     *   5) 取出siteNameLength
     *   6) 取出siteName
     *   7) 取出keyLength
     *   8) 根据keyLength取出key
     *   9) 将当前实例作为参数调用validate(),
     *     1. validate返回非null，抛出字段异常
     * @param data 输入的字节数组
     * @throws ClassFieldException 如果验证失败
     * @since 0.0.0
     */
    public AssignKeyProtocol(byte[] data) {
        super(Constant.Protocol.ASSIGN_KEY_PROTOCOL_TYPE);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.get(); // 取出type
        setContentLength(buffer.getLong());
        this.keyType = buffer.get();

        int siteNameLength = buffer.getInt();
        this.siteName = new String(buffer.array(), buffer.position(), siteNameLength);
        buffer.position(buffer.position() + siteNameLength);

        int keyLength = buffer.getInt();
        this.key = new byte[keyLength];
        buffer.get(this.key);

        String validationError = validate(this);
        if (validationError != null) {
            throw new ClassFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：获取keyType值
     * @return keyType 密钥类型
     * @since 0.0.0
     */
    public byte keyType() {
        return keyType;
    }

    public String siteName() {
        return siteName;
    }

    /**
     * <pre>
     * 说明：获取key
     * @return key 密钥
     * @since 0.0.0
     */
    public byte[] key() {
        return key;
    }

    /**
     * <pre>
     * 说明：验证传入参数的属性是否合规
     * 实现步骤：
     *   1) 判定keyType非0也非1，则返回
     *   "The keyType field is value(invalid)."
     *   2) 判定key为空，则抛出参数异常
     *   "The key field is null."
     * @param protocol 要验证的对象
     * @return 错误信息，如果合法则返回null
     * @since 0.0.0
     */
    @Override
    public String validate(AssignKeyProtocol protocol) {
        if (protocol.keyType != Constant.KeyType.PUBLIC_KEY && protocol.keyType != Constant.KeyType.SYMMETRIC_KEY) {
            return "The keyType field is " + protocol.keyType + "(invalid).";
        }
        if (StringUtils.isEmpty(protocol.siteName)) {
            return "The siteName field is " + protocol.siteName + "(invalid).";
        }
        if (protocol.key == null || protocol.key.length == 0) {
            throw new IllegalArgumentException("The key field is null.");
        }
        return null; // 合法
    }

    /**
     * <pre>
     * 说明：返回该协议字节数据形式
     * 实现步骤：
     *   1) 计算contentLength
     *   2) 构建ByteBuffer实例buf
     *   3) buf装入type
     *   4) buf装入contentLength
     *   5) buf装入keyType
     *   6) buf装入siteNameLength
     *   7) buf装入siteName
     *   8) buf装入keyLength
     *   9) buf装入key
     * @return ByteBuffer 协议的字节数据
     * @since 0.0.0
     */
    @Override
    public ByteBuffer data() {
        ByteBuffer buf = ByteBuffer.allocate((int) (1 + 8 + contentLength()));
        buf.put(type()); // buf装入type
        buf.putLong(contentLength()); // buf装入contentLength
        byte[] siteName = this.siteName.getBytes();
        buf.putInt(siteName.length);
        buf.put(siteName);
        buf.put(keyType); // buf装入keyType
        buf.putInt(key.length); // buf装入keyLength
        buf.put(key); // buf装入key
        return buf;
    }

    @Override
    public String tempFile() {
        return "";
    }
}

