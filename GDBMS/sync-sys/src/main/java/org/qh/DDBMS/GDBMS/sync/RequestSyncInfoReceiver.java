package org.qh.DDBMS.GDBMS.sync;


import com.qh.protocol.exception.ProtocolResolveException;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.common.Constant;
import org.qh.DDBMS.common.entity.SyncInfoEntity;
import org.qh.DDBMS.common.input.DDBMSReceiver;
import org.qh.DDBMS.common.output.DDBMSSender;
import org.qh.DDBMS.common.protocol.RequestSyncInfoProtocol;
import org.qh.DDBMS.common.protocol.SyncInfoProtocol;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/18
 * @Version: 0.0.0
 * @Description: 接受处理请求同步信息的接收器
 * @Specification
 *   1. 协议类型：SYNC_INFO_PROTOCOL_TYPE
 *   2. 对象协议数据用途：REQUEST_SYNC_INFO_PROTOCOL
 */
public class RequestSyncInfoReceiver implements DDBMSReceiver<BaseTransportProtocol, Object> {

    /**
     * <pre>
     * 说明：同步模块顶级接口
     * </pre>
     */
    @Resource
    private Sync sync;

    /**
     * <pre>
     * 说明：用于发送协议实例
     * </pre>
     */
    @Resource
    private DDBMSSender<String> sender;

    /**
     * <pre>
     * 说明：接受用户协议，并进行处理
     * 规范：
     *   1. RequestSyncInfoProtocol协议中的参数
     *     - dbName
     *     - last
     *     - siteName
     * 实现步骤：
     *   1) 判定BaseTransportProtocol不是RequestSyncInfoProtocol，则抛出解析异常
     *   2) 将协议转化为RequestSyncInfoProtocol实例
     *   3) 解析得到数据库名和最后提交事务同步信息id
     *   4) 调用Sync的retrieve()，并得到result
     *   5) 将result封装为SyncInfoProtocol返回
     * </pre>
     *
     * @param protocol 用户协议
     * @param obj 附加数据
     * @return 处理后的同步信息协议
     * @throws Exception 解析异常
     */
    @Override
    public TransportProtocol receive(BaseTransportProtocol protocol, Object obj) throws InvocationTargetException, IllegalAccessException {
        byte[] bytes = protocol.data();
        if (bytes[0] != Constant.Protocol.SYNC_INFO_PROTOCOL_TYPE ||
                bytes[Byte.BYTES + Long.BYTES] != Constant.ObjectProtocolDataUse.REQUEST_SYNC_INFO_PROTOCOL) {
            throw new ProtocolResolveException("Do not accept the protocol");
        }
        RequestSyncInfoProtocol p = new RequestSyncInfoProtocol(bytes);

        List<SyncInfoEntity> res = null;
        try {
            res = sync.retrieve((String) p.getData()[0], (Long) p.getData()[1]);
        } catch (SQLException e) {
        }
        sender.send((String) p.getData()[2], new SyncInfoProtocol(res), null, false);
        return null;

    }
}
