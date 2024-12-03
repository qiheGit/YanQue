package org.qh.DDBMS.common.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.qh.protocol.exception.ProtocolFieldException;
import com.qh.protocol.exception.ProtocolResolveException;
import com.qh.protocol.net.AbstractTransferProtocol;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.Validator;
import org.qh.tools.parse.ObjectByteParseUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/26
 * @Version: 0.0.0
 * @Description: 确认协议
 */

public class ACKProtocol extends AbstractTransferProtocol<ByteBuffer> implements Validator<ACKProtocol, String>, Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * <pre>
     * 说明：确认信息类型
     * </pre>
     */
    private short ackType;

    /**
     * <pre>
     * 说明：确认信息附带的数据
     * 注意实现：
     *   1. data中元素不可以为null
     * </pre>
     */
    private Object[] data;

    /**
     * <pre>
     * 说明：协议实例缓存
     * </pre>
     */
    private ByteBuffer buffer;

    /**
     * <pre>
     * 说明：此类全参构造器
     * 实现步骤：
     *   1) 为属性赋值
     *   2) 将当前实例作为参数执行validate()
     *      1. validate返回非null，则抛出参数异常
     * </pre>
     *
     * @param ackType 确认信息类型
     * @param data 附带的数据
     * @since 0.0.0
     */
    public ACKProtocol(short ackType, Object... data) {
        super(Constant.Protocol.ACK_PROTOCOL_TYPE);
        this.ackType = ackType;
        this.data = data;

        String validationError = validate(this);
        if (validationError != null) {
            throw new ProtocolFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：根据传入的数据，构建一个该类的实例
     * 实现步骤：
     *   1) 解析出type
     *   2) 解析出contentLength
     *   3) 解析出ackType
     *   4) 解析出data
     *   5) 将当前实例作为参数执行validate()
     *      1. validate返回非null，则抛出参数异常
     * </pre>
     *
     * @param protocol 字节数组形式的该类实例
     * @since 0.0.0
     */
    public ACKProtocol(byte[] protocol) {
        super(protocol[0]);
        try {
            ByteBuffer buffer = ByteBuffer.wrap(protocol);
            buffer.get(); // 解析出协议类型
            setContentLength(buffer.getLong());
            this.ackType = buffer.getShort();
            resolveData(buffer);
        } catch (IOException e) {
            throw new ProtocolResolveException(e);
        }
        String validationError = validate(this);
        if (validationError != null) {
            throw new ProtocolFieldException(validationError);
        }
    }

    /**
     * <pre>
     * 说明：从buffer中解析出当前协议的附带数据
     * 实现步骤：
     *   1. 解析出数据个数
     *   2. 构建data实例
     *   3. 循环地解析出所有的数据
     *     3.1. 解析出数据所占的长度
     *     3.2. 解析出数据
     * </pre>
     * @param buffer 数据封装buffer
     * @return 当前协议的附带数据
     * @since 0.0.0
     */
    private void resolveData(ByteBuffer buffer) throws IOException {
        int dataCount = buffer.get();
        byte[] obj = null;
        this.data = new Object[dataCount];

        for (int i = 0; i < dataCount; i++) {
            obj = new byte[buffer.getInt()];
            buffer.get(obj);
            this.data[i] = ObjectByteParseUtils.parseByteToObject(obj);
        }
    }

    /**
     * <pre>
     * 说明：检查传入参数是否合规
     * 实现步骤：
     *   1) 判定ackType不存在，返回"The ackType is value(invalid)."
     * </pre>
     *
     * @param ackProtocol 要检查的ACKProtocol实例
     * @return 校验结果，返回错误信息或null
     * @since 0.0.0
     */
    @Override
    public String validate(ACKProtocol ackProtocol) {
        switch (ackProtocol.ackType) {
            case Constant.ACKType.COMMIT_TYPE:
            case Constant.ACKType.RESPONSE_COMMIT_TYPE:
                break;
            default:
                return "The ackType is " + ackProtocol.ackType + "(invalid).";
        }
        return null;
    }

    /**
     * <pre>
     * 说明：返回当前协议的ByteBuffer格式
     * 实现步骤：
     *   1) 计算该协议的contentLength
     *   2) 构建一个ByteBuffer实例
     *   3) 将type装入buf
     *   4) 将contentLength装入buf
     *   5) 将ackType装入buf
     *   6) 将数据个数装入buf
     *   7) 循环地将各个数据装入buf
     *      1. 装入数据的类型长度
     *      2. 装入数据类型
     *      3. 装入数据长度
     *      4. 装入数据
     *   8) 返回buf
     * </pre>
     *
     * @return ByteBuffer 格式的协议数据
     * @since 0.0.0
     */
    @Override
    public ByteBuffer data() {
        ByteBuffer buf = null;
        if (this.buffer != null) return this.buffer;
        try {
            byte[][] binaryData = binaryData();
            calContentLength(binaryData);
            buf = ByteBuffer.allocate((int) (1 + 8 + contentLength()));
            buf.put(type()); // type
            buf.putLong(contentLength()); // contentLength
            buf.putShort(ackType); // ackType
            putData(buf, binaryData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.buffer = buf;
    }

    /**
     * 说明：将当前协议实例的data装入buf
     * 实现步骤：
     *   1. 将data.length装入buf
     *   2. 循环地装入data
     *     2.1. 装入data[i]的长度
     *     2.2. 装入data[i]
     * @param buf 当前实例封装的buf
     * @param binaryData 当前协议附带数据的二进制形式
     * @since 0.0.0
     */
    private void putData(ByteBuffer buf, byte[][] binaryData) {
        buf.put((byte) data.length);
        for (int i = 0; i < data.length; i++) {
            buf.putInt(binaryData[i].length);
            buf.put(binaryData[i]);
        }
    }

    /**
     * <pre>
     * 说明：返回当前协议附带数据的二进制形式
     * 实现步骤：
     *   1. 判定data为空则返回空数据
     *   2. 构建binaryData实例
     *   3. 循环地将data转化为binaryData
     *     3.1. 将data[i]装入binaryData
     *   4. 返回装入binaryData
     * </pre>
     * @return data属性的二进制形式
     * @throws JsonProcessingException
     * @since 0.0.0
     */
    private byte[][] binaryData() throws IOException {
        if (data == null || data.length < 1) return new byte[0][];
        byte[][] binaryData = new byte[data.length][];
        for (int i = 0; i < data.length; i++) {
            binaryData[i] = ObjectByteParseUtils.parseObjectToByte(data[i]);
        }
        return binaryData;
    }

    /**
     * <pre>
     * 说明：计算当前协议的内容长度
     * 实现步骤：
     *   1. 加上ackType占2字节
     *   2. 加上data.length占1字节
     *   3. 加上各个data[i]的实例占长度占4个字节
     *   4. 加上binaryData总字节长度
     *   5. 设置当前协议的内容长度
     * </pre>
     * @since 0.0.0
     */
    private void calContentLength(byte[][] binaryData) {
        int sum = 2 + 1 + data.length * Integer.BYTES;
        for (byte[] binaryDatum : binaryData) {
            sum += binaryDatum.length;
        }
        setContentLength(sum);
    }

    @Override
    public String tempFile() {
        return "";
    }

    public short getAckType() {
        return ackType;
    }


    public Object[] getData() {
        return data;
    }

}
