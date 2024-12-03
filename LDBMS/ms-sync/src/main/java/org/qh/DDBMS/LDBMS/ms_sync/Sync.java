package org.qh.DDBMS.LDBMS.ms_sync;

import com.qh.net.Receiver;
import com.qh.protocol.net.BaseTransportProtocol;
import com.qh.protocol.net.TransportProtocol;
import org.qh.DDBMS.common.input.DDBMSReceiver;

/**
 *
 * @Author: qihe
 * @Date: 2024/10/28
 * @Version: 0.0.0
 * @Description: 该接口提供在从站点执行数据库更新同步API
 */
public interface Sync extends DDBMSReceiver<BaseTransportProtocol, Object> {

    /**
     * <pre>
     * 说明：根据接受到的同步协议进行数据库同步
     * </pre>
     * @param baseTransportProtocol 基础传输协议
     * @return 传输协议
     * @since 0.0.0
     */
    TransportProtocol sync(BaseTransportProtocol baseTransportProtocol) throws Exception;

    @Override
    default TransportProtocol receive(BaseTransportProtocol baseTransportProtocol, Object obj) throws Exception {
        return sync(baseTransportProtocol);
    }
}

















