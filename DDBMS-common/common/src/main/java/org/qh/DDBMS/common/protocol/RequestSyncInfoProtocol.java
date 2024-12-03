package org.qh.DDBMS.common.protocol;

import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.db.DBTransaction;

import java.io.Serializable;
import java.sql.Statement;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: 用于请求同步信息的协议类
 * @Specification:
 *   1) dataUse:
 *     1. 1表示该协议实例中包含请求同步信息的事务对象
 *   2) data:
 *     1. lastDBTransaction
 *     2. requiredDBTransaction
 */
public class RequestSyncInfoProtocol extends ObjectTransportProtocol implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * <pre>
     * 说明：data[]中各个对象的数据类型
     * </pre>
     */

    private static final Class<?>[] DATA_TYPES = new Class[]{String.class, Long.class, String.class};

    /**
     * <pre>
     * 说明：该类的构造器
     * 实现步骤：
     *   1) 调用父类的构造器
     * </pre>
     * @param dbName 请求哪个数据库的同步信息
     * @param transactionCount 最后一个数据库提交的事务
     * @param siteName 请求站点名
     * @since 0.0.0
     */
    public RequestSyncInfoProtocol(String dbName, long transactionCount, String siteName) {
        super(Constant.Protocol.SYNC_INFO_PROTOCOL_TYPE, Constant.ObjectProtocolDataUse.REQUEST_SYNC_INFO_PROTOCOL,
               dbName, transactionCount, siteName);
    }

    /**
     * <pre>
     * 说明：将传入的参数解析为一个RequestSyncInfoProtocol实例
     * 实现步骤：
     *   1) 调用父类的构造器
     * </pre>
     *
     * @param bytes 协议的字节形式
     * @since 0.0.0
     */
    public RequestSyncInfoProtocol(byte[] bytes) {
        super(bytes);
    }

    /**
     * <pre>
     * 说明：检查传入的参数是否合规
     * 实现步骤：
     *   1) 判定data == null || data长度 != 3，则返回"The data field is not valid."
     *   2) 判定data中存在null，则返回"There is null in data."。
     *   3) 判定3个实例类型不是String,long, String则返回"The content of the data is not valid."
     *   3) 返回null
     * </pre>
     *
     * @param protocol 当前协议实例
     * @return 错误信息或null
     * @since 0.0.0
     */
    @Override
    public String validate(ObjectTransportProtocol protocol) {
        Object[] data = protocol.getData();
        if (data == null || data.length != 3) {
            return "The data field is not valid.";
        }
        for (int i = 0; i < 3; i++) {
            if (data[i] == null) return "There is null in data.";
            if (!DATA_TYPES[i].isInstance(data[i])) return "The content of the data is not valid.";
        }
        return null;
    }

}
