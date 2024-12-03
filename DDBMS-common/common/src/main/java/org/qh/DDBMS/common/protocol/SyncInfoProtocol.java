package org.qh.DDBMS.common.protocol;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: 用于发送同步信息的协议
 * @Specification:
 *   1) dataUse:
 *     1. 0表示该协议实例中包含要求"从站点"进行同步的事务信息
 *   2) data；
 *     1. SyncInfoEntity[]
 */

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.entity.SyncInfoEntity;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * <pre>
 * 说明：用于发送同步信息的协议
 * 父类：ObjectTransportProtocol
 * type: 39
 * </pre>
 */
public class SyncInfoProtocol extends ObjectTransportProtocol implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * <pre>
     * 说明：该类实例的二进制形式缓存
     * </pre>
     */
    private ByteBuffer buffer;

    /**
     * <pre>
     * 说明：构造器，要求传入一个SyncInfoEntity集合
     * 实现步骤：
     *   1) 调用父类的构造器
     *      1. dataUse = 0
     * </pre>
     *
     * @param list SyncInfoEntity集合
     * @since 0.0.0
     */
    public SyncInfoProtocol(List<SyncInfoEntity> list) {
        super(Constant.Protocol.SYNC_INFO_PROTOCOL_TYPE, Constant.ObjectProtocolDataUse.SYNC_INFO_PROTOCOL,
                list);
    }

    /**
     * <pre>
     * 说明：根据二级协议的字节形式转化为该协议实例
     * 实现步骤：
     *   1) 调用父类构造器方法进行初步初始化
     * </pre>
     *
     * @param bytes 协议的字节形式
     * @since 0.0.0
     */
    public SyncInfoProtocol(byte[] bytes) {
        super(bytes);

    }

    /**
     * <pre>
     * 说明：检验传入参数是否合规。
     * 实现步骤：
     *   1) 调用父类的该方法，返回非null，则直接返回结果。
     *   2) 判定dataUse不是0，返回
     *   "The dataUse field is value(invalid)."
     *   3) 判定data是空，则返回"The data field is
     *   empty."
     *   4) 判定data中存在null，则返回
     *   "There is null in data."
     *   5) 返回null
     * </pre>
     *
     * @param oTP 当前协议实例
     * @return 错误信息或null
     * @since 0.0.0
     */
    @Override
    public String validate(ObjectTransportProtocol oTP) {
        String parentValidation = super.validate(oTP);
        if (parentValidation != null) return parentValidation;
        if (oTP.getDataUse() != Constant.ObjectProtocolDataUse.SYNC_INFO_PROTOCOL) {
            return "The dataUse field is " + oTP.getDataUse() + "(invalid).";
        }
        Object[] data = oTP.getData();
        if (data == null || data.length == 0) return "The data field is empty.";
        for (Object entity : data) {
            if (entity == null) {
                return "There is null in data.";
            }
        }
        return null;
    }

    /**
     * <pre>
     * 说明：将当前协议转化为ByteBuffer形式
     * 实现步骤：
     *   1) 判定buffer不是null，则直接返回buffer
     *   2) 调用父类的data方法，获取到buffer
     *   3) 返回为当前buffer赋值并返回buffer
     * </pre>
     *
     * @return ByteBuffer
     * @since 0.0.0
     */
    public ByteBuffer data() {
        if (buffer != null) {
            return buffer;
        }
        return buffer = super.data();
    }
}

