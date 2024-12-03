package org.qh.DDBMS.common.protocol;

import com.qh.exception.ClassFieldException;
import com.qh.protocol.exception.ProtocolFieldException;
import com.qh.protocol.exception.ProtocolResolveException;
import com.qh.protocol.net.AbstractCallbackProtocol;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.parse.ObjectByteParseUtils;
import org.qh.tools.str.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/24
 * @Version: 0.0.0
 * @Description: 该类描述了调用者执行，LDBMS中方法的协议。
 */
public class CallMethodProtocol extends AbstractCallbackProtocol<ByteBuffer> implements Validator<CallMethodProtocol, String> , Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 说明：指定方法的uri
     */
    private String uri;

    /**
     * 说明：执行目标方法所需要传入的参数
     */
    private Object[] args;


    /**
     * <pre>
     * 说明：该类的全参构造器
     * 实现步骤：
     *   1) 为属性赋值
     *   2) 将当前实例作为参数调用validate()
     *      1. validate()返回非null，抛出异常
     * </pre>
     * @param callBackKey 回调key
     * @param uri  方法的URI
     * @param args 执行目标方法所需的参数
     * @since 0.0.0
     */
    public CallMethodProtocol(long callBackKey, String uri, Object[] args) {
        super(Constant.Protocol.CALL_METHOD_PROTOCOL_TYPE);
        this.uri = uri;
        this.args = args;
        this.key(callBackKey);
        String validate = validate(this);
        if (validate != null) {
            throw new ClassFieldException(validate);
        }
    }

    /**
     * <pre>
     * 说明：将传入的数据解析为一个CallMethodProtocol的构造器
     * 实现步骤：
     *   1) 调用initProtocol()进行属性的初始化
     *   2) 将当前实例作为参数调用validate()
     *      1. validate()返回非null，抛出异常
     * </pre>
     * @param data 输入的字节数组
     * @since 0.0.0
     */
    public CallMethodProtocol(byte[] data) {
        super(Constant.Protocol.CALL_METHOD_PROTOCOL_TYPE);
        initProtocol(data);
        String validate = validate(this);
        if (validate != null) {
            throw new ProtocolFieldException(validate);
        }
    }

    /**
     * <pre>
     * 说明：根据传入的数据初始化当前实例的属性值
     * 实现步骤：
     *   1) 解析出协议类型
     *   2) 解析出内容长度
     *   3) 解析出callbackKey
     *   4) 解析出uri的字节数
     *   5) 解析出uri
     *   6) 解析出args的个数
     *   7) 循环解析args
     *     1. 解析出arg的字节数
     *     2. 解析出arg
     * </pre>
     * @param data 输入的字节数组
     * @since 0.0.0
     */
    private void initProtocol(byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.get();
            setContentLength(buffer.getLong());
            key(buffer.getLong());
            resolveUri(buffer);
            resolveArgs(buffer);
        } catch (Exception e) {
            throw new ProtocolResolveException(e);
        }
        String validate = validate(this);
        if (validate != null) {
            throw new ProtocolFieldException(validate);
        }

    }

    /**
     * <pre>
     * 说明：解析出当前协议中的uri
     * 实现步骤：
     *   1. 解析出uri的长度
     *   2. 解析出uri
     *   3. 将uri转为String，并为字段赋值
     * </pre>
     * @param buffer 封装了协议数据的buffer
     * @since 0.0.0
     */
    private void resolveUri(ByteBuffer buffer) {
        int uriLength = buffer.getInt();
        byte[] bytes = new byte[uriLength];
        buffer.get(bytes);
        uri = new String(bytes);
    }

    /**
     * <pre>
     * 说明：解析出当前协议中的参数
     * 实现步骤：
     *   1) 解析出args的个数
     *   2) 循环解析args
     *      1. 解析出arg的字节数
     *      2. 解析出arg
     * </pre>
     * @param buffer
     * @since 0.0.0
     */
    private void resolveArgs(ByteBuffer buffer) throws IOException {
        int argc = buffer.get();
        byte[]  arg = null;

        this.args = new Object[argc];
        for (int i = 0; i < argc; i++) {
            arg = new byte[ buffer.getInt()];
            this.args[i] = ObjectByteParseUtils.parseByteToObject(arg);
        }

    }


    /**
     * <pre>
     * 说明：验证传入的参数是否合规
     * 实现步骤：
     *   1) 判定type!=37,则返回类型不正确的参数异常
     *   2) 判定uri为空，则返回uri不正确的异常
     * </pre>
     * @param protocol 要验证的CallMethodProtocol实例
     * @return 结果字符串
     * @since 0.0.0
     */
    @Override
    public String validate(CallMethodProtocol protocol) {
        if (protocol.type() != Constant.Protocol.CALL_METHOD_PROTOCOL_TYPE) {
            return "Invalid protocol type(" + protocol.type() + ")";
        }
        if (StringUtils.isEmpty(protocol.uri)) {
            return "Invalid uri(" + protocol.uri + ")";
        }
        return null;
    }

    /**
     * <pre>
     * 说明：该方法将当前协议实例转化为字节形式
     * 实现步骤：
     *   1) 将该协议字段封装为字节二维数组
     *   2) 计算并设置当前协议内容长度
     *   3) 构建一个ByteBuffer，buf
     *   4) 将type装入buf
     *   5) 将协议内容字节长度装入buf
     *   6) 将callbackKey装入buf
     *   7) args的个数装入buf
     *   8) 遍历args，将每个args装入buf
     *     1. 将当前参数的字节形式长度装入buf
     *     2. 将当前参数的字节形式装入buf
     *   9) 返回buf
     * </pre>
     * @return ByteBuffer 转化后的数据
     * @since 0.0.0
     */
    public ByteBuffer data() {
        ByteBuffer buffer = null;
        try {
            byte[][] fields = fields();
            long contentLength = calContentLength(fields);
            buffer = ByteBuffer.allocate(1 + 8 + (int) contentLength);
            buffer.put(type());
            buffer.putLong(contentLength);
            buffer.putLong(key());
            buffer.put((byte) args.length);
            putField(buffer, fields);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    /**
     * <pre>
     * 说明：将args装入到buffer中
     * 实现步骤：
     *   1. 遍历args
     *     1.1. 将当前参数的字节形式长度装入buf
     *     1.2. 将当前参数的字节形式装入buf
     * </pre>
     * @param buffer 当前协议实例的字节buffer
     * @param fields 当前协议实例字段的二进制数组表示形式
     * @since 0.0.0
     */
    private void putField(ByteBuffer buffer, byte[][] fields) {
        for (int i = 0; i < args.length; i++) {
            buffer.putInt(fields[i].length);
            buffer.put(fields[i]);
        }
    }

    /**
     * <pre>
     * 说明：计算并设置当前协议内容字节数
     * 实现步骤：
     *   1. 加上callbackKey长度8字节
     *   2. 加上args个数1字节
     *   3. 加上fields的总字节数
     *   4. 加上各个field的长度位所占字节数
     *     4.1. uriLength占4B
     *     4.2. argLength占4B
     *   5. 为当前协议实例的contentLength赋值并返回
     * </pre>
     * @param fields 当前协议字段的字节数组表示形式
     * @return 当前协议的总内容长度
     * @since 0.0.0
     */
    private long calContentLength(byte[][] fields) {
        long fieldsByteNumber = fieldLength(fields);
        long eachFieldLengthByteNumber = 4L + ((long) args.length * Integer.BYTES);
        setContentLength(Long.BYTES + Byte.BYTES + fieldsByteNumber + eachFieldLengthByteNumber);
        return contentLength();
    }

    private long fieldLength(byte[][] fields) {
        long sum = 0;
        for (byte[] field : fields) {
            sum += field.length;
        }
        return sum;
    }

    /**
     * <pre>
     * 说明：
     *   1. 将当前协议的主要字段封装为一个二维字节数组
     *   2. 主要涉及字段为uri和args
     * 实现步骤：
     *   1. 构建result数组
     *   2. result[0] 赋值为uri
     *   3. 循环地将args赋值给result
     *   4. 返回result
     * </pre>
     * @return 当前协议字段的组成的二维字节数组
     * @since 0.0.0
     */
    private byte[][] fields() throws IOException {
        byte[][] result = new byte[1 + args.length][];
        result[0] = uri.getBytes();
        for (int i = 0; i < args.length; i++) {
            result[i + 1] = ObjectByteParseUtils.parseObjectToByte(args[i]);
        }
        return result;
    }

    @Override
    public String tempFile() {
        return "";
    }

    public String uri() {
        return uri;
    }

    public Object[] args() {
        return args;
    }
}

