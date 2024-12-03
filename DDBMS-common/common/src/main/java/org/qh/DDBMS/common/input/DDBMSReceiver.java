package org.qh.DDBMS.common.input;

import com.qh.protocol.net.TransportProtocol;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @Author: qihe
 * @Date: 2024/11/16
 * @Version: 0.0.0
 * @Description: DDBMS中的Receiver接口, 用于接受处理client发送的协议对象
 */
public interface DDBMSReceiver<Q extends TransportProtocol, F> {
    /**
     * <pre>
     * 说明：处理接收到的协议
     * @param protocol Q 接收到的协议
     * @param obj F 处理过程中所需要的辅助对象
     * @return TransportProtocol 处理后的协议
     * @since 0.0.0
     */
    TransportProtocol receive(Q protocol, F obj) throws Exception;
}

